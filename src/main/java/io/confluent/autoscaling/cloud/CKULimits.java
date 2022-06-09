package io.confluent.autoscaling.cloud;

/**
 * Cluster Limits of 1 CKU
 */
public class CKULimits {

    public static final String RECEIVED_BYTES = "received_bytes";
    public static final int RECEIVED_BYTES_LIMIT = 50 * 1024 * 1024;

    public static final String SENT_BYTES = "sent_bytes";
    public static final int SENT_BYTES_LIMIT = 150 * 1024 * 1024;

    public static final String REQUEST_COUNT = "request_count";
    public static final int REQUEST_COUNT_LIMIT = 15000;

    public static final String CONNECTION_COUNT = "active_connection_count";
    public static final int CONNECTION_COUNT_LIMIT = 9000;

    public static final String CLUSTER_LOAD_PERCENT = "cluster_load_percent";
}
