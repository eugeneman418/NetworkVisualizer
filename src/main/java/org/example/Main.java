package org.example;

import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import org.example.routing.Router;

public class Main {

    public static void main(String[] args) {
        Router router = new Router("netherlands-latest.osm.pbf");
        ResponsePath responsePath = router.routeCar(51.9243, 4.4700, 51.9910, 4.3649);
        responsePath.getP
    }
}