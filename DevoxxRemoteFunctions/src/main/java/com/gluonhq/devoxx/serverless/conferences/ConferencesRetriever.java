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
package com.gluonhq.devoxx.serverless.conferences;

import com.gluonhq.devoxx.serverless.util.ConferenceUtil;

import javax.json.*;
import javax.json.stream.JsonCollectors;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

class ConferencesRetriever {

    private static final Logger LOGGER = Logger.getLogger(ConferencesRetriever.class.getName());

    private static final Client client = ClientBuilder.newClient();

    public String retrieve(String cfpEndpoint, String ... paths) {

        WebTarget target = client.target(cfpEndpoint);
        for (String s : paths) { target = target.path(s); }
        Response conferences = target.request().get();
        if (conferences.getStatus() == Response.Status.OK.getStatusCode()) {
            try (JsonReader conferencesReader = Json.createReader(new StringReader(conferences.readEntity(String.class)))) {
                return conferencesReader.readArray().stream()
                        .map(conf -> (JsonObject) conf)
                        .map(ConferenceUtil::createCleanResponseForClientFromOldEndpoint)
                        .collect(JsonCollectors.toJsonArray()).toString();
            }
        } else {
            LOGGER.log(Level.WARNING, "Retrieval of conferences failed with", conferences.getStatus());
        }
        return Json.createArrayBuilder().build().toString();
    }
}