package org.networkvisualizer.topology;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileReader;
import java.util.*;

public class NetworkParser {



    public static Network parseJson(String filePath) throws Exception {
        JSONObject root = new JSONObject(new JSONTokener(new FileReader(filePath)));

        // Valid transport modes
        Set<String> validModes = Set.of("truck", "train", "barge");

        // Parse nodes
        JSONArray nodeArray = root.getJSONArray("nodes");
        Map<String, Network.Node> nodeMap = new HashMap<>();

        for (int i = 0; i < nodeArray.length(); i++) {
            JSONObject nodeObj = nodeArray.getJSONObject(i);
            String name = nodeObj.getString("name");
            double lon = nodeObj.getDouble("lon");
            double lat = nodeObj.getDouble("lat");
            nodeMap.put(name, new Network.Node(name, lon, lat));
        }

        // Parse edges
        JSONArray edgeArray = root.getJSONArray("edges");
        List<Network.Edge> edges = new ArrayList<>();

        for (int i = 0; i < edgeArray.length(); i++) {
            JSONObject edgeObj = edgeArray.getJSONObject(i);
            String from = edgeObj.getString("from");
            String to = edgeObj.getString("to");
            String mode = edgeObj.getString("mode");

            if (!nodeMap.containsKey(from)) {
                throw new IllegalArgumentException("Unknown 'from' node: " + from);
            }
            if (!nodeMap.containsKey(to)) {
                throw new IllegalArgumentException("Unknown 'to' node: " + to);
            }
            if (!validModes.contains(mode)) {
                throw new IllegalArgumentException("Invalid mode: " + mode);
            }

            edges.add(new Network.Edge(from, to, mode));
        }

        return new Network(nodeMap, edges);
    }

    public static void main(String[] args) throws Exception {
        System.out.print(parseJson("resources/network.json")); // Replace with your actual file path
    }
}
