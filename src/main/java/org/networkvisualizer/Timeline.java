package org.networkvisualizer;

import com.opencsv.CSVReader;
import org.apache.commons.collections.OrderedBidiMap;
import org.opencv.core.Point;
import org.opencv.dnn.Net;

import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Timeline {

    private TreeMap<Double, Set<Event>> events = new TreeMap<>(); // use tree map so events are ordered by time
    private Network network;

    private Map<Link, Set<Network.Edge>> links = new HashMap<>(); // acceleration structure to store the edges passing though each link

    public Timeline(Network network, Collection<Event> events) throws Exception {
        this.network = network;
        if (!new HashSet<>(network.edges).containsAll(events.stream().map(event -> event.edge).collect(Collectors.toSet()))) {
            throw new IllegalArgumentException("Timeline contains events on non-existing edges");
        }

        for (var event: events) {
            if (event.time < 0) throw new IllegalArgumentException("Time cannot be negative");
            this.events.computeIfAbsent(event.time, t -> new HashSet<>()).add(event);
        }

        List<Network.Route> routes = network.getRoutes();
        for (int i = 0; i < network.edges.size(); i++) {
            Network.Edge edge = network.edges.get(i);
            List<Point> route = routes.get(i).path();
            for (int j = 1; j < route.size(); j++) {
                Point start = route.get(j-1);
                Point end = route.get(j);
                Link link = new Link(start, end);
                // add edge to set (create set first if link is new)
                links.computeIfAbsent(link, l -> new HashSet<>()).add(edge);
            }
        }


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

    public Set<Event> getEvents(double time, Link link) {
        // for each edge that passes over link, get all of its events, then aggregate
        return links.get(link).stream().map(edge -> getEvents(time, edge))
                .flatMap(Set::stream).collect(Collectors.toSet());
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
     * A link is a pair of consecutive points on a route. In other words, a route is a sequence of links
     */
    public record Link(Point start, Point end) {

    }
}
