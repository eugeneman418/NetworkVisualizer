package org.networkvisualizer.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.networkvisualizer.network.Network;
import org.networkvisualizer.network.Timeline;
import org.opencv.core.Point;

import java.io.IOException;
import java.util.*;

import static org.networkvisualizer.server.HandlerUtil.parseQueryParams;
import static org.networkvisualizer.server.HandlerUtil.respond;

/**
 * Responds with list of events at a given time on a given path on intersection graph (index given in url param) for some modes
 * time=1.5&path=3&modes=TRUCK,BARGE
 */
public class LinkHandler implements HttpHandler {
    private final Network network;
    private final Timeline timeline;

    public LinkHandler(Network network, Timeline timeline) {
        this.network = network;
        this.timeline = timeline;
    }


    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
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

            // Check that "path" is provided
            if (!queryParams.containsKey("path")) {
                throw new IllegalArgumentException("Missing required parameter: path");
            }

            // Parse and validate time
            int timeIdx;
            try {
                timeIdx = Integer.parseInt(queryParams.get("time").get(0));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid value for time: must be a number");
            }
            double time;
            try {
                time = timeline.getTimesteps().get(timeIdx);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid index for time: " + timeIdx);
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

            int pathIdx;
            try {
                pathIdx = Integer.parseInt(queryParams.get("path").get(0));
                if (pathIdx < 0 || pathIdx >= network.intersectionGraph.paths.size())
                    throw new NumberFormatException("invalid index");
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid value for path index: must be an integer");
            }

            String response = encodeEvents(time, network.intersectionGraph.paths.get(pathIdx), modes);
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
     * [
     *  {"from": String,
     *  "to": String,
     *  "category": String,
     *  "quantity": int
     *  },
     *  ...]
     */
    private String encodeEvents(double time, List<Point> path, Set<String> modes) {
        Set<Timeline.Event> events =  timeline.getEvents(time, path, modes);
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode jsonEvents = mapper.createArrayNode();
        for (Timeline.Event event : events){
            ObjectNode jsonEvent = mapper.createObjectNode();
            jsonEvent.put("from", event.edge().from());
            jsonEvent.put("to", event.edge().to());
            jsonEvent.put("category", event.category());
            jsonEvent.put("quantity", event.quantity());
            jsonEvents.add(jsonEvent);
        }
        return jsonEvents.toString();
    }
}
