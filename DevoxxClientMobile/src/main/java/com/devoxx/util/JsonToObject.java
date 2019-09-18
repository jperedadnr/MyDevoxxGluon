package com.devoxx.util;

import com.devoxx.model.Speaker;

import javax.json.JsonObject;
import java.util.List;
import java.util.stream.Collectors;

public class JsonToObject {
    
    public static List<Speaker> toSpeakers(List<JsonObject> source) {
        return source.stream()
                .map(s -> toSpeaker(s))
                .collect(Collectors.toList());
    }
    
    public static Speaker toSpeaker(JsonObject source) {
        Speaker speaker = new Speaker();
        speaker.setUuid(String.valueOf(source.getInt("id")));
        speaker.setBio(getNullable(source,"bio"));
        speaker.setFirstName(getNullable(source,"firstName"));
        speaker.setLastName(getNullable(source,"lastName"));
        speaker.setAvatarURL(getNullable(source,"imageUrl"));
        speaker.setCompany(getNullable(source,"company"));
        speaker.setTwitter(getNullable(source, "twitterHandle"));
        return speaker;
    }

    private static String getNullable(JsonObject source, String key) {
        return source.isNull(key) ? "" : source.getString(key);
    }
}
