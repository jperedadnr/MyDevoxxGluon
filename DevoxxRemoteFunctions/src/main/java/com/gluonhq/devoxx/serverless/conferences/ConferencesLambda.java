package com.gluonhq.devoxx.serverless.conferences;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import java.io.*;

abstract class ConferencesLambda implements RequestStreamHandler {

    abstract String getCfpEndpoint();

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        String jsonOutput = new ConferencesRetriever().retrieve(getCfpEndpoint());
        try (Writer writer = new OutputStreamWriter(output)) {
            writer.write(jsonOutput);
        }
    }
}
