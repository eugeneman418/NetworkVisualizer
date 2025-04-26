package org.networkvisualizer.visualizer;

import com.sothawo.mapjfx.*;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.networkvisualizer.network.Network;
import org.networkvisualizer.network.Timeline;
import org.opencv.core.Point;

import java.util.*;
import java.util.stream.Collectors;

public class VisualizationController {
    private Network network;
    private Timeline timeline;

    private int timeIndex;

    private Map<List<Point>, CoordinateLine> pathToCoordinateLine = new HashMap<>();
    private CoordinateLine testLine = new CoordinateLine(new Coordinate(51.9225, 4.47917), new Coordinate(52.0705, 4.3007));
    private MapLabel testLabel;

    private Map<String, Integer> maxIntensityByMode;
    private Integer maxIntensity;

    @FXML
    private Rectangle colorBar; // The BarChart for displaying the vehicle data
    @FXML
    private Text maxIntensityLabel;
    @FXML
    private VBox colorBarContainer;

    @FXML
    private MapView mapView;         // The MapView for displaying the map
    @FXML
    private TableView<?> tableView;  // The TableView for displaying origin, destination, category, quantity
    @FXML
    private TableColumn<?, ?> originCol; // Column for origin in the TableView
    @FXML
    private TableColumn<?, ?> destCol;   // Column for destination in the TableView
    @FXML
    private TableColumn<?, ?> catCol;    // Column for category in the TableView
    @FXML
    private TableColumn<?, ?> quantCol;  // Column for quantity in the TableView
    @FXML
    private CheckBox showTruckBox;  // CheckBox to toggle visibility of truck data
    @FXML
    private CheckBox showTrainBox;  // CheckBox to toggle visibility of train data
    @FXML
    private CheckBox showBargeBox;  // CheckBox to toggle visibility of barge data
    @FXML
    private Slider timeSlider;      // Slider for adjusting the time
    @FXML
    private Text timeLabel;         // Text element to display the time label

    /**
     * setter for network and timeline
     * @param network
     * @param timeline
     */
    public void init(Network network, Timeline timeline) {
        this.network = network;
        this.timeline = timeline;

        timeLabel.setText("Time: " + currentTime());
        timeSlider.setMin(0);
        timeSlider.setMax(timeline.getTimesteps().size() - 1);
        timeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int rounded = Math.round(newVal.floatValue()); // snap to nearest int
            if (rounded != timeIndex) {
                timeIndex = rounded;
                timeLabel.setText("Time: " + currentTime());
                updateMap();
            }

        });


        disableControl(); // disable control until maps is loaded in

        calculateMaxIntensityByMode();
        initializeMap();

        colorBar.setFill(new LinearGradient(
                0, 0, 0, 1,
                true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.RED),
                new Stop(1, Color.GREEN)
        ));
        colorBar.heightProperty().bind(colorBarContainer.heightProperty().multiply(0.8));




    }

    private void disableControl() {
        showTruckBox.setDisable(true);
        showTrainBox.setDisable(true);
        showBargeBox.setDisable(true);
        timeSlider.setDisable(true);

    }

    private void enableControl() {
        showTruckBox.setDisable(false);
        showTrainBox.setDisable(false);
        showBargeBox.setDisable(false);
        timeSlider.setDisable(false);

    }

    private void initializeMap() {
        mapView.setMapType(MapType.OSM);
        double centerLat = 0;
        double centerLon = 0;
        for (Network.Vertex vertex : network.nodes.values()) {
            centerLat += vertex.lat();
            centerLon += vertex.lon();
        }
        if (network.nodes.values().size() > 0) {
            centerLat /= network.nodes.values().size();
            centerLon /= network.nodes.values().size();
        }

        mapView.setCenter(new Coordinate( centerLat, centerLon)); // start at centroid of nodes in network
        mapView.initialize();

        mapView.initializedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                afterMapIsInitialized();
            }
        });

    }

    private void afterMapIsInitialized() {


        for (List<Point> path : network.intersectionGraph.paths.keySet()) {
            CoordinateLine coordinateLine = new CoordinateLine(
                    path.stream().map(VisualizationController::pointToCoordinates).collect(Collectors.toList()));
            coordinateLine.setColor(Color.CYAN).setWidth(5).setVisible(true);
            pathToCoordinateLine.put(path,coordinateLine);
            mapView.addCoordinateLine(coordinateLine);
        }
        updateMap();


        enableControl();
    }

    private double currentTime() {
        return timeline.getTimesteps().get(timeIndex);
    }

    private void calculateMaxIntensityByMode() {
        if (maxIntensityByMode == null) {
            maxIntensityByMode = new HashMap<>();
        }
        else {
            maxIntensityByMode.clear();
        }


        for (double time : timeline.getTimesteps()) { // across all time steps
            for (List<Point> path : network.intersectionGraph.paths.keySet()) { // on each path
                for (String mode : Network.validModes) { // for every vehicle type
                    int intensity = timeline.getEvents(time, path, mode).stream()
                            .mapToInt(Timeline.Event::quantity).sum();
                    if (!maxIntensityByMode.containsKey(mode) || maxIntensityByMode.get(mode) < intensity) {
                        maxIntensityByMode.put(mode, intensity);
                    }
                }
            }
        }
    }

    /**
     * update map when selected mode changes
     */
    @FXML
    private void updateMap() {


        // update what red on colour bar indicates
        maxIntensity = 0;
        if (showTruckBox.isSelected()) {
            maxIntensity = Math.max(maxIntensity, maxIntensityByMode.get(Network.TRUCK));
        }
        if (showTrainBox.isSelected()) {
            maxIntensity = Math.max(maxIntensity, maxIntensityByMode.get(Network.TRAIN));
        }
        if (showBargeBox.isSelected()) {
            maxIntensity = Math.max(maxIntensity, maxIntensityByMode.get(Network.BARGE));
        }

        maxIntensityLabel.setText(String.valueOf(maxIntensity));

        // rerender routes
        renderRoutes(currentTime());

    }


    private void renderRoutes(double time) {
        // each path on intersection graph is a coordinate line
        // all links on a path have the same traffic, so we only need to query on first link of each path
        for (List<Point> path : network.intersectionGraph.paths.keySet()) {
            CoordinateLine coordinateLine = pathToCoordinateLine.get(path);

            Set<String> modes = new HashSet<>();
            if (showTruckBox.isSelected()) {
                modes.add(Network.TRUCK);
            }
            if (showTrainBox.isSelected()) {
                modes.add(Network.TRAIN);
            }
            if (showBargeBox.isSelected()) {
                modes.add(Network.BARGE);
            }
            int intensity = timeline.getEvents(time, path, modes).stream().mapToInt(Timeline.Event::quantity).sum();
            mapView.removeCoordinateLine(coordinateLine);
            coordinateLine.setColor(intensityToColor(intensity));
            mapView.addCoordinateLine(coordinateLine);


        }

    }

    private static Coordinate pointToCoordinates(Point p) {
        return new Coordinate(p.x, p.y);
    }

    private Color intensityToColor(int intensity) {
        float ratio = ((float) intensity) / maxIntensity;
        ratio = ratio < 0 ? 0 : ratio; // clamp
        ratio = ratio > 1 ? 1 : ratio;

        // linearly interpolate between Color.GREEN and Color.REF
        double red = (1 - ratio) * Color.GREEN.getRed() + ratio * Color.RED.getRed();
        double green = (1 - ratio) * Color.GREEN.getGreen() + ratio * Color.RED.getGreen();
        double blue = (1 - ratio) * Color.GREEN.getBlue() + ratio * Color.RED.getBlue();
        return new Color(red, green, blue, 1.0);

    }

}
