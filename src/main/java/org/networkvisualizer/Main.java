package org.networkvisualizer;

import com.graphhopper.ResponsePath;
import org.networkvisualizer.routing.Router;
import org.opencv.core.Point;

import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        Router router = new Router("resources/test/zh.osm.pbf");
        Network network = NetworkParser.parseJson("resources/test/network.json");
        network.calculateRoutes(router);
        Timeline timeline = Timeline.initializeFromCsv("resources/test/timeline.csv", network, true);

        System.out.println(network.edges.get(10));
        System.out.println(network.getRoutes().get(10));

        for (var event: timeline.getEvents(21.4173,
                new Timeline.Link(
                        new Point(51.9161665, 4.4819428),
                        new Point(51.9156208, 4.4819686)
                )
            ))
            System.out.println(event);

    }
}