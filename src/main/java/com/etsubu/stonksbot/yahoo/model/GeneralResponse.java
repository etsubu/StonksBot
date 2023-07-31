package com.etsubu.stonksbot.yahoo.model;

import com.etsubu.stonksbot.yahoo.StockName;
import com.google.gson.Gson;
import org.json.JSONObject;

public class GeneralResponse {
    private static final Gson gson = new Gson();

    public static DataResponse parseResponse(String json, StockName name) {
        JSONObject root = new JSONObject(json);
        JSONObject data = root.getJSONObject("quoteSummary").getJSONArray("result").getJSONObject(0);
        DataResponse response = gson.fromJson(data.toString(), DataResponse.class);
        response.setName(name);
        return response;
    }
}
