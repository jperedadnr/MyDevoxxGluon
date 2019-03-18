package com.gluonhq.devoxx.serverless.conference;

import com.gluonhq.devoxx.serverless.util.ConferenceUtil;

import javax.json.Json;
import javax.json.JsonReader;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConferenceRetriever {

    private static final Logger LOGGER = Logger.getLogger(ConferenceRetriever.class.getName());

    private static final Client client = ClientBuilder.newClient();

    public String retrieve(String cfpEndpoint, String id) {

        Response conferences = client.target(cfpEndpoint).path(id).request().get();
        if (conferences.getStatus() == Response.Status.OK.getStatusCode()) {
            try (JsonReader conferenceReader = Json.createReader(new StringReader(conferences.readEntity(String.class)))) {
                return ConferenceUtil.makeBackwardCompatible(conferenceReader.readObject()).toString();
            }
        } else {
            LOGGER.log(Level.WARNING, "Retrieval of conferences failed with", conferences.getStatus());
        }
        return Json.createArrayBuilder().build().toString();
    }
}