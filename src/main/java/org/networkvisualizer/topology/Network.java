package org.networkvisualizer.topology;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class Network {

    public Map<String, Node> nodes;
    public List<Edge> edges;

    public Network(List<Node> nodes, List<Edge> edges) {
        this.edges = edges;
        this.nodes = new HashMap<>();
        for (var node : nodes) {
            this.nodes.put(node.name, node);
        }
    }

    public Network(Map<String, Node> nodes, List<Edge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public String toString() {
        // Example: print parsed data
        StringBuilder str = new StringBuilder();
        str.append("Nodes:\n");
        for (var node : nodes.values()) {
            str.append(node).append("\n");
        }

        str.append("Edges:\n");
        for (var edge :edges) {
            str.append(edge).append("\n");
        }
        return str.toString();
    }

    public record Node(String name, double lon, double lat) {

        public String toString() {
            return name + " (" + lon + ", " + lat + ")";
        }
    }

    public record Edge(String from, String to, String mode) {

        public String toString() {
            return from + " -> " + to + " via " + mode;
        }
    }


}
