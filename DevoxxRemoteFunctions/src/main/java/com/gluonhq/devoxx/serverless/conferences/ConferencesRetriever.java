package com.gluonhq.devoxx.serverless.conferences;

import com.gluonhq.devoxx.serverless.util.ConferenceUtil;

import javax.json.*;
import javax.json.stream.JsonCollectors;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.StringReader;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

class ConferencesRetriever {

    private static final Logger LOGGER = Logger.getLogger(ConferencesRetriever.class.getName());

    private static final Client client = ClientBuilder.newClient();

    public String retrieve(String cfpEndpoint, String ... paths) {

        WebTarget target = client.target(cfpEndpoint);
        Arrays.asList(paths).forEach(target::path);
        Response conferences = target.request().get();
        if (conferences.getStatus() == Response.Status.OK.getStatusCode()) {
            try (JsonReader conferencesReader = Json.createReader(new StringReader(conferences.readEntity(String.class)))) {
                return conferencesReader.readArray().stream()
                        .map(conf -> (JsonObject) conf)
                        .map(ConferenceUtil::makeBackwardCompatible)
                        .collect(JsonCollectors.toJsonArray()).toString();
            }
        } else {
            LOGGER.log(Level.WARNING, "Retrieval of conferences failed with", conferences.getStatus());
        }
        return Json.createArrayBuilder().build().toString();
    }
}