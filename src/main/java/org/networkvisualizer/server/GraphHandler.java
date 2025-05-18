package org.networkvisualizer.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.networkvisualizer.network.IntersectionGraph;
import org.opencv.core.Point;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.networkvisualizer.server.HandlerUtil.respond;

class GraphHandler implements HttpHandler {
    private final IntersectionGraph graph;

    public GraphHandler(IntersectionGraph graph) {
        this.graph = graph;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response;
        try {
            response = encodeGraph();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            respond(exchange, 200,  response);
        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = "{\"error\": \"Internal Server Error\"}";
            respond(exchange, 500, errorMessage);
        }


    }

    /**
     * Encodes the intersection graph as a list of JSON objects.
     * Each object contains:
     * - "path": list of [x, y] points
     * - "edges": list of integers (edge indices)
     */
    private String encodeGraph() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode resultArray = mapper.createArrayNode();

        for (List<Point> path : graph.paths) {
            Set<Integer> edges = graph.pathToEdges.get(path);
            ObjectNode pathObject = mapper.createObjectNode();

            // Build "path": [[x1, y1], [x2, y2], ...]
            ArrayNode pathArray = mapper.createArrayNode();
            for (Point p : path) {
                ArrayNode pointArray = mapper.createArrayNode();
                pointArray.add(p.x);
                pointArray.add(p.y);
                pathArray.add(pointArray);
            }
            pathObject.set("path", pathArray);

            // Build "edges": [1, 2, 3]
            ArrayNode edgesArray = mapper.createArrayNode();
            for (Integer edge : edges) {
                edgesArray.add(edge);
            }
            pathObject.set("edges", edgesArray);

            resultArray.add(pathObject);
        }

        return resultArray.toString();
    }
}
