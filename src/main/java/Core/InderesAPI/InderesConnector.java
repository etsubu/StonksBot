package Core.InderesAPI;

import Core.InderesAPI.DataStructures.RecommendationEntry;

import java.io.IOException;
import java.util.List;

public interface InderesConnector {
    List<RecommendationEntry> queryRecommendations() throws IOException, InterruptedException;
}
