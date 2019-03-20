/**
 * Copyright (c) 2019, Gluon Software
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation and/or other materials provided
 *    with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse
 *    or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.devoxx.views.helper;

import com.devoxx.filter.TimePeriod;
import com.gluonhq.connect.source.FileDataSource;
import com.gluonhq.impl.cloudlink.client.PrivateStorage;

import javax.json.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Filter {

    private static final String FILTER_FILE_NAME = "filter.json";
    private static final Logger LOGGER = Logger.getLogger(Filter.class.getName());

    private String conferenceId;
    private List<Integer> days = new ArrayList<>();
    private List<String> tracks = new ArrayList<>();
    private List<String> sessionTypes = new ArrayList<>();
    private TimePeriod timePeriod;

    public Filter(String conferenceId) {
        this.conferenceId = conferenceId;
    }

    public String getConferenceId() {
        return conferenceId;
    }

    public void setConferenceId(String conferenceId) {
        this.conferenceId = conferenceId;
    }

    public List<Integer> getDays() {
        return days;
    }

    public List<String> getTracks() {
        return tracks;
    }

    public List<String> getSessionTypes() {
        return sessionTypes;
    }

    public TimePeriod getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(TimePeriod timePeriod) {
        this.timePeriod = timePeriod;
    }

    /**
     * Adds the filter as an entry to the list of filters in the filter file.
     */
    public void save() {
        JsonArray filter;
        File filterFile = filterFile();
        if (filterFile != null) {
            if (filterFile.exists()) {
                filter = rebuildJson(filterFile);
            } else {
                filter = Json.createArrayBuilder().add(createJson()).build();
            }
            FileDataSource fileDataSource = new FileDataSource(filterFile);
            try (JsonWriter writer = Json.createWriter(fileDataSource.getOutputStream())) {
                writer.write(filter);
            } catch (IOException e) {
                LOGGER.log(Level.FINE, "Could not write to filter file.", e);
            }
        }
    }

    /**
     * Returns the {@link JsonObject} entry for the conferenceId from the filter file.
     * @param conferenceId The conferenceId to be loaded.
     */
    public static Filter load(String conferenceId) {
        File filterFile = filterFile();
        if (filterFile != null && filterFile.exists()) {
            FileDataSource fileDataSource = new FileDataSource(filterFile);
            try (JsonReader reader = Json.createReader(fileDataSource.getInputStream())) {
                for (JsonValue jsonValue : reader.readArray()) {
                    if(((JsonObject) jsonValue).getString("id").equals(conferenceId)) {
                        return createObject((JsonObject) jsonValue);
                    }
                }
            } catch (IOException e) {
                LOGGER.log(Level.FINE, "Could not read to filter file.", e);
            }
        }
        return null;
    }

    /**
     * Removes the json entry for the conferenceId from the filter file.
     * @param conferenceId The conferenceId to be removed.
     */
    public static void remove(String conferenceId) {
        File filterFile = filterFile();
        if (filterFile == null || !filterFile.exists()) return;
        FileDataSource fileDataSource = new FileDataSource(filterFile);

        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        try {
            JsonReader reader = Json.createReader(fileDataSource.getInputStream());
            for (JsonValue jsonValue : reader.readArray()) {
                if(!((JsonObject) jsonValue).getString("id").equals(conferenceId)) {
                    arrayBuilder.add(jsonValue);
                }
            }
            JsonWriter writer = Json.createWriter(fileDataSource.getOutputStream());
            writer.write(arrayBuilder.build());
        } catch (IOException e) {
            LOGGER.log(Level.FINE, "Could not delete json value from filter file.", e);
        }
    }

    private JsonArray rebuildJson(File filterFile) {
        FileDataSource fileDataSource = new FileDataSource(filterFile);
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        try (JsonReader reader = Json.createReader(fileDataSource.getInputStream())) {
            for (JsonValue jsonValue : reader.readArray()) {
                JsonObject jsonObject = (JsonObject) jsonValue;
                if (!jsonObject.getString("id").equals(getConferenceId())) {
                    arrayBuilder.add(jsonObject);
                }
            }
            arrayBuilder.add(createJson());
        } catch (IOException e) {
            LOGGER.log(Level.FINE, "Could not read to filter file.", e);
        }
        return arrayBuilder.build();
    }

    private static File filterFile() {
        File root = PrivateStorage.get();
        if (root != null) {
            return new File(root, FILTER_FILE_NAME);
        }
        return null;
    }

    private JsonObject createJson() {
        JsonObjectBuilder builder = Json.createObjectBuilder();

        builder.add("id", conferenceId);
        JsonArrayBuilder daysJsonArray = Json.createArrayBuilder();
        for (Integer day : days) {
            daysJsonArray.add(day);
        }
        builder.add("days", daysJsonArray);
        JsonArrayBuilder tracksJsonArray = Json.createArrayBuilder();
        for (String track : tracks) {
            tracksJsonArray.add(track);
        }
        builder.add("tracks", tracksJsonArray);
        JsonArrayBuilder sessionTypesJsonArray = Json.createArrayBuilder();
        for (String sessionType : sessionTypes) {
            sessionTypesJsonArray.add(sessionType);
        }
        builder.add("sessionTypes", sessionTypesJsonArray);
        builder.add("timePeriod", timePeriod.toString());

        return builder.build();
    }

    private static Filter createObject(JsonObject jsonObject) {
        Filter filter = new Filter(jsonObject.getString("id"));
        for (int i = 0; i < jsonObject.getJsonArray("days").size(); i++) {
            filter.getDays().add( jsonObject.getJsonArray("days").getInt(i));
        }
        for (int i = 0; i < jsonObject.getJsonArray("tracks").size(); i++) {
            filter.getTracks().add( jsonObject.getJsonArray("tracks").getString(i));
        }
        for (int i = 0; i < jsonObject.getJsonArray("sessionTypes").size(); i++) {
            filter.getSessionTypes().add( jsonObject.getJsonArray("sessionTypes").getString(i));
        }
        filter.setTimePeriod(TimePeriod.fromString(jsonObject.getString("timePeriod")));
        return filter;
    }
}
