package Core.InderesAPI;

import Core.InderesAPI.DataStructures.RecommendationEntry;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface InderesConnector {
    Set<RecommendationEntry> queryRecommendations() throws IOException, InterruptedException;
    Map<String,RecommendationEntry> queryRecommendationsMap() throws IOException, InterruptedException;
}
