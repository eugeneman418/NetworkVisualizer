package org.networkvisualizer.server;

import com.sun.net.httpserver.HttpServer;
import org.networkvisualizer.network.Network;
import org.networkvisualizer.network.NetworkParser;
import org.networkvisualizer.network.Timeline;
import org.networkvisualizer.routing.Router;

import java.net.InetSocketAddress;

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
        server.createContext("/graph", new GraphHandler(network.intersectionGraph));
        server.createContext("/metadata", new MetadataHandler(timeline));
        server.createContext("/intensity", new IntensityHandler(network, timeline));
        server.createContext("/link", new LinkHandler(network, timeline));


        server.start();
        System.out.println("Server started at http://localhost:"+port+"/");
    }

    public VisualizationServer(String osmPath, String networkPath, String timelinePath) throws Exception {
        this(osmPath, networkPath, timelinePath, 8000);
    }


}

