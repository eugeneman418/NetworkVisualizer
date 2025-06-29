package org.networkvisualizer.network;

import com.graphhopper.ResponsePath;
import com.graphhopper.util.PointList;
import org.networkvisualizer.routing.Router;

import java.util.*;


public class Network {
    public static final String TRUCK = "truck";
    public static final String TRAIN = "train";
    public static final String BARGE = "barge";

    public static final Set<String> validModes = Set.of(TRUCK, TRAIN, BARGE);



    public Map<String, Vertex> nodes;

    public List<Edge> edges;
    // list of routes matching the list of edges

    public List<Double> distances;

    public IntersectionGraph intersectionGraph;


    public Network(List<Vertex> vertices, List<Edge> edges) {
        this.edges = edges;
        this.nodes = new HashMap<>();
        for (var node : vertices) {
            this.nodes.put(node.name, node);
        }
    }

    public Network(Map<String, Vertex> nodes, List<Edge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public static boolean isValidMode(String mode) {
        return validModes.contains(mode);
    }




    /**
     * Compute list of simplified routes corresponding to the edge list
     */
    public void calculateRoutes(Router router) throws Exception {
        distances = new ArrayList<>(edges.size());
        List<PointList> ghRoutes = new ArrayList<>(edges.size());

        for (var edge: edges) {
            Vertex from = nodes.get(edge.from());
            Vertex to = nodes.get(edge.to());

            ResponsePath route = switch (edge.mode()) {
                case Network.TRUCK -> router.routeTruck(from.lat(), from.lon(), to.lat(), to.lon());
                case Network.TRAIN -> router.routeTrain(from.lat(), from.lon(), to.lat(), to.lon());
                case Network.BARGE -> router.routeBarge(from.lat(), from.lon(), to.lat(), to.lon());
                default -> {
                    if (Network.isValidMode(edge.mode())) {
                        throw new Exception("router not implemented for mode: " + edge.mode());
                    }
                    else
                        throw new Exception("invalid transportation mode: "+ edge.mode());
                }
            };
            distances.add(route.getDistance() / 1000); // OSM distance is in meters by default for some reason
            ghRoutes.add(route.getPoints());

        }

        intersectionGraph = IntersectionGraph.fromGhRoutes(ghRoutes);



    }

    public String toString() {
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

    public record Vertex(String name, double lon, double lat) {

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
