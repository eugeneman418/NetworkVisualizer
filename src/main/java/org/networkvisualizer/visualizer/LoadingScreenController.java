package org.networkvisualizer.visualizer;

import com.opencsv.CSVWriter;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.networkvisualizer.network.Network;
import org.networkvisualizer.network.NetworkParser;
import org.networkvisualizer.network.Timeline;
import org.networkvisualizer.routing.Router;
import org.networkvisualizer.server.VisualizationServer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class LoadingScreenController {

    private File osm, network, events; // File is just an abstraction over path, it contains no data
    private boolean serverStarted = false;

    @FXML private Button selectMapButton;
    @FXML private Text mapLabel;

    @FXML private Button selectNetworkButton;
    @FXML private Text networkLabel;

    @FXML private Button selectEventsButton;
    @FXML private Text eventsLabel;

    @FXML private Button startSimulationButton;

    @FXML private Button exportDistMatrixButton;


    @FXML
    private void selectMap(ActionEvent event) {
        // Show dialog and store path if a file is chosen
        File selectedFile = selectFile(List.of(new FileChooser.ExtensionFilter("OSM Files", "*.osm.pbf")));
        if (selectedFile != null) {
            osm = selectedFile;
            mapLabel.setText(selectedFile.getName());

            updateButtonStates();
        }
    }

    // Called when "Choose Network" button is clicked
    @FXML
    private void selectNetwork(ActionEvent event) {
        File selectedFile = selectFile(List.of(new FileChooser.ExtensionFilter("Json", "*.json")));
        if (selectedFile != null) {
            network = selectedFile;
            networkLabel.setText(selectedFile.getName());

            updateButtonStates();
        }
    }

    // Called when "Choose Events" button is clicked
    @FXML
    private void selectEvents(ActionEvent event) {
        File selectedFile = selectFile(List.of(new FileChooser.ExtensionFilter("CSV", "*.csv")));
        if (selectedFile != null) {
            events = selectedFile;
            eventsLabel.setText(selectedFile.getName());

            updateButtonStates();
        }
    }

    // Called when "Start Simulation" button is clicked
    @FXML
    private void startSimulation(ActionEvent event) {
        if (serverStarted) {
            System.out.println("Server is already running.");
            return;
        }


        startSimulationButton.setDisable(true); // optionally disable the button

        try {
            if (!serverStarted) {
                VisualizationServer server = new VisualizationServer(
                        osm.getAbsolutePath(),
                        network.getAbsolutePath(),
                        events.getAbsolutePath()
                );
                startSimulationButton.setText("Go to localhost:"+server.port);
                serverStarted = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
            serverStarted = false; // reset if server fails to start
            // Optionally re-enable the button if desired

            startSimulationButton.setDisable(false);
            startSimulationButton.setText("Start Visualization");


        }
    }



    @FXML
    private void exportMatrix(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save OD Matrix");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));

        // Suggest a default file name
        fileChooser.setInitialFileName("od_matrix.csv");

        // Set extension filters
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        // Get the window
        Stage stage = (Stage) exportDistMatrixButton.getScene().getWindow();

        // Show save dialog
        File saveFile = fileChooser.showSaveDialog(stage);

        if (saveFile != null) {
            try (CSVWriter writer = new CSVWriter(new FileWriter(saveFile))) {
                writer.writeNext(new String[]{"From", "To", "Mode", "Distance (km)"});

                Router router = new Router(this.osm.getPath());
                Network net = NetworkParser.parseJson(this.network.getPath());
                net.calculateRoutes(router);
                for (int i = 0; i < net.edges.size(); i++) {
                    Network.Edge edge = net.edges.get(i);
                    double dist = net.distances.get(i);
                    writer.writeNext(new String[]{edge.from(), edge.to(), edge.mode(), String.valueOf(dist)});
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }



    // Optional: gets called automatically after FXML is loaded
    @FXML
    public void initialize() {
        startSimulationButton.setDisable(true);
        exportDistMatrixButton.setDisable(true);
    }

    private File selectFile(List<FileChooser.ExtensionFilter> extensions) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));

        // Optional: Set file filters
        fileChooser.getExtensionFilters().addAll(extensions);

        // Get the Stage from the button
        Stage stage = (Stage) startSimulationButton.getScene().getWindow();

        // Show dialog and store path if a file is chosen
        return fileChooser.showOpenDialog(stage);
    }

    private void updateStartButtonState() {
        boolean allFilesSelected = osm != null && network != null && events != null;
        startSimulationButton.setDisable(!allFilesSelected);
    }

    private void updateExportButtonState() {
        boolean allFilesSelected = osm != null && network != null;
        exportDistMatrixButton.setDisable(!allFilesSelected);
    }

    private void updateButtonStates() {
        updateExportButtonState();
        updateStartButtonState();
    }

}
