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

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.gluonhq.devoxx.serverless.util.ConferenceUtil;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class ConferenceLambda implements RequestStreamHandler {

    private static final String ID_OLD = "\"65\"";
    private static final String CFP_ENDPOINT_OLD = "https://www.devoxxians.com/api/public/events/";
    private static final String CFP_ENDPOINT_NEW = "https://vxdms2019.cfp.dev/api/";

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        String id, cfpEndpoint;

        try (JsonReader reader = Json.createReader(input)) {
            JsonObject jsonInput = reader.readObject();
            id = jsonInput.isNull("id") ? null : jsonInput.getString("id");
            cfpEndpoint = jsonInput.getString("cfpEndpoint");
            if (cfpEndpoint != null && !ConferenceUtil.isNewCfpURL(cfpEndpoint)) {
                cfpEndpoint = CFP_ENDPOINT_OLD;
            }
        }
        String jsonOutput = new ConferenceRetriever().retrieve(cfpEndpoint, id);
        try (Writer writer = new OutputStreamWriter(output)) {
            writer.write(jsonOutput);
        }
    }

    public static void main(String[] args) throws IOException {
        final String json = String.format("{\"id\": %s, \"cfpEndpoint\":\"%s\"}", ID_OLD, CFP_ENDPOINT_OLD); // Replace with null and CFP_ENDPOINT_NEW
        InputStream input = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new ConferenceLambda().handleRequest(input, output, null);
        System.out.println("output = " + new String(output.toByteArray(), StandardCharsets.UTF_8));
    }
}