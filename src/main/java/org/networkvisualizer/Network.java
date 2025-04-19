package org.networkvisualizer;

import com.graphhopper.ResponsePath;
import com.graphhopper.util.PointList;
import org.networkvisualizer.routing.Router;
import org.opencv.core.Point;

import java.util.*;


public class Network {

    public static final String TRUCK = "truck";
    public static final String TRAIN = "train";
    public static final String BARGE = "barge";

    public Map<String, Node> nodes;
    public List<Edge> edges;

    private List<Route> routes;

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

    public static boolean validMode(String mode) {
        return Set.of(TRUCK, TRAIN, BARGE).contains(mode);
    }

    public List<Route> getRoutes() throws Exception {
        if (routes == null) throw new Exception("routes not initialized, call calculateRoutes first");
        else return routes;
    }

    /**
     * Compute list of simplified routes corresponding to the edge list
     */
    public List<Route> calculateRoutes(Router router) throws Exception {
        List<Double> distances = new ArrayList<>(edges.size());
        List<PointList> ghRoutes = new ArrayList<>(edges.size());
        for (var edge: edges) {
            Network.Node from = nodes.get(edge.from());
            Network.Node to = nodes.get(edge.to());
            ResponsePath route = switch (edge.mode()) {
                case Network.TRUCK -> router.routeTruck(from.lat(), from.lon(), to.lat(), to.lon());
                case Network.TRAIN -> router.routeTrain(from.lat(), from.lon(), to.lat(), to.lon());
                case Network.BARGE -> router.routeBarge(from.lat(), from.lon(), to.lat(), to.lon());
                default -> {
                    if (Network.validMode(edge.mode())) {
                        throw new Exception("router not implemented for mode: " + edge.mode());
                    }
                    else
                        throw new Exception("invalid transportation mode: "+ edge.mode());
                }
            };
            distances.add(route.getDistance() / 1000); // OSM distance is in meters by default for some reason
            ghRoutes.add(route.getPoints());
        }

        List<List<Point>> simplifiedRoutes = Simplifier.simplifyNetwork(ghRoutes);
        assert edges.size() == distances.size() && edges.size() == simplifiedRoutes.size(): "Excepts one route per edge";

        routes = new ArrayList<>();
        for (int i = 0 ; i < edges.size(); i++) {
            routes.add(new Route(simplifiedRoutes.get(i), distances.get(i)));
        }
        return routes;
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

    public record Route(List<Point> path, double distance) {

    }






}
