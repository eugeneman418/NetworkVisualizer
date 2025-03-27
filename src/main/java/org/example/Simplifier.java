package org.example;

import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.*;

public class Simplifier {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static List<Point> simplifyPolyline(List<Point> points, Map<Point, Boolean> preserveMap, double epsilon) {
        if (points == null || points.size() < 3) return points;

        List<List<Point>> segments = splitIntoSegments(points, preserveMap);
        List<Point> result = new ArrayList<>();

        for (List<Point> segment : segments) {
            if (segment.size() <= 2) {
                result.addAll(segment);
            } else {
                result.addAll(simplifySegment(segment, epsilon));
            }
        }
        return result;
    }

    private static List<List<Point>> splitIntoSegments(List<Point> points, Map<Point, Boolean> preserveMap) {
        List<List<Point>> segments = new ArrayList<>();
        List<Point> currentSegment = new ArrayList<>();

        for (Point p : points) {
            currentSegment.add(p);
            if (preserveMap.getOrDefault(p, false) && currentSegment.size() > 1) {
                segments.add(new ArrayList<>(currentSegment));
                currentSegment.clear();
                currentSegment.add(p);
            }
        }
        if (!currentSegment.isEmpty()) {
            segments.add(currentSegment);
        }
        return segments;
    }

    private static List<Point> simplifySegment(List<Point> segment, double epsilon) {
        MatOfPoint2f inputCurve = new MatOfPoint2f();
        inputCurve.fromList(segment);
        MatOfPoint2f outputCurve = new MatOfPoint2f();

        Imgproc.approxPolyDP(inputCurve, outputCurve, epsilon, false);
        return new ArrayList<>(outputCurve.toList());
    }

    public static List<List<Point>> simplifyNetwork(List<PointList> ghRoutes) {
        // simplify network while preserving intersections
        Map<Point, Boolean> points = new HashMap<>();
        List<List<Point>> routes = new ArrayList<>();
        for (PointList ghRoute : ghRoutes) {
            List<Point> route = new ArrayList<>();
            for (GHPoint ghPoint : ghRoute) {
                Point p = new Point(ghPoint.getLat(), ghPoint.getLon());
                if (points.containsKey(p))
                    points.put(p, true); // overlapping points are true
                else points.put(p, false);
                route.add(p);
            }
            routes.add(route);
        }
        return routes.stream().map(r -> simplifyPolyline(r, points, 0.1)).toList();
    }
}
