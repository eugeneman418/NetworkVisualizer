package org.networkvisualizer.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.networkvisualizer.network.Network;
import org.networkvisualizer.network.Timeline;
import org.opencv.core.Point;
import org.opencv.dnn.Net;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Time;
import java.util.*;

import static org.networkvisualizer.server.HandlerUtil.parseQueryParams;
import static org.networkvisualizer.server.HandlerUtil.respond;

/**
 * responds with the intensity on all links, requires time and modes in url param
 * time=1.5&modes=TRUCK,BARGE
 */
public class IntensityHandler implements HttpHandler {
    private final Network network;
    private final Timeline timeline;

    public IntensityHandler(Network network, Timeline timeline) {
        this.network = network;
        this.timeline = timeline;
    }


    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");

        try {
            Map<String, List<String>> queryParams = parseQueryParams(exchange.getRequestURI());

            // Check that "time" is provided
            if (!queryParams.containsKey("time")) {
                throw new IllegalArgumentException("Missing required parameter: time");
            }

            // Check that "modes" is provided
            if (!queryParams.containsKey("modes")) {
                throw new IllegalArgumentException("Missing required parameter: modes");
            }

            // Parse and validate time
            double time;
            try {
                time = Double.parseDouble(queryParams.get("time").get(0));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid value for time: must be a number");
            }


            // Parse and validate modes
            Set<String> modes = new HashSet<>();
            String[] modeArray = queryParams.get("modes").get(0).split(",");
            for (String mode : modeArray) {
                if (!Network.isValidMode(mode)) {
                    throw new IllegalArgumentException("Invalid mode: " + mode);
                }
                modes.add(mode);
            }

            // Normal response
            String response = encodeIntensities(time, modes);
            respond(exchange, 200, response);

        } catch (IllegalArgumentException e) {
            // Bad Request: 400
            String errorMessage = "{\"error\": \"" + e.getMessage() + "\"}";
            respond(exchange, 400, errorMessage);

        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = "{\"error\": \"Internal Server Error\"}";
            respond(exchange, 500, errorMessage);
        }

    }

    /**
     * encodes intensity on each path as json
     */
    private String encodeIntensities(double time, Set<String> modes) {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode intensities = mapper.createArrayNode();
        for (List<Point> path: network.intersectionGraph.paths) {
            int intensity = timeline.getEvents(time, path, modes).stream().mapToInt(Timeline.Event::quantity).sum();
            intensities.add(intensity);
        }
        return intensities.toString();
    }
}
