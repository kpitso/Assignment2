package com.example.whiteboard;

import com.example.whiteboard.WhiteboardUI;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class WhiteboardApp extends Application {

    private WhiteboardUI whiteboardUI;
    private WhiteboardController controller;

    @Override
    public void start(Stage primaryStage) {
        // Create the controller which will manage the application logic
        controller = new WhiteboardController();

        // Create the UI components
        whiteboardUI = new WhiteboardUI(controller);

        // Set up the scene
        Scene scene = new Scene(whiteboardUI.getRoot(), 1200, 700);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        primaryStage.setTitle("Whiteboard");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
