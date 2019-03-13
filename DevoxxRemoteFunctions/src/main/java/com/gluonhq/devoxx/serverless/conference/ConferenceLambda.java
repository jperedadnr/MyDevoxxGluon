package com.gluonhq.devoxx.serverless.conference;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class ConferenceLambda implements RequestStreamHandler {

    private static final String CFP_ENDPOINT = "https://www.devoxxians.com/api/public/events/";

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        try (JsonReader reader = Json.createReader(input)) {
            JsonObject jsonInput = reader.readObject();
            String id = jsonInput.getString("id");
            String jsonOutput = new ConferenceRetriever().retrieve(CFP_ENDPOINT, id);
            try (Writer writer = new OutputStreamWriter(output)) {
                writer.write(jsonOutput);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        InputStream input = new ByteArrayInputStream("{\"id\":\"46\"}".getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new ConferenceLambda().handleRequest(input, output, null);
        System.out.println("output = " + new String(output.toByteArray(), StandardCharsets.UTF_8));
    }
}