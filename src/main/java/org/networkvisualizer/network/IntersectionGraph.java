package org.networkvisualizer.network;

import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.util.*;
import java.util.stream.Collectors;


public class IntersectionGraph {
    static {
        nu.pattern.OpenCV.loadLocally() ;
    }
    public Set<Point> nodes = new HashSet<>(); // nodes are either endpoints or branching points

    public Map<List<Point>, Set<Integer>> pathToEdges = new HashMap<>();

    public List<List<Point>> paths = new ArrayList<>(); // use this list for a fix order of iteration (since hashset doesn't have a fixed order of iteration through its keys)


    /**
     * Create intersection group from routes.
     * Simplification factor is error tolerance during simplification process, in percentage of path length
     * Simplification factor <= 0 means no simplifcation, higher means more simplification
     * @param routes
     * @param simplificationFactor
     */
    public IntersectionGraph(final List<List<Point>> routes, final double simplificationFactor) {
        List<Set<Point>> setRoutes = routes.stream().map(r -> new HashSet<>(r)).collect(Collectors.toList()); //set representation for faster query
        for (int i = 0; i < routes.size(); i++) {
            List<Point> thisRoute = routes.get(i);
            if (thisRoute.isEmpty()) continue;
            nodes.add(thisRoute.get(0)); // end points are branching points
            nodes.add(thisRoute.get(thisRoute.size()-1));
            for (int k = 1; k < thisRoute.size()-1; k++) {
                for (int j = i+1; j < routes.size(); j++) {
                    Set<Point> otherRoute = setRoutes.get(j);
                    Point previousPoint = thisRoute.get(k-1);
                    Point currentPoint = thisRoute.get(k);
                    Point nextPoint = thisRoute.get(k+1);
                    if (otherRoute.contains(currentPoint)) { // intersection
                        if (!otherRoute.contains(previousPoint) || !otherRoute.contains(nextPoint)) // branching point
                            nodes.add(currentPoint);
                    }

                }
            }

        }


        // now compute the pathToEdges
        for (int n = 0 ; n < routes.size(); n++) {
            List<Point> route = routes.get(n);
            if (route.isEmpty()) continue;
            List<Point> currentPath = new ArrayList<>();
            currentPath.add(route.get(0)); // add starting point
            for (int i = 1; i < route.size(); i++) {
                Point nextPoint = route.get(i);
                currentPath.add(nextPoint); // expand current path by 1 point
                if (nodes.contains(nextPoint)) { // next point is intersection -> end of path
                    pathToEdges.computeIfAbsent(currentPath, k -> new TreeSet<>()).add(n); // add path to pathToEdges, and save the route number it belongs to
                    currentPath = new ArrayList<>(); //start new path
                    currentPath.add(nextPoint); // start & end points of a path are always nodes
                }
            }

        }

        if (simplificationFactor > 0) {
            simplifyPaths(simplificationFactor);
        }

        for (List<Point> path : pathToEdges.keySet()) {
            paths.add(path);
        }
    }

    public IntersectionGraph(final List<List<Point>> routes) {
        this(routes, 0.05); // default 5% tolerance
    }

    public void simplifyPaths(double simplificationFactor) {
        Map<List<Point>, Set<Integer>> simplifiedPaths = new HashMap<>();
        for (var entry : pathToEdges.entrySet()) {
            simplifiedPaths.put(simplifyPath(entry.getKey(), simplificationFactor), entry.getValue());
        }
        pathToEdges = simplifiedPaths;
    }

    private static List<Point> simplifyPath(List<Point> path, double simplificationFactor) {
        MatOfPoint2f inputCurve = new MatOfPoint2f();
        inputCurve.fromList(path);
        MatOfPoint2f outputCurve = new MatOfPoint2f();
        double epsilon = simplificationFactor * Imgproc.arcLength(inputCurve,false);
        Imgproc.approxPolyDP(inputCurve, outputCurve, epsilon, false);

//        System.out.println("Input segment length: " + path.size());
//        System.out.println("Output segment length: " + outputCurve.toList().size());
        return new ArrayList<>(outputCurve.toList());
    }

    public static IntersectionGraph fromGhRoutes(List<PointList> ghRoutes) {
        List<List<Point>> routes = new ArrayList<>();
        for (PointList ghRoute : ghRoutes) {
            List<Point> route = new ArrayList<>();
            for (GHPoint ghPoint : ghRoute) {
                Point p = new Point(ghPoint.getLat(), ghPoint.getLon());
                route.add(p);
            }
            routes.add(route);
        }
        return new IntersectionGraph(routes);
    }

}
