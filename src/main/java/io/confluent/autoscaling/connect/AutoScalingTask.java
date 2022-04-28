package io.confluent.autoscaling.connect;

import com.google.gson.JsonObject;
import io.confluent.autoscaling.cloud.ClustersHandler;
import io.confluent.autoscaling.cloud.MetricsHandler;
import io.confluent.autoscaling.cloud.RequestException;
import kotlin.Pair;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static io.confluent.autoscaling.cloud.CKULimits.*;
import static io.confluent.autoscaling.connect.AutoScalingConfig.*;

public class AutoScalingTask extends SourceTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoScalingTask.class);
    private ClustersHandler clusters;
    private MetricsHandler metrics;
    private AutoScalingConfig config;
    private List<String> metricsList;
    private String clusterId;

    @Override
    public String version() {
        return VersionUtil.getVersion();
    }

    @Override
    public void start(Map<String, String> props) {
        this.config = new AutoScalingConfig(props);
        this.clusterId = config.getList(CLUSTERS).get(0);
        this.clusters = new ClustersHandler(config.getString(CLOUD_KEY), config.getString(CLOUD_SECRET),
                clusterId, config.getString(ENVIRONMENT));
        this.metrics = new MetricsHandler(config.getString(CLOUD_KEY), config.getString(CLOUD_SECRET),
                clusterId);
        this.metricsList = config.getList(METRICS);
    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException {

        try {
            final var state = (JsonObject) clusters.readCluster();

            final var statusCKU = state.getAsJsonObject("status").get("cku").getAsInt();
            final var configCKU = state.getAsJsonObject("spec").getAsJsonObject("config").get("cku").getAsInt();
            final var lowerBound = config.getInt(LOWER_THRESHOLD);
            final var upperBound = config.getInt(UPPER_THRESHOLD);
            final var minCKU = config.getInt(MIN_SIZE);
            final var maxCKU = config.getInt(MAX_SIZE);
            final var dryRun = config.getBoolean(DRY_RUN);

            if (statusCKU > configCKU) {
                LOGGER.info("Cluster shrinking in progress");
            } else if (statusCKU < configCKU) {
                LOGGER.info("Cluster expansion in progress");
            } else {

                final var expansionNeeded = new LongAdder();
                final var shrinkingNeeded = new LongAdder();

                metricsList.forEach(metric -> {
                    try {
                        final var periods = config.getInt(EVALUATION_PERIODS);

                        final var json = (JsonObject) metrics.readMetric(metric, config.getString(PERIOD),
                                config.getString(INTERVAL));

                        final var data = json.getAsJsonArray("data");

                        final var allElements = StreamSupport
                                .stream(data.spliterator(), false)
                                .map(e -> (JsonObject) e)
                                .map(o -> new Pair<>(o.get("timestamp").getAsString(), o.get("value").getAsBigDecimal()))
                                .sorted(Comparator.comparing(Pair<String, BigDecimal>::getFirst))
                                .collect(Collectors.toList());

                        final List<Pair<String, BigDecimal>> subElements = allElements.size() > periods ? allElements
                                .subList(allElements.size() - periods, allElements.size()) : allElements;

                        final var elements = subElements.stream()
                                .map(p -> extracted(statusCKU, metric, p.getSecond(), config.getString(PERIOD)))
                                .collect(Collectors.toList());

                        LOGGER.info("Metric [" + metric + "] evaluations " + elements);

                        if (elements.stream().allMatch(p -> p > upperBound)) expansionNeeded.increment();
                        if (elements.stream().allMatch(p -> p < lowerBound)) shrinkingNeeded.increment();

                    } catch (RequestException e) {
                        LOGGER.error("Failure", e);
                    }
                });

                if (expansionNeeded.intValue() > 0 && statusCKU < maxCKU) {
                    LOGGER.warn("Expanding the cluster [" + clusterId + "]");
                    if (!dryRun) clusters.updateCluster(statusCKU + 1);
                }

                if (shrinkingNeeded.intValue() == metricsList.size() && statusCKU > minCKU) {
                    LOGGER.warn("Shrinking the cluster [" + clusterId + "]");
                    if (!dryRun) clusters.updateCluster(statusCKU - 1);
                }
            }
        } catch (RequestException e) {
            e.printStackTrace();
        }

        Thread.sleep(this.config.getLong(POLL_INTERVAL_MS));

        return null;
    }

    static long extracted(int statusCKU, String metricName, BigDecimal metricValue, String timeBucket) {
        return switch (metricName) {
            case RECEIVED_BYTES -> asPercent(statusCKU, metricValue.longValue(), limitToBucket(RECEIVED_BYTES_LIMIT, timeBucket));
            case SENT_BYTES -> asPercent(statusCKU, metricValue.longValue(), limitToBucket(SENT_BYTES_LIMIT, timeBucket));
            case REQUEST_COUNT -> asPercent(statusCKU, metricValue.longValue(), limitToBucket(REQUEST_COUNT_LIMIT, timeBucket));
            case CONNECTION_COUNT -> asPercent(statusCKU, metricValue.longValue(), limitToBucket(CONNECTION_COUNT_LIMIT, timeBucket));
            case CLUSTER_LOAD_PERCENT -> (long) (metricValue.doubleValue() * 100);
            default -> throw new UnsupportedOperationException("Metric [" + metricName + "] is not supported.");
        };
    }

    /**
     * "PT1M" "PT5M" "PT15M" "PT30M" "PT1H" "PT4H" "PT6H" "PT12H" "P1D"
     */
    static long limitToBucket(long metricLimit, String timeBucket) {
        return switch (timeBucket) {
            case "PT1M" -> metricLimit * 60;
            case "PT5M" -> metricLimit * 60 * 5;
            case "PT15M" -> metricLimit * 60 * 15;
            case "PT30M" -> metricLimit * 60 * 30;
            case "PT1H" -> metricLimit * 60 * 60;
            case "PT4H" -> metricLimit * 60 * 60 * 4;
            case "PT6H" -> metricLimit * 60 * 60 * 6;
            case "PT12H" -> metricLimit * 60 * 60 * 12;
            case "P1D" -> metricLimit * 60 * 60 * 24;
            default -> throw new UnsupportedOperationException("The bucket [" + timeBucket + "] is not supported.");
        };
    }

    static long asPercent(int statusCKU, long metricValue, long metricLimit) {
        return (100L * metricValue) / (metricLimit * statusCKU);
    }

    @Override
    public void stop() {
        LOGGER.warn("Stopping the task...");
    }
}
