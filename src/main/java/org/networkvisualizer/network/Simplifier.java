package org.networkvisualizer.network;

import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.*;

public class Simplifier {
    static {
        nu.pattern.OpenCV.loadLocally() ;
    }

    public static List<Point> simplifyPolyline(List<Point> points, Map<Point, Boolean> fixedMap) {
        if (points == null || points.size() < 3) return points;

        List<Point> result = new ArrayList<>();

        List<List<Point>> segments = toSegments(points, fixedMap);
        for (var segment: segments) {
            if (segment.size() < 3) {
                result.addAll(segment);
            }
            else if (fixedMap.getOrDefault(segment.get(0), false)) {
                result.addAll(segment);
            }
            else {
                result.addAll(simplifySegment(segment));
            }
        }

        return result;
    }


    /**
     * split points into segments each points in each segment are either all true or all false
     * according to fixedMap
     */
    public static List<List<Point>> toSegments(List<Point> points, Map<Point, Boolean> fixedMap) {
        List<List<Point>> segments = new ArrayList<>();
        if (points == null || points.isEmpty()) return segments;

        List<Point> currentSegment = new ArrayList<>();
        Boolean currentFlag = fixedMap.getOrDefault(points.get(0), false);

        for (Point p : points) {
            Boolean flag = fixedMap.getOrDefault(p, false);
            if (flag.equals(currentFlag)) {
                currentSegment.add(p);
            } else {
                segments.add(new ArrayList<>(currentSegment));
                currentSegment.clear();
                currentSegment.add(p);
                currentFlag = flag;
            }
        }

        if (!currentSegment.isEmpty()) {
            segments.add(currentSegment);
        }

        return segments;
    }


    private static List<Point> simplifySegment(List<Point> segment) {
        MatOfPoint2f inputCurve = new MatOfPoint2f();
        inputCurve.fromList(segment);
        MatOfPoint2f outputCurve = new MatOfPoint2f();
        double epsilon = 100*Imgproc.arcLength(inputCurve,false); // distance between old and new pathToEdges is at most 50% path length
        Imgproc.approxPolyDP(inputCurve, outputCurve, epsilon, false);

//        System.out.println("Input segment length: " + segment.size());
//        System.out.println("Output segment length: " + outputCurve.toList().size());
        return new ArrayList<>(outputCurve.toList());
    }

    public static List<List<Point>> simplifyNetwork(List<PointList> ghRoutes) {
        // simplify network while preserving intersections
        Map<Point, Boolean> fixedMap = new HashMap<>();
        List<List<Point>> routes = new ArrayList<>();
        for (PointList ghRoute : ghRoutes) {
            List<Point> route = new ArrayList<>();
            for (GHPoint ghPoint : ghRoute) {
                Point p = new Point(ghPoint.getLat(), ghPoint.getLon());
                if (fixedMap.containsKey(p))
                    fixedMap.put(p, true); // overlapping fixedMap are true
                else fixedMap.put(p, false);
                route.add(p);
            }
            routes.add(route);
        }


        var simplifiedRoutes = routes.stream().map(r -> simplifyPolyline(r, fixedMap)).toList();
//        System.out.println("Num points on input routes: " + String.valueOf(routes.stream().mapToInt(List::size).sum()));
//        System.out.println("Num points on output routes: " + String.valueOf(simplifiedRoutes.stream().mapToInt(List::size).sum()));
        return simplifiedRoutes;
    }




}
