package io.confluent.autoscaling.connect;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;

import java.util.Map;

public class AutoScalingConfig extends AbstractConfig {

    public static final String CLOUD_KEY = "confluent.cloud.key";
    private static final String CONFLUENT_CLOUD_KEY_DOC = "Confluent Cloud API Key";

    public static final String CLOUD_SECRET = "confluent.cloud.secret";
    private static final String CONFLUENT_CLOUD_SECRET_DOC = "Confluent Cloud API Secret";

    public static final String ENVIRONMENT = "auto.scaling.environment";
    private static final String ENVIRONMENT_DOC = "Environment ID";

    public static final String CLUSTERS = "auto.scaling.clusters";
    private static final String CLUSTERS_DOC = "List of cluster IDs";

    public static final String POLL_INTERVAL_MS = "auto.scaling.poll.interval.ms";
    public static final String POLL_INTERVAL_MS_DOC = "Poll interval in milliseconds";

    public static final String PERIOD = "auto.scaling.period";
    public static final String PERIOD_DOC = "Evaluation period; e.g. PT5M";

    public static final String EVALUATION_PERIODS = "auto.scaling.evaluation.periods";
    public static final String EVALUATION_PERIODS_DOC = "Number of consecutive evaluation periods";

    public static final String INTERVAL = "auto.scaling.interval";
    public static final String INTERVAL_DOC = "Evaluation interval; e.g. now-2h|h/now";

    public static final String LOWER_THRESHOLD = "auto.scaling.lower.threshold";
    public static final String LOWER_THRESHOLD_DOC = "Lower bound for scaling in";

    public static final String UPPER_THRESHOLD = "auto.scaling.upper.threshold";
    public static final String UPPER_THRESHOLD_DOC = "Upper bound for scaling out";

    public static final String MIN_SIZE = "auto.scaling.min.size";
    public static final String MIN_SIZE_DOC = "Minimal number of CKU";

    public static final String MAX_SIZE = "auto.scaling.max.size";
    public static final String MAX_SIZE_DOC = "Maximum number of CKU";

    public static final String METRICS = "auto.scaling.metrics";
    public static final String METRICS_DOC = "List of metrics to observe";

    public static final String DRY_RUN = "auto.scaling.dry.run";
    public static final String DRY_RUN_DOC = "Dry-run mode";

    //  --poll-interval-ms 3000
    //  --interval now-2h|h/now
    //  --period PT5M
    //  --evaluation-periods 2
    //  --lower-threshold 40
    //  --upper-threshold 60
    //  --min-size 1
    //  --max-size 5
    //  --metrics [active_connection_count,partition_count]

    public static final ConfigDef CONFIG_DEF =
            new ConfigDef()
                    .define(CLOUD_KEY, Type.STRING, Importance.HIGH, CONFLUENT_CLOUD_KEY_DOC)
                    .define(CLOUD_SECRET, Type.STRING, Importance.HIGH, CONFLUENT_CLOUD_SECRET_DOC)
                    .define(ENVIRONMENT, Type.STRING, Importance.HIGH, ENVIRONMENT_DOC)
                    .define(CLUSTERS, Type.LIST, Importance.HIGH, CLUSTERS_DOC)
                    .define(POLL_INTERVAL_MS, Type.LONG, Importance.HIGH, POLL_INTERVAL_MS_DOC)
                    .define(INTERVAL, Type.STRING, Importance.HIGH, INTERVAL_DOC)
                    .define(PERIOD, Type.STRING, Importance.HIGH, PERIOD_DOC)
                    .define(EVALUATION_PERIODS, Type.INT, Importance.HIGH, EVALUATION_PERIODS_DOC)
                    .define(LOWER_THRESHOLD, Type.INT, Importance.HIGH, LOWER_THRESHOLD_DOC)
                    .define(UPPER_THRESHOLD, Type.INT, Importance.HIGH, UPPER_THRESHOLD_DOC)
                    .define(MIN_SIZE, Type.INT, Importance.HIGH, MIN_SIZE_DOC)
                    .define(MAX_SIZE, Type.INT, Importance.HIGH, MAX_SIZE_DOC)
                    .define(METRICS, Type.LIST, Importance.HIGH, METRICS_DOC)
                    .define(DRY_RUN, Type.BOOLEAN, false, Importance.LOW, DRY_RUN_DOC);

    public AutoScalingConfig(Map<String, ?> parsedConfig) {
        super(CONFIG_DEF, parsedConfig);
    }
}
