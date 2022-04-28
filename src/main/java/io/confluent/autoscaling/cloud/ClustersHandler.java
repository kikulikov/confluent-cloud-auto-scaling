package io.confluent.autoscaling.cloud;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ClustersHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClustersHandler.class.getName());
    private static final String GET_TEMPLATE = "https://api.confluent.cloud/cmk/v2/clusters/%s?environment=%s";
    private static final String PATCH_TEMPLATE = "https://api.confluent.cloud/cmk/v2/clusters/%s";
    private static final String PATCH_BODY = "{\"spec\":{\"config\":{\"cku\":%d,\"kind\":\"Dedicated\"},\"environment\":{\"id\":\"%s\"}}}";
    private static final MediaType MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");
    private static final String REQUEST_ERR = "Failed %s request with status [%s] and message [%s]";
    private static final String IO_ERR = "Failed %s request to %s";

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    private final String readClustersUrl;
    private final String updateClustersUrl;
    private final String environmentId;
    private final String basicAuth;

    public ClustersHandler(String authKey, String authSecret, String clusterId, String environmentId) {
        this.basicAuth = Credentials.basic(authKey, authSecret);
        this.environmentId = environmentId;
        this.readClustersUrl = String.format(GET_TEMPLATE, clusterId, environmentId);
        this.updateClustersUrl = String.format(PATCH_TEMPLATE, clusterId);
    }

    public JsonElement readCluster() throws RequestException {
        final var request = new Request.Builder()
                .addHeader("Authorization", basicAuth)
                .url(readClustersUrl).build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return gson.fromJson(response.body().string(), JsonElement.class);
            }
            throw new RequestException(String.format(REQUEST_ERR, "GET", response.code(), response.message()));
        } catch (IOException e) {
            throw new RequestException(String.format(IO_ERR, "GET", readClustersUrl), e);
        }
    }

    // The number of Confluent Kafka Units (CKUs) for Dedicated cluster types.
    // MULTI_ZONE dedicated clusters must have more than two CKUs.

    public JsonElement updateCluster(int ckuNumber) throws RequestException {
        final var strBody = String.format(PATCH_BODY, ckuNumber, this.environmentId);
        final var requestBody = RequestBody.create(strBody, MEDIA_TYPE);
        final var request = new Request.Builder()
                .addHeader("Authorization", basicAuth)
                .url(updateClustersUrl).patch(requestBody).build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return gson.fromJson(response.body().string(), JsonElement.class);
            }
            throw new RequestException(String.format(REQUEST_ERR, "PATCH", response.code(), response.message()));
        } catch (IOException e) {
            throw new RequestException(String.format(IO_ERR, "PATCH", readClustersUrl), e);
        }
    }
}

/*
{
  "api_version": "cmk/v2",
  "id": "lkc-22rwq2",
  "kind": "Cluster",
  "metadata": {
    "created_at": "2022-03-28T17:06:26.917794Z",
    "resource_name": "crn://confluent.cloud/organization=2f2c58a1-2c22-4597-bb70-3469daa4e326/environment=env-7qgq2/cloud-cluster=lkc-22rwq2/kafka=lkc-22rwq2",
    "self": "https://api.confluent.cloud/cmk/v2/clusters/lkc-22rwq2",
    "updated_at": "2022-04-20T13:26:45.942867Z"
  },
  "spec": {
    "availability": "SINGLE_ZONE",
    "cloud": "AWS",
    "config": {
      "cku": 1,
      "kind": "Dedicated",
      "zones": [
        "euw2-az3"
      ]
    },
    "display_name": "kk-aws-dedicated",
    "environment": {
      "api_version": "org/v2",
      "id": "env-7qgq2",
      "kind": "Environment",
      "related": "https://api.confluent.cloud/org/v2/environments/env-7qgq2",
      "resource_name": "crn://confluent.cloud/organization=2f2c58a1-2c22-4597-bb70-3469daa4e326/environment=env-7qgq2"
    },
    "http_endpoint": "https://pkc-yy1p7.eu-west-2.aws.confluent.cloud:443",
    "kafka_bootstrap_endpoint": "SASL_SSL://pkc-yy1p7.eu-west-2.aws.confluent.cloud:9092",
    "region": "eu-west-2"
  },
  "status": {
    "cku": 1,
    "phase": "PROVISIONED"
  }
}
 */