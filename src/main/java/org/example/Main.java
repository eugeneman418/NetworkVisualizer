package org.example;

import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import org.example.routing.Router;
import org.opencv.core.Point;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        Router router = new Router("netherlands-latest.osm.pbf");
        ResponsePath responsePath1 = router.routeCar(51.9243, 4.4700, 51.9910, 4.3649);
        ResponsePath responsePath2 = router.routeCar(51.9243, 4.4700, 51.9910, 4.3660);
        List<List<Point>> simplified = Simplifier.simplifyNetwork(List.of(responsePath1.getWaypoints(), responsePath2.getWaypoints()));
    }
}