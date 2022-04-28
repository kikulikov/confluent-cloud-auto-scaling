package io.confluent.autoscaling.cloud;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import okhttp3.*;

import java.io.IOException;

public class MetricsHandler {

    private static final String BODY = "{\"aggregations\":[{\"metric\":\"io.confluent.kafka.server/%s\"}]," +
            "\"filter\":{\"field\":\"resource.kafka.id\",\"op\":\"EQ\",\"value\":\"%s\"}," +
            "\"granularity\":\"%s\",\"intervals\":[\"%s\"]}";

    private static final String METRICS_URL = "https://api.telemetry.confluent.cloud/v2/metrics/cloud/query";
    private static final MediaType MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");
    private static final String REQUEST_ERR = "Failed %s request with status [%s] and message [%s]";
    private static final String IO_ERR = "Failed %s request to %s";

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private final String basicAuth;
    private final String clusterId;

    public MetricsHandler(String authKey, String authSecret, String clusterId) {
        this.basicAuth = Credentials.basic(authKey, authSecret);
        this.clusterId = clusterId;
    }

    public JsonElement readMetric(String metric, String period, String interval) throws RequestException {

        final var strBody = String.format(BODY, metric, clusterId, period, interval);
        final var requestBody = RequestBody.create(strBody, MEDIA_TYPE);

        final var request = new Request.Builder()
                .addHeader("Authorization", basicAuth)
                .url(METRICS_URL).post(requestBody).build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                final var str = response.body().string();
                return gson.fromJson(str, JsonElement.class);
            }
            throw new RequestException(String.format(REQUEST_ERR, "GET", response.code(), response.message()));
        } catch (IOException e) {
            throw new RequestException(String.format(IO_ERR, "GET", METRICS_URL), e);
        }
    }

    /**
     * What should I do if a query returns a 5xx response code?
     * We recommended retrying these type of responses. Usually, this is an indication
     * of a transient server-side issue. You should design your client implementations
     * for querying the Metrics API to be resilient to this type of response for minutes-long periods.
     */
}

/*
REQUEST

{
  "aggregations": [
    {
      "metric": "io.confluent.kafka.server/received_bytes",
      "agg": "SUM"
    }
  ],
  "filter": {
    "field": "resource.kafka.id",
    "op": "EQ",
    "value": "lkc-22rwq2"
  },
  "granularity": "PT5M",
  "intervals": [
    "now-2h|h/now"
  ],
  "limit": 50
}

RESPONSE

{
    "data": [
        {
            "timestamp": "2022-04-21T15:10:00Z",
            "value": 1574.0
        },
        {
            "timestamp": "2022-04-21T15:15:00Z",
            "value": 0.0
        },
        {
            "timestamp": "2022-04-21T15:20:00Z",
            "value": 0.0
        },
        {
            "timestamp": "2022-04-21T15:25:00Z",
            "value": 0.0
        },
        {
            "timestamp": "2022-04-21T15:30:00Z",
            "value": 16359.0
        }
    ]
}
 */