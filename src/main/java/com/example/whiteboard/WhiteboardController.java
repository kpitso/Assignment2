package com.example.whiteboard;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class WhiteboardController {
    private GraphicsContext gc;
    private Color currentColor = Color.BLACK;
    private double brushSize = 5;
    private String currentTool = "pen";
    private boolean isTextMode = false;

    public void setGraphicsContext(GraphicsContext gc) {
        this.gc = gc;
        updateToolSettings();
    }

    public void handleMousePressed(double x, double y) {
        if (isTextMode) {
            return;
        }

        gc.beginPath();
        gc.moveTo(x, y);
        gc.stroke();
    }

    public void handleMouseDragged(double x, double y) {
        if (!isTextMode) {
            gc.lineTo(x, y);
            gc.stroke();
        }
    }

    public void handleMouseReleased() {
        if (!isTextMode) {
            gc.closePath();
        }
    }

    public void setTool(String tool) {
        currentTool = tool;
        isTextMode = tool.equals("text");
        updateToolSettings();
    }

    public void setColor(Color color) {
        currentColor = color;
        updateToolSettings();
    }

    public void setBrushSize(double size) {
        brushSize = size;
        gc.setLineWidth(brushSize);
    }
    public boolean isTextMode() {
        return isTextMode;
    }
    public String getCurrentTool() {
        return currentTool;
    }
    public Color getCurrentColor() {
        return currentColor;
    }
    public double getBrushSize() {
        return brushSize;
    }

    public void clearCanvas() {
        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
    }

    public Image loadImage(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            return new Image(file.toURI().toString());
        }
        return null;
    }

    public MediaPlayer loadMedia(Stage stage, String type) {
        FileChooser fileChooser = new FileChooser();
        String[] extensions;
        String description;

        if (type.equals("video")) {
            extensions = new String[]{"*.mp4", "*.avi", "*.mov", "*.flv"};
            description = "Video Files";
        } else {
            extensions = new String[]{"*.mp3", "*.wav", "*.aac"};
            description = "Audio Files";
        }

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(description, extensions));
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            return new MediaPlayer(new Media(file.toURI().toString()));
        }
        return null;
    }

    public void saveCanvas(Stage stage, String format) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(format + " files", "*." + format.toLowerCase()));
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            Image snapshot = gc.getCanvas().snapshot(null, null);
            ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null),
                    format.toLowerCase(), file);
        }
    }

    private void updateToolSettings() {
        if (currentTool.equals("pen") || currentTool.equals("text")) {
            gc.setStroke(currentColor);
            gc.setFill(currentColor);
        } else if (currentTool.equals("eraser")) {
            gc.setStroke(Color.WHITE);
        }
        gc.setLineWidth(brushSize);
    }

    //

}