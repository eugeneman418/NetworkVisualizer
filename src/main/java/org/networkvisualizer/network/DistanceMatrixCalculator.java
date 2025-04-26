package org.networkvisualizer.network;

import com.opencsv.CSVWriter;
import org.networkvisualizer.routing.Router;


import java.io.FileWriter;


public class DistanceMatrixCalculator {
    public static void main(String[] args) throws Exception {
        String osmPath = args[0];
        String networkPath = args[1];
        String outputPath = args[2];

        try (CSVWriter writer = new CSVWriter(new FileWriter(outputPath))) {
            writer.writeNext(new String[]{"From", "To", "Mode", "Distance (km)"});

            Router router = new Router(osmPath);
            Network network = NetworkParser.parseJson(networkPath);
            network.calculateRoutes(router);
            for (int i = 0; i < network.edges.size(); i++) {
                Network.Edge edge = network.edges.get(i);
                double dist = network.getRoutes().get(i).distance();
                writer.writeNext(new String[]{edge.from(), edge.to(), edge.mode(), String.valueOf(dist)});
            }
        }


    }
}
