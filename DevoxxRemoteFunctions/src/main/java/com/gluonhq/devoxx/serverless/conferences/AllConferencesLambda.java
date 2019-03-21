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

import com.amazonaws.services.lambda.runtime.Context;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class AllConferencesLambda implements ConferencesLambda {

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        String time = null;
        if (input != null) {
            try (JsonReader reader = Json.createReader(input)) {
                JsonObject jsonInput = reader.readObject();
                time = jsonInput.containsKey("time") ? jsonInput.getString("time") : null;

            }
        }
        if (time == null || time.isEmpty()) {
            time = "upcoming";
        }
        String jsonOutput = new ConferencesRetriever().retrieve(getCfpEndpoint(), time);
        try (Writer writer = new OutputStreamWriter(output)) {
            writer.write(jsonOutput);
        }
    }

    @Override
    public String getCfpEndpoint() {
        return "https://www.devoxxians.com/api/public/events";
    }

    public static void main(String[] args) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        // new AllConferencesLambda().handleRequest(new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8)), output, null);
        // new AllConferencesLambda().handleRequest(new ByteArrayInputStream("{\"time\":\"upcoming\"}".getBytes(StandardCharsets.UTF_8)), output, null);
        new AllConferencesLambda().handleRequest(new ByteArrayInputStream("{\"time\":\"past\"}".getBytes(StandardCharsets.UTF_8)), output, null);
        System.out.println("output = " + new String(output.toByteArray(), StandardCharsets.UTF_8));
    }
}