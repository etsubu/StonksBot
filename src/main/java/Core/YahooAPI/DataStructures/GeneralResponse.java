package Core.YahooAPI.DataStructures;

import com.google.gson.Gson;
import org.json.JSONObject;

public class GeneralResponse {
    private static final Gson gson = new Gson();

    public static DataResponse parseResponse(String json) {
        JSONObject root = new JSONObject(json);
        JSONObject data = root.getJSONObject("quoteSummary").getJSONArray("result").getJSONObject(0);
        return gson.fromJson(data.toString(), DataResponse.class);
    }
}
