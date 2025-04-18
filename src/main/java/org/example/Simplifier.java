package org.example;

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
        System.out.print("input size: ");
        System.out.println(points.size());

        if (points == null || points.size() < 3) return points;

        List<Point> result = new ArrayList<>();

        List<List<Point>> segments = toSegments(points, fixedMap);
        for (var segment: segments) {
            if (segment.size() < 3) {
                result.addAll(segment);
            }
            else if (fixedMap.getOrDefault(segment.getFirst(), false)) {
                result.addAll(segment);
            }
            else {
                result.addAll(simplifySegment(segment));
            }
        }

        System.out.print("output size: ");
        System.out.println(result.size());
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
        Boolean currentFlag = fixedMap.getOrDefault(points.getFirst(), false);

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
        double epsilon = 0.1*Imgproc.arcLength(inputCurve,false); // distance between old and new paths is at most 1% path length
        Imgproc.approxPolyDP(inputCurve, outputCurve, epsilon, false);

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


        return routes.stream().map(r -> simplifyPolyline(r, fixedMap)).toList();
    }
}
