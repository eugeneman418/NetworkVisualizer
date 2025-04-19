package org.networkvisualizer;

import org.networkvisualizer.routing.Router;

public class InstanceLoader {
    public final Network network;
    public final Timeline timeline;

    public final Router router;

    public InstanceLoader() throws Exception {
        network = NetworkParser.parseJson("resources/test/network.json");
        timeline = Timeline.initializeFromCsv("resources/test/timeline.csv", network, true);
        router = new Router("resources/test/zh.osm.pbf");

    }

}
