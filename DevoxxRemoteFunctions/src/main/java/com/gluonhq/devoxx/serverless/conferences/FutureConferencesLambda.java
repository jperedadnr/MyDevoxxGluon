package com.gluonhq.devoxx.serverless.conferences;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FutureConferencesLambda extends ConferencesLambda {

    @Override
    String getCfpEndpoint() {
        return "https://www.devoxxians.com/api/public/events/upcoming";
    }

    public static void main(String[] args) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new FutureConferencesLambda().handleRequest(null, output, null);
        System.out.println("output = " + new String(output.toByteArray(), StandardCharsets.UTF_8));
    }
}