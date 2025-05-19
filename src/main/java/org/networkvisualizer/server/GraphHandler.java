package org.networkvisualizer.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.networkvisualizer.network.IntersectionGraph;
import org.networkvisualizer.network.Network;
import org.opencv.core.Point;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.networkvisualizer.server.HandlerUtil.respond;

class GraphHandler implements HttpHandler {
    private final Network network;

    public GraphHandler(Network network) {
        this.network = network;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
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
     * {
     *     nodes: [{name: string, lat: double, lon: double]
     *     links: [link (see below)]
     * }
     *
     * Each link object contains:
     * - "path": list of [x, y] points
     * - "edges": list of integers (edge indices)
     */
    private String encodeGraph() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode res = mapper.createObjectNode();
        ArrayNode nodesArray = mapper.createArrayNode();
        for (Network.Vertex vertex : network.nodes.values()) {
            ObjectNode node = mapper.createObjectNode();
            node.put("name", vertex.name());
            node.put("lat", vertex.lat());
            node.put("lon", vertex.lon());
            nodesArray.add(node);
        }

        res.put("nodes", nodesArray);
        ArrayNode linksArray = mapper.createArrayNode();

        for (List<Point> path : network.intersectionGraph.paths) {
            Set<Integer> edges = network.intersectionGraph.pathToEdges.get(path);
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

            linksArray.add(pathObject);
        }
        res.put("links", linksArray);
        return res.toString();
    }
}
