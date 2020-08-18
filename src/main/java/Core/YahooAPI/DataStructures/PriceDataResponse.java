package Core.YahooAPI.DataStructures;

import lombok.Getter;

import java.util.List;

@Getter
public class PriceDataResponse {
    private PriceMeta meta;
    private List<Integer> timestamps;
    private List<Long> timestamp;
}
