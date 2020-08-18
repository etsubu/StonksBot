package Core.YahooAPI.DataStructures.SearchStructures;

import lombok.Getter;

import java.util.List;

@Getter
public class SearchResponse {
    private int count;
    private List<AssetEntry> quotes;
}
