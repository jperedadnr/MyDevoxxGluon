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
package com.gluonhq.devoxx.serverless.conference;

import javax.json.Json;
import javax.json.JsonReader;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.gluonhq.devoxx.serverless.util.ConferenceUtil.*;

public class ConferenceRetriever {

    private static final Logger LOGGER = Logger.getLogger(ConferenceRetriever.class.getName());

    private static final Client client = ClientBuilder.newClient();

    public String retrieve(String cfpEndpoint, String id) {

        WebTarget target = client.target(cfpEndpoint);
        if (isNewCfpURL(cfpEndpoint)) {
            target = target.path("public").path("event");
        } else {
            target = target.path(id);
        }
        Response conferences = target.request().get();
        if (conferences.getStatus() == Response.Status.OK.getStatusCode()) {
            try (JsonReader conferenceReader = Json.createReader(new StringReader(conferences.readEntity(String.class)))) {
                if (isNewCfpURL(cfpEndpoint)) {
                    return createCleanResponseForClientFromNewEndpoint(conferenceReader.readObject()).toString();
                } else {
                    return createCleanResponseForClientFromOldEndpoint(conferenceReader.readObject()).toString();
                }
            }
        } else {
            LOGGER.log(Level.WARNING, "Retrieval of conferences failed with", conferences.getStatus());
        }
        return Json.createArrayBuilder().build().toString();
    }
}