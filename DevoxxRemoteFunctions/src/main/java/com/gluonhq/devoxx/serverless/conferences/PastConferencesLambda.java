package com.gluonhq.devoxx.serverless.conferences;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class PastConferencesLambda extends ConferencesLambda {

    @Override
    String getCfpEndpoint() {
        return "https://www.devoxxians.com/api/public/events/past";
    }

    public static void main(String[] args) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new PastConferencesLambda().handleRequest(null, output, null);
        System.out.println("output = " + new String(output.toByteArray(), StandardCharsets.UTF_8));
    }
}
