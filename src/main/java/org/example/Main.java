package org.example;

import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import org.example.routing.Router;
import org.opencv.core.Point;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        String osmPath = args[0];

        Router router = new Router(osmPath);
        ResponsePath responsePath1 = router.routeCar(51.9243, 4.4700, 52, 4.3649);
        ResponsePath responsePath2 = router.routeCar( 51.9243, 4.4700, 52, 4.3660);
        List<List<Point>> simplified = Simplifier.simplifyNetwork(List.of(responsePath1.getPoints(), responsePath2.getPoints()));

    }
}