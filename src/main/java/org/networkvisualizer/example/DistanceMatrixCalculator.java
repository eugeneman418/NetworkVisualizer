package org.networkvisualizer.example;

import com.opencsv.CSVWriter;
import org.networkvisualizer.network.Network;
import org.networkvisualizer.network.NetworkParser;
import org.networkvisualizer.routing.Router;


import java.io.FileWriter;


public class DistanceMatrixCalculator {
    public static void main(String[] args) throws Exception {
        String osmPath = "src/test/resources/zh.osm.pbf";
        String networkPath = "src/test/resources/network.json";
        String outputPath = "src/main/resources/distance_matrix.csv";

        try (CSVWriter writer = new CSVWriter(new FileWriter(outputPath))) {
            writer.writeNext(new String[]{"From", "To", "Mode", "Distance (km)"});

            Router router = new Router(osmPath);
            Network network = NetworkParser.parseJson(networkPath);

            network.calculateRoutes(router);

            for (int i = 0; i < network.edges.size(); i++) {
                Network.Edge edge = network.edges.get(i);
                double dist = network.distances.get(i);
                writer.writeNext(new String[]{edge.from(), edge.to(), edge.mode(), String.valueOf(dist)});
            }
        }


    }
}
