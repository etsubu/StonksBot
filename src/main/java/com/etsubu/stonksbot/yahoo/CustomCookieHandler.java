package com.etsubu.stonksbot.yahoo;

import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.Map;

public class CustomCookieHandler extends CookieHandler {
    private final CookieManager defaultCookieManager;

    public CustomCookieHandler() {
        defaultCookieManager = new CookieManager();
    }

    @Override
    public void put(URI uri, Map<String, List<String>> responseHeaders) throws IOException {
        CookieStore store = defaultCookieManager.getCookieStore();
        List<String> cookiesHeader = responseHeaders.get("Set-Cookie");
        if (cookiesHeader != null) {
            for (String cookieHeader : cookiesHeader) {
                try {
                    System.out.println(cookieHeader);
                    HttpCookie cookie = HttpCookie.parse(cookieHeader).get(0);

                    // Check if the cookie has deprecated attributes
                    if (cookie.getValue().contains("$Version") ||
                            cookie.getValue().contains("$Path") ||
                            cookie.getValue().contains("$Domain")) {
                        // Handle the deprecated attributes here
                        // For demonstration, we'll just remove the invalid attributes from the cookie value
                        String cleanedCookieValue = cleanDeprecatedAttributes(cookie.getValue());
                        cookie.setValue(cleanedCookieValue);
                    }

                    store.add(uri, cookie);
                } catch (Exception e) {
                    System.out.println("Skipping cookie " + cookieHeader);
                }
            }
        }
    }

    @Override
    public Map<String, List<String>> get(URI uri, Map<String, List<String>> requestHeaders) throws IOException {
        return defaultCookieManager.get(uri, requestHeaders);
    }

    private String cleanDeprecatedAttributes(String cookieValue) {
        // Remove deprecated attributes like "$Version", "$Path", and "$Domain"
        return cookieValue.replaceAll("\\$Version=DELETE;", "")
                .replaceAll("\\$Path=DELETE;", "")
                .replaceAll("\\$Domain=DELETE;", "");
    }
}