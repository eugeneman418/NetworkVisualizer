package org.networkvisualizer.server;

import com.sun.net.httpserver.HttpServer;
import org.networkvisualizer.network.Network;
import org.networkvisualizer.network.NetworkParser;
import org.networkvisualizer.network.Timeline;
import org.networkvisualizer.routing.Router;
import org.opencv.core.Point;

import java.net.InetSocketAddress;
import java.util.List;

public class VisualizationServer {

    Network network;
    Timeline timeline;

    HttpServer server;
    public int port;
    public VisualizationServer(String osmPath, String networkPath, String timelinePath, int port) throws Exception {
        // osmPath: path to osm.pbf file
        // networkPath: path to network json file
        // timelinePath: path to timeline csv file
        Router router = new Router(osmPath);
        network = NetworkParser.parseJson(networkPath);
        network.calculateRoutes(router);
        timeline = Timeline.initializeFromCsv(timelinePath, network, true);
        this.port = port;

        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/graph", new GraphHandler(network));
        server.createContext("/metadata", new MetadataHandler(timeline));
        server.createContext("/intensity", new IntensityHandler(network, timeline));
        server.createContext("/link", new LinkHandler(network, timeline));
        server.createContext("/node", new NodeHandler(network, timeline));

//        for (int i = 0; i < 1; i++) {
//            List<Point> path = network.intersectionGraph.paths.get(i);
//            System.out.print(i + ": ");
//            for (int edgeIdx : network.intersectionGraph.pathToEdges.get(path)) {
//                Network.Edge edge = network.edges.get(edgeIdx);
//                System.out.print(edge.toString() +", ");
//            }
//            System.out.println("");
//            for (int edgeIdx : network.intersectionGraph.pathToEdges.get(path)) {
//                System.out.println(network.debugRoutes.get(edgeIdx));
//            }
//
//        }

        server.start();
        System.out.println("Server started at http://localhost:"+port+"/");
    }

    public VisualizationServer(String osmPath, String networkPath, String timelinePath) throws Exception {
        this(osmPath, networkPath, timelinePath, 8000);
    }


}

