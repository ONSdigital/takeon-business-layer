package uk.gov.ons.collection.service;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import lombok.extern.log4j.Log4j2;
import uk.gov.ons.collection.exception.ApiRequestException;

@Log4j2
public class ApiRequest {

    private String url;
    private String data;
    CloseableHttpClient httpClient;
    int statusCode;
    private static final int STATUS_OK = 200;

    public ApiRequest(String url) {
        this.url = url;
    }

    public ApiRequest(String url, String dataToSend) {
        this.url = url;
        this.data = dataToSend;
    }

    public void apiPostJson() throws ApiRequestException, IOException {
        httpClient = HttpClientBuilder.create().build();
        try {
            HttpPost request = new HttpPost(url);
            StringEntity params = new StringEntity(data, ContentType.APPLICATION_JSON);
            request.addHeader("Content-Type", "application/json");
            request.setEntity(params);
            HttpResponse httpResp = httpClient.execute(request);
            statusCode = httpResp.getStatusLine().getStatusCode();
            if (statusCode != STATUS_OK) {
                throw new IOException("Exception in calling Save Response");
            }
            log.info("HTTP Status Code: " + statusCode);
        } catch (Exception err) {
            log.error("Exception: " + err);
            throw new ApiRequestException("Error with request, HTTP Status code:" + statusCode);
        } finally {
            httpClient.close();
        }
    }

    public void apiPostParameters() throws ApiRequestException, IOException {
        httpClient = HttpClientBuilder.create().build();
        try {
            HttpPost request = new HttpPost(url);
            HttpResponse httpResp = httpClient.execute(request);
            statusCode = httpResp.getStatusLine().getStatusCode();
            log.info("HTTP Status Code: " + statusCode);
            if (statusCode != STATUS_OK) {
                throw new IOException("Exception in calling Derived Save Response");
            }
        } catch (Exception err) {
            log.error("Exception: " + err);
            throw new ApiRequestException("Error with request, HTTP Status code:" + statusCode);
        } finally {
            httpClient.close();
        }
    }

}