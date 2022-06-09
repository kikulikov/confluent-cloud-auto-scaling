package io.confluent.autoscaling.connect;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static io.confluent.autoscaling.connect.AutoScalingConfig.CLUSTERS;

public class AutoScalingConnector extends SourceConnector {

    private static final Logger log = LoggerFactory.getLogger(AutoScalingConnector.class);
    private static final String CONNECTOR_NAME = AutoScalingConnector.class.getSimpleName();
    private AutoScalingConfig config;
    private Map<String, String> props;

    @Override
    public String version() {
        return VersionUtil.getVersion();
    }

    /**
     * This will be executed once per connector. This can be used to handle connector level setup.
     */
    @Override
    public void start(Map<String, String> map) {
        log.info("The {} has been started.", CONNECTOR_NAME);
        this.config = new AutoScalingConfig(map);
        this.props = Collections.unmodifiableMap(map);
    }

    /**
     * Returns your task implementation.
     */
    @Override
    public Class<? extends Task> taskClass() {
        return AutoScalingTask.class;
    }

    /**
     * Defines the individual task configurations that will be executed.
     * The connector creates a task per cluster from the configuration.
     * The `tasks.max` configuration property will be ignored.
     */
    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        final List<String> clusters = this.config.getList(CLUSTERS);
        final List<Map<String, String>> configs = new ArrayList<>();

        clusters.forEach(cluster -> {
            final var map = new HashMap<>(this.props);
            map.replace(CLUSTERS, cluster); // replace the list of clusters with a single cluster ID
            configs.add(map);
        });

        return configs;
    }

    @Override
    public void stop() {
        log.info("The {} has been stopped.", CONNECTOR_NAME);
    }

    @Override
    public ConfigDef config() {
        return AutoScalingConfig.CONFIG_DEF;
    }
}
