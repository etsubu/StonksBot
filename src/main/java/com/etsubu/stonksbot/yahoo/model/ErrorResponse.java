package com.etsubu.stonksbot.yahoo.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Optional;

public record ErrorResponse (String code, String description){
    public static Optional<ErrorResponse> parseErrorResponse(String json) {
        try {
            JSONObject root = new JSONObject(json);
            JSONObject data = root.getJSONObject("finance").getJSONObject("error");
            return Optional.of(new ErrorResponse(data.getString("code"), data.getString("description")));
        } catch (JSONException e) {
            return Optional.empty();
        }
    }

    public boolean missingCrumb() {
        return description.equals("Invalid Crumb");
    }

    public boolean missingCookie() {
        return description.equals("Invalid Cookie");
    }
}
