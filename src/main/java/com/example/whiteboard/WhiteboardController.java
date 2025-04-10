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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class WhiteboardController {
    private GraphicsContext gc;
    private Color currentColor = Color.BLACK;
    private double brushSize = 5;
    private String currentTool = "pen";
    private boolean isTextMode = false;
    private Deque<Image> undoStack = new ArrayDeque<>();
    private Deque<Image> redoStack = new ArrayDeque<>();
    private double lastX, lastY;
    private List<MediaElement> mediaElements = new ArrayList<>();
    private MediaElement selectedMediaElement;
    private double dragStartX, dragStartY;

    public static class MediaElement {
        public javafx.scene.Node node;
        public double startX, startY;
        public MediaPlayer mediaPlayer;
        public boolean isDragging;

        public MediaElement(javafx.scene.Node node, double startX, double startY, MediaPlayer mediaPlayer) {
            this.node = node;
            this.startX = startX;
            this.startY = startY;
            this.mediaPlayer = mediaPlayer;
            this.isDragging = false;
        }
    }

    public void setGraphicsContext(GraphicsContext gc) {
        this.gc = gc;
        updateToolSettings();
    }

    public void handleMousePressed(double x, double y) {
        lastX = x;
        lastY = y;

        if (isTextMode) return;

        gc.beginPath();
        gc.moveTo(x, y);
        gc.stroke();
        saveCurrentState();
    }

    public void handleMouseDragged(double x, double y) {
        if (!isTextMode && (currentTool.equals("pen") || currentTool.equals("eraser"))) {
            gc.lineTo(x, y);
            gc.stroke();
        }
    }

    public void handleMouseReleased() {
        if (!isTextMode) gc.closePath();
    }

    public void handleMediaDragStart(double x, double y) {
        for (MediaElement element : mediaElements) {
            if (element.node.getBoundsInParent().contains(x, y)) {
                selectedMediaElement = element;
                dragStartX = x - element.node.getLayoutX();
                dragStartY = y - element.node.getLayoutY();
                element.isDragging = true;
                break;
            }
        }
    }

    public void handleMediaDrag(double x, double y) {
        if (selectedMediaElement != null) {
            selectedMediaElement.node.setLayoutX(x - dragStartX);
            selectedMediaElement.node.setLayoutY(y - dragStartY);
        }
    }

    public void handleMediaDragEnd() {
        if (selectedMediaElement != null) {
            selectedMediaElement.isDragging = false;
            selectedMediaElement = null;
        }
    }

    public void setTool(String tool) {
        currentTool = tool;
        isTextMode = tool.equals("text");
        updateToolSettings();
    }

    private void updateToolSettings() {
        if (currentTool.equals("pen") || currentTool.equals("text")) {
            gc.setStroke(currentColor);
            gc.setFill(currentColor);
        } else if (currentTool.equals("eraser")) {
            gc.setStroke(Color.WHITE);
        }
        gc.setLineWidth(currentTool.equals("eraser") ? brushSize * 2 : brushSize);
    }

    public boolean isTextMode() { return isTextMode; }
    public double getBrushSize() { return brushSize; }
    public Color getCurrentColor() { return currentColor; }
    public String getCurrentTool() { return currentTool; }

    public void setColor(Color color) {
        currentColor = color;
        updateToolSettings();
    }

    public void setBrushSize(double size) {
        brushSize = size;
        updateToolSettings();
    }

    public void clearCanvas() {
        saveCurrentState();
        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
        clearMediaElements();
    }

    public void clearMediaElements() {
        for (MediaElement element : mediaElements) {
            if (element.mediaPlayer != null) {
                element.mediaPlayer.stop();
            }
        }
        mediaElements.clear();
    }

    public Image loadImage(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File file = fileChooser.showOpenDialog(stage);
        return file != null ? new Image(file.toURI().toString()) : null;
    }

    public MediaPlayer loadMedia(Stage stage, String type) {
        FileChooser fileChooser = new FileChooser();
        String[] extensions = type.equals("video") ?
                new String[]{"*.mp4", "*.avi", "*.mov"} :
                new String[]{"*.mp3", "*.wav", "*.aac"};
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(type + " Files", extensions));
        File file = fileChooser.showOpenDialog(stage);
        return file != null ? new MediaPlayer(new Media(file.toURI().toString())) : null;
    }

    public void addMediaElement(MediaElement element) {
        mediaElements.add(element);
    }

    public void openFile(Stage stage) {
        Image image = loadImage(stage);
        if (image != null) {
            saveCurrentState();
            gc.drawImage(image, 0, 0);
        }
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            redoStack.push(gc.getCanvas().snapshot(null, null));
            gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
            gc.drawImage(undoStack.pop(), 0, 0);
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            undoStack.push(gc.getCanvas().snapshot(null, null));
            gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
            gc.drawImage(redoStack.pop(), 0, 0);
        }
    }

    public void saveCanvas(Stage stage, String format) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(format + " files", "*." + format.toLowerCase())
        );
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            Image snapshot = gc.getCanvas().snapshot(null, null);
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(snapshot, null);
            ImageIO.write(bufferedImage, format.toLowerCase(), file);
        }
    }

    private void saveCurrentState() {
        undoStack.push(gc.getCanvas().snapshot(null, null));
        redoStack.clear();
    }
}
