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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.networkvisualizer.server.HandlerUtil.parseQueryParams;
import static org.networkvisualizer.server.HandlerUtil.respond;

public class NodeHandler implements HttpHandler {
    private final Network network;
    private final Timeline timeline;

    public NodeHandler(Network network, Timeline timeline) {
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
            if (!queryParams.containsKey("node")) {
                throw new IllegalArgumentException("Missing required parameter: path");
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

            String nodeName = queryParams.get("node").get(0);
            if (!network.nodes.containsKey(nodeName))
                throw new IllegalArgumentException("Invalid node: " + nodeName);

            String response = encodeEvents(time, network.nodes.get(nodeName), modes);
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
    private String encodeEvents(double time, Network.Vertex node, Set<String> modes) {
        Set<Timeline.Event> events =  timeline.getEvents(time, node, modes);
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
