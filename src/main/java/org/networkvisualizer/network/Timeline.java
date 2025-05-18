package org.networkvisualizer.network;

import com.opencsv.CSVReader;
import org.opencv.core.Point;

import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Timeline {

    private final TreeMap<Double, Set<Event>> events = new TreeMap<>(); // use tree map so events are ordered by time

    private final Network network;

    public Map<String, Integer> maxIntensityByMode;

    public Timeline(Network network, Collection<Event> events) throws Exception {
        if (!new HashSet<>(network.edges).containsAll(events.stream().map(event -> event.edge).collect(Collectors.toSet()))) {
            throw new IllegalArgumentException("Timeline contains events on non-existing edges");
        }

        for (var event: events) {
            if (event.time < 0) throw new IllegalArgumentException("Time cannot be negative");
            this.events.computeIfAbsent(event.time, t -> new HashSet<>()).add(event);
        }

        this.network = network;

        calculateMaxIntensityByMode();
    }

    public double endTime() {
        return events.lastKey();
    }

    public List<Double> getTimesteps() {
        List<Double> timesteps = events.keySet().stream().toList();
        assert IntStream.range(1, timesteps.size())
                .allMatch(i -> timesteps.get(i - 1) <= timesteps.get(i)): "Timesteps should be returned as sorted list";
        return timesteps;
    }

    /**
     * get all events at a time step
     */
    public Set<Event> getEvents(double time) {
        return events.get(time);
    }

    /**
     * get all events at a time step along an edge
     */
    public Set<Event> getEvents(double time, Network.Edge edge) {
        return getEvents(time).stream().filter(event -> event.edge.equals(edge)).collect(Collectors.toSet());
    }

    /**
     * get events at a time step, filtered by mode
     */
    public Set<Event> getEvents(double time, String mode) {
        return getEvents(time).stream().filter(event -> event.edge.mode().equals(mode)).collect(Collectors.toSet());
    }

    public Set<Event> getEvents(double time, List<Point> path) {
        // for each edge that passes over link, get all of its events, then aggregate

        return network.intersectionGraph.pathToEdges.get(path).stream().map(edgeIdx -> getEvents(time, network.edges.get(edgeIdx)))
                .flatMap(Set::stream).collect(Collectors.toSet());
    }

    public Set<Event> getEvents(double time, List<Point> path, String mode) {
        // for each edge that passes over link, get all of its events, then aggregate
        return getEvents(time, path).stream()
                .filter(event -> event.edge.mode().equals(mode)).collect(Collectors.toSet());
    }

    public Set<Event> getEvents(double time, List<Point> path, Set<String> modes) {
        // for each edge that passes over link, get all of its events, then aggregate
        return getEvents(time, path).stream()
                .filter(event -> modes.contains(event.edge.mode())).collect(Collectors.toSet());
    }



    public static Timeline initializeFromCsv(String csvPath, Network network, boolean hasHeader) throws Exception {
        List<Event> events = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(csvPath))) {
            String[] line;

            if (hasHeader) reader.readNext(); // skip header

            while ((line = reader.readNext()) != null) {
                if (line.length != 6) throw new Exception("Excepts CSV to have 6 columns, but found: " + line.length);
                double time = Double.parseDouble(line[0]);
                String from = line[1];
                String to = line[2];
                String mode = line[3];
                String category = line[4];
                int quantity = Integer.parseInt(line[5]);
                Network.Edge edge = new Network.Edge(from, to, mode);
                events.add(new Event(edge, time, category, quantity));
            }
        }
        return new Timeline(network, events);
    }







    /**
     * A timeline is a sequence of events.
     * Each event is defined by when it occurs and where (the edge) it occurs.
     * Event describes transportation of `quantity` of items from `category`.
     */
    public record Event(Network.Edge edge, double time, String category, int quantity) {

    }

    /**
     * A link is a pair of consecutive points on a route. In other words, a route is a sequence of pathToEdges
     */
    public record Link(Point start, Point end) {
    }

    private void calculateMaxIntensityByMode() {
        if (maxIntensityByMode == null) {
            maxIntensityByMode = new HashMap<>();
        }
        else {
            maxIntensityByMode.clear();
        }


        for (double time : getTimesteps()) { // across all time steps

            for (List<Point> path : network.intersectionGraph.paths) { // on each path
                for (String mode : Network.validModes) { // for every vehicle type
                    int intensity = getEvents(time, path, mode).stream()
                            .mapToInt(Timeline.Event::quantity).sum();
                    if (!maxIntensityByMode.containsKey(mode) || maxIntensityByMode.get(mode) < intensity) {
                        maxIntensityByMode.put(mode, intensity);
                    }
                }
            }
        }
    }

}
