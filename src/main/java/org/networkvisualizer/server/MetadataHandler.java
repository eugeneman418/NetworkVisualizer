package org.networkvisualizer.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.networkvisualizer.network.Timeline;

import java.io.IOException;

import static org.networkvisualizer.server.HandlerUtil.respond;

/**
 * {
 *     time_steps: [float]
 *     max_intensities: {string (mode): int}
 * }
 */
public class MetadataHandler implements HttpHandler {
    private final Timeline timeline;
    public MetadataHandler(Timeline timeline) {
        this.timeline = timeline;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response = encodeMetadata();
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        respond(exchange, 200, response);
    }

    private String encodeMetadata() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode res = mapper.createObjectNode();
        ArrayNode timeSteps = mapper.createArrayNode();
        ObjectNode maxIntensities = mapper.createObjectNode();

        for (double time : timeline.getTimesteps()) {
            timeSteps.add(time);
        }

        res.put("time_steps", timeSteps);

        for (String mode : timeline.maxIntensityByMode.keySet()) {
            int intensity = timeline.maxIntensityByMode.get(mode);
            maxIntensities.put(mode, intensity);
        }
        res.put("max_intensities", maxIntensities);
        return res.toString();
    }
}
