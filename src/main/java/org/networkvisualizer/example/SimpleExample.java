package org.networkvisualizer.example;

import org.networkvisualizer.server.VisualizationServer;

public class SimpleExample {

    public static void main(String[] args) {
        try {
            VisualizationServer server = new VisualizationServer("src/test/resources/zh.osm.pbf",
                    "src/test/resources/network.json",
                    "src/test/resources/timeline.csv"
            );
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
