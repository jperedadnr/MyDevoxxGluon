package com.gluonhq.devoxx.serverless.util;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class ConferenceUtil {

    public static JsonObject makeBackwardCompatible(JsonObject conf) {
        if (conf.containsKey("fromDate")) {
            String fromDate = conf.getString("fromDate");
            conf = enrich(conf, "fromDate", "fromDate", fromDate.substring(0, 10));
        }
        if (conf.containsKey("toDate")) {
            String toDate = conf.getString("toDate");
            conf = enrich(conf, "toDate", "endDate", toDate.substring(0, 10));
        }
        if (conf.containsKey("apiURL")) {
            String apiURL = conf.getString("apiURL", "");
            conf = enrich(conf, "apiURL", "cfpURL", apiURL);
        }
        if (conf.containsKey("eventCategory")) {
            String eventCategory = conf.getString("eventCategory", "");
            conf = enrich(conf, "eventCategory", "eventType", eventCategory);
        }
        if (conf.containsKey("id")) {
            String id = String.valueOf(conf.getInt("id"));
            conf = enrich(conf, null, "cfpVersion", id);
        }
        return conf;
    }

    private static JsonObject enrich(JsonObject source, String sourceKey, String key, String value) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        source.entrySet().stream().filter(entry -> !entry.getKey().equals(sourceKey)).forEach(entry -> builder.add(entry.getKey(), entry.getValue()));
        builder.add(key, value);
        return builder.build();
    }
}