package org.networkvisualizer.topology;

import com.graphhopper.ResponsePath;
import org.networkvisualizer.topology.routing.Router;
import org.opencv.core.Point;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        String osmPath = args[0];
        //String networkPath = args[1];


        Router router = new Router(osmPath);
        ResponsePath responsePath1 = router.routeTruck(51.9243, 4.4700, 52, 4.3649);
        ResponsePath responsePath2 = router.routeTruck( 51.9243, 4.4700, 52, 4.3660);
        List<List<Point>> simplified = Simplifier.simplifyNetwork(List.of(responsePath1.getPoints(), responsePath2.getPoints()));

    }
}