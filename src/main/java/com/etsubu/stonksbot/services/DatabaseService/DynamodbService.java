package com.etsubu.stonksbot.services.DatabaseService;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.etsubu.stonksbot.configuration.ConfigLoader;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@AllArgsConstructor
public class DynamodbService implements ItemStorage {
    private static final Logger log = LoggerFactory.getLogger(DynamodbService.class);
    private static final String TABLE_PREFIX = "registration";

    private static final String buildTableName(String serverId) {
        return TABLE_PREFIX + "-" + serverId;
    }

    @Override
    public int entries(String serverId) {
        try {
            var result = AmazonDynamoDBClientBuilder.defaultClient().scan(buildTableName(serverId), List.of("COUNT"));
            return result.getCount();
        } catch (ResourceNotFoundException e) {
            log.error("Table not found", e);
        } catch (AmazonServiceException e) {
            log.error("Amazon service exception", e);
        }
        return -1;
    }

    @Override
    public boolean addEntry(String serverId, String key, Map<String, String> values) {
        String tableName = buildTableName(serverId);
        HashMap<String, AttributeValue> item_values = new HashMap<String, AttributeValue>();

        item_values.put("id", new AttributeValue(key));

        for (String fieldKey : values.keySet()) {
            item_values.put(fieldKey, new AttributeValue(values.get(fieldKey)));
        }

        try {
            AmazonDynamoDBClientBuilder.defaultClient().putItem(tableName, item_values);
            return true;
        } catch (ResourceNotFoundException e) {
            log.error("Table not found", e);
        } catch (AmazonServiceException e) {
            log.error("Amazon service exception", e);
        }
        return false;
    }

    @Override
    public boolean removeEntry(String serverId, String key) {
        String tableName = buildTableName(serverId);
        try {
            AmazonDynamoDBClientBuilder.defaultClient().deleteItem(tableName, Map.of("id", new AttributeValue(key)));
            return true;
        } catch (ResourceNotFoundException e) {
            log.error("Table not found", e);
        } catch (AmazonServiceException e) {
            log.error("Amazon service exception", e);
        }
        return false;
    }

    @Override
    public Optional<Map<String, String>> getEntry(String serverId, String key) {
        try {
            var result = AmazonDynamoDBClientBuilder.defaultClient().getItem(buildTableName(serverId), Map.of("id", new AttributeValue(key))).getItem();
            Map<String, String> items = new HashMap<>();
            for (var entry : result.entrySet()) {
                items.put(entry.getKey(), entry.getValue().toString());
            }
            return Optional.of(items);
        } catch (ResourceNotFoundException e) {
            log.error("Table not found", e);
        } catch (AmazonServiceException e) {
            log.error("Amazon service exception", e);
        }
        return Optional.empty();
    }
}
