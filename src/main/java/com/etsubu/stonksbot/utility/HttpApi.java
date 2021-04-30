package com.etsubu.stonksbot.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Tiny Api for com.stonksbot.Core.HTTP Get requests
 * @author etsubu
 * @version 28 Aug 2018
 *
 */
public class HttpApi {
    private static final Logger log = LoggerFactory.getLogger(HttpApi.class);
    private static final long TIMEOUT = 30;
    private static final HttpClient client;
    private static final String USER_AGENT_PRODUCT = "StonksBot";
    private static final String VERSION;

    static  {
        client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(TIMEOUT)).build();
        VERSION = VersionParser.applicationVersion();
    }
    /**
     * Sends com.stonksbot.Core.HTTP Get request and returns the response in String
     * @param url URL to request
     * @return Response in String
     * @throws IOException If there was an connection error
     */
    public static Optional<String> sendGet(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if(response.statusCode() != 200) {
            log.error("Request returned invalid status code {}", response.statusCode());
            return Optional.empty();
        }
        return Optional.of(response.body());
    }

    public static Map<String, String> sendMultipleGet(Set<String> urls) throws IOException, InterruptedException {
        Map<String, CompletableFuture<HttpResponse<String>>> responses = new HashMap<>((int)(urls.size()*1.25)+1);
        Map<String, String> responseMap = new HashMap<>((int)(urls.size()*1.25)+1);
        for(String url : urls) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .setHeader("User-Agent", USER_AGENT_PRODUCT + "/" + VERSION)
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();
            CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            responses.put(url, response);
        }
        // Wait for responses and filter those that succeeded with status code 200
        responses.entrySet().stream()
                .map(x -> {
                    try {
                        return new Pair<>(x.getKey(), x.getValue().get());
                    } catch (InterruptedException | ExecutionException e) {
                        log.error("Failed to retrieve wall for profile {}", x.getKey());
                    }
                    return null;
                })
                .filter(x -> x != null && x.second.statusCode() == 200 && x.second.body() != null)
                .forEach(x -> responseMap.put(x.first, x.second.body()));
        return responseMap;
    }

    public static Optional<byte[]> downloadFile(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();
        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if(response.statusCode() != 200) {
            log.error("Request returned invalid status code {}", response.statusCode());
            return Optional.empty();
        }
        return Optional.of(response.body());
    }
}
