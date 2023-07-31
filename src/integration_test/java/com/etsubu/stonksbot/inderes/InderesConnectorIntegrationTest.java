package com.etsubu.stonksbot.inderes;

import com.etsubu.stonksbot.inderes.model.RecommendationEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class InderesConnectorIntegrationTest {
    private InderesConnector inderesConnector;

    @BeforeEach
    public void init() {
        inderesConnector = new InderesConnectorImpl();
    }

    @Test
    public void testQueryRecommendations() throws IOException, InterruptedException {
        Set<RecommendationEntry> recommendations = inderesConnector.queryRecommendations();
        assertFalse(recommendations.isEmpty());
        // Verify that there is at least one entry that has all the valid parameters
        assertTrue(recommendations.stream().anyMatch(x -> {
            boolean valid;
            valid = (x.getName() != null);
            valid &= (x.getCurrency() != null);
            valid &= (x.getIsin() != null);
            valid &= (x.getRecommendation() != null);
            valid &= (x.getRecommendationText() != null);
            valid &= (x.getRisk() != null);
            valid &= (x.getTarget() != null);
            int r = Integer.parseInt(x.getRecommendation());
            valid &= (r >= 1 && r <= 5);
            return valid;
        }));
        // Check that recommendation types have not changed
        assertTrue(recommendations.stream().allMatch(x -> x.getRecommendation() == null
                || (Integer.parseInt(x.getRecommendation()) >= 1 && Integer.parseInt(x.getRecommendation()) <= 5)));
    }
}
