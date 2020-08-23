package Core.Lunch;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public class LunchResponse {
    @SerializedName("LunchMenu")
    private LunchMenu menu;

}
