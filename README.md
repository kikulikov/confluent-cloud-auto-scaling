# Confluent Cloud Auto Scaling

## Scaling Up Story

`GIVEN` A dedicated cluster on Confluent Cloud

`WHEN` the load suddenly increases and reaches the capacity limit of the cluster

`THEN` we want to scale up the dedicated cluster automatically (increase the number of CKUs)

## Scaling Down Story

`GIVEN` A dedicated cluster on Confluent Cloud

`WHEN` the load decreases over time and reaches there is a lot of capacity of the cluster

`THEN` we want to scale down the dedicated cluster automatically (decrease the number of CKUs)

## Cluster Limits of 1 CKU

| Fixed limits | ---
| --- | ---
| Storage    | unlimited
| Partitions | up to 4,500
| Connection attempts | up to 250/s

| Guidelines | ---
| --- | --- |
| *Ingress | ~50 MB/s
| *Egress | ~150 MB/s
| *Requests | ~15,000/s
| *Client connections | ~9,000

## Config

The service is implemented in a way similar to other cloud auto-scaling tools.

You need to specify auto-scaling boundaries so your CC cluster never scales above or below the certain limit.

```properties
auto.scaling.min.size=1
auto.scaling.max.size=5
```

Also, you need to describe a list of metrics to monitor and the thresholds to trigger the cluster size adjustment.
Currently, the service supports only `received_bytes,sent_bytes,request_count,active_connection_count,cluster_load_percent` 
metrics from [Metrics Reference](https://api.telemetry.confluent.cloud/docs/descriptors/datasets/cloud). 

```properties
auto.scaling.lower.threshold=20
auto.scaling.upper.threshold=50
auto.scaling.metrics=received_bytes,active_connection_count,cluster_load_percent
```

And the number of evaluations above/below the threshold (so occasional intermittent short term spikes do not trigger the sizing).

```properties
auto.scaling.evaluation.periods=3
```

Confluent Cloud credentials are essential as well, so it can execute commands and retrieve monitoring metrics.

```properties
confluent.cloud.key=4JWTGETPDCFZL3HI
confluent.cloud.secret=XXX
```

Please, see a full configuration example below.

```properties
name=confluent-cloud-auto-scaling
connector.class=io.confluent.autoscaling.connect.AutoScalingConnector
confluent.cloud.key=4JWTGETPDCFZL3HI
confluent.cloud.secret=XXX
auto.scaling.environment=env-7qgq2
auto.scaling.clusters=lkc-22rwq2
auto.scaling.poll.interval.ms=25000
auto.scaling.interval=now-2h|h/now
auto.scaling.period=PT1M
auto.scaling.evaluation.periods=3
auto.scaling.lower.threshold=20
auto.scaling.upper.threshold=50
auto.scaling.min.size=1
auto.scaling.max.size=5
auto.scaling.metrics=received_bytes,active_connection_count,cluster_load_percent
auto.scaling.dry.run=false
```

To ensure a faster response to changes in the metric value, we recommend that you scale on metrics with a 1-minute (`PT1M`)
frequency. Scaling on metrics with a 5-minute (`PT5M`) frequency can result in slower response times and scaling on stale metric
data.

## Usage

The service is utilising the Kafka Connect framework, so you can run it as a connector in a cluster or stand alone.

Build the project as

```shell
mvn clean package
```

Run in a standalone mode

```shell
connect-standalone connect-standalone.properties connect-auto-scaling.properties
```

The examples of Kafka Connect and application configuration files can be seen at 
[connect-standalone.properties](src/main/resources/connect-standalone.properties) 
and [connect-auto-scaling.properties](src/main/resources/connect-auto-scaling.properties)  

For more details on the standalone mode please see 
[Configuring and Running Workers](https://docs.confluent.io/home/connect/self-managed/userguide.html#standalone-mode).

## Miscellaneous

Confluent Cloud Metrics API Reference
https://api.telemetry.confluent.cloud/docs/descriptors/datasets/cloud

Confluent Cloud Metrics
https://docs.confluent.io/cloud/current/monitoring/metrics-api.html

Dedicated clusters
https://docs.confluent.io/cloud/current/clusters/cluster-types.html#dedicated-cluster

Confluent Cloud APIs
https://docs.confluent.io/cloud/current/api.html

Discover available resources
https://docs.confluent.io/cloud/current/monitoring/metrics-api.html#discover-available-resources

Discover available metrics
https://docs.confluent.io/cloud/current/monitoring/metrics-api.html#discover-available-metrics

Query metric values
https://api.telemetry.confluent.cloud/docs#tag/Version-2/paths/~1v2~1metrics~1{dataset}~1query/post

```text
Enum: "PT1M" "PT5M" "PT15M" "PT30M" "PT1H" "PT4H" "PT6H" "PT12H" "P1D" "ALL"

Defines the time buckets that the aggregation is performed for. Buckets are specified in 
ISO-8601 duration syntax, but only the enumerated values are supported. Buckets are aligned to 
UTC boundaries. The special ALL value defines a single bucket for all intervals.

The allowed granularity for a query is restricted by the length of that query's interval.
```