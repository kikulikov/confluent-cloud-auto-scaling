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
| Storage	| unlimited
| Partitions | up to 4,500
| Connection attempts | up to 250/s

| Guidelines | ---
| --- | --- |
| *Ingress | ~50 MB/s
| *Egress | ~150 MB/s
| *Requests | ~15,000/s
| *Client connections | ~3,000

## Links

https://api.telemetry.confluent.cloud/docs/descriptors/datasets/cloud

https://docs.confluent.io/cloud/current/monitoring/metrics-api.html

https://docs.confluent.io/cloud/current/clusters/cluster-types.html#dedicated-cluster

https://docs.confluent.io/cloud/current/api.html

---
## Config

To ensure a faster response to changes in the metric value, we recommend that you scale on 
metrics with a 1-minute frequency. Scaling on metrics with a 5-minute frequency can result 
in slower response times and scaling on stale metric data.

TODO `--warmup 180` ???
TODO `--cooldown 180` ???

TODO check dedicated
TODO scaling when status only
TODO task per cluster

--period 120
--evaluation-periods 2
--lower-threshold 40
--upper-threshold 60
--min-size 1
--max-size 5
--metrics [active_connection_count,partition_count]

1. auto.scaling.environment [string]
2. auto.scaling.clusters [array]
3. auto.scaling.default.threshold [percentage]
4. auto.scaling.poll.interval.ms [ms]
5. auto.scaling.metric.partitions [partition_count]
6. auto.scaling.metric.attempts [???]
7. auto.scaling.metric.ingress [received_bytes]
8. auto.scaling.metric.egress [sent_bytes]
9. auto.scaling.metric.requests [request_count]
10. auto.scaling.metric.connections [active_connection_count]

---
## REST Samples

https://api.telemetry.confluent.cloud/docs#tag/Version-2/paths/~1v2~1metrics~1{dataset}~1query/post

__granularity__

```text
Enum: "PT1M" "PT5M" "PT15M" "PT30M" "PT1H" "PT4H" "PT6H" "PT12H" "P1D" "ALL"

Defines the time buckets that the aggregation is performed for. Buckets are specified in 
ISO-8601 duration syntax, but only the enumerated values are supported. Buckets are aligned to 
UTC boundaries. The special ALL value defines a single bucket for all intervals.

The allowed granularity for a query is restricted by the length of that query's interval.
```

https://docs.confluent.io/cloud/current/quotas/index.html#ccloud-resource-limits-kafka-cluster

Discover available resources 
https://docs.confluent.io/cloud/current/monitoring/metrics-api.html#discover-available-resources

Discover available metrics
https://docs.confluent.io/cloud/current/monitoring/metrics-api.html#discover-available-metrics

http 'https://api.telemetry.confluent.cloud/v2/metrics/cloud/descriptors/resources' --auth '4JWTGETPDCFZL3HI:XXX'
http 'https://api.telemetry.confluent.cloud/v2/metrics/cloud/descriptors/metrics?resource_type=kafka' --auth '4JWTGETPDCFZL3HI:XXX'

```json
{
  "aggregations": [
    {
      "metric": "io.confluent.kafka.server/active_connection_count"
    }
  ],
  "filter": {
    "field": "resource.kafka.id",
    "op": "EQ",
    "value": "lkc-22rwq2"
  },
  "granularity": "PT5M",
  "intervals": [
    "2022-04-01T15:00:00Z/PT1H"
  ],
  "limit": 50
}
```
http GET 'https://api.telemetry.confluent.cloud/v2/metrics/cloud/query' --auth "$API_KEY:$SECRET" < active_connection_count.json

http GET 'https://api.confluent.cloud/cmk/v2/clusters/lkc-22rwq2?environment=env-7qgq2' --auth "$API_KEY:$SECRET"

http PATCH 'https://api.confluent.cloud/cmk/v2/clusters/lkc-22rwq2?environment=env-7qgq2' --auth "$API_KEY:$SECRET" \
--raw '{"spec":{"config":{"cku":2,"kind":"Dedicated"},"environment":{"id":"env-7qgq2"}}}'
