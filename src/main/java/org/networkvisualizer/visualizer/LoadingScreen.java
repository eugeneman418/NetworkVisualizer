package org.networkvisualizer.visualizer;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import org.networkvisualizer.server.VisualizationServer;

import java.io.IOException;

public class LoadingScreen extends Application {



    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(LoadingScreen.class.getResource("/javafx/landing_page.fxml"));

        Scene scene = new Scene(fxmlLoader.load() );
        stage.setTitle("Network Visualizer");
        stage.setScene(scene);
        // stage.setMaximized(true);

        stage.show();
    }
    public static void main(String[] args) throws IOException {
        launch();

//        Router router = new Router("resources/test/zh.osm.pbf");
//        Network network = NetworkParser.parseJson("resources/test/network.json");
//        network.calculateRoutes(router);
//        Timeline timeline = Timeline.initializeFromCsv("resources/test/timeline.csv", network, true);
//
//        System.out.println(network.edges.get(10));
//        System.out.println(network.getRoutes().get(10));
//
//        for (var event: timeline.getEvents(21.4173,
//                new Timeline.Link(
//                        new Point(51.9161665, 4.4819428),
//                        new Point(51.9156208, 4.4819686)
//                )
//            ))
//            System.out.println(event);

    }
}