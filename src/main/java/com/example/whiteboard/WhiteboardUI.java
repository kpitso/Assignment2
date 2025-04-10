package com.example.whiteboard;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.media.MediaView;
import javafx.scene.image.ImageView;

public class WhiteboardUI {
    private BorderPane root;
    private Canvas canvas;
    private TextField textField;
    private WhiteboardController controller;
    private Pane canvasContainer;

    public WhiteboardUI(WhiteboardController controller) {
        this.controller = controller;
        this.canvasContainer = new Pane(); // Initialize canvasContainer first
        initializeUI();
    }

    private void initializeUI() {
        root = new BorderPane();
        root.getStyleClass().add("root");

        // Create canvas
        canvas = new Canvas(800, 600);
        controller.setGraphicsContext(canvas.getGraphicsContext2D());

        // Text input field
        textField = new TextField();
        textField.setVisible(false);

        // Set up canvas container
        canvasContainer.getStyleClass().add("canvas-container");
        canvasContainer.getChildren().addAll(canvas, textField);

        // Bind canvas size to container
        canvas.widthProperty().bind(canvasContainer.widthProperty());
        canvas.heightProperty().bind(canvasContainer.heightProperty());

        // Setup drawing handlers (now called after canvasContainer is initialized)
        setupCanvasHandlers();

        // Create left toolbar (vertical)
        VBox leftToolbar = createLeftToolbar();

        // Create top toolbar
        HBox topToolbar = createTopToolbar();

        // Set up main layout
        BorderPane mainContent = new BorderPane();
        mainContent.setTop(topToolbar);
        mainContent.setCenter(canvasContainer);

        root.setLeft(leftToolbar);
        root.setCenter(mainContent);
    }

    private void setupCanvasHandlers() {
        canvas.setOnMousePressed(e -> {
            if (controller.isTextMode()) {
                textField.setLayoutX(e.getX());
                textField.setLayoutY(e.getY());
                textField.setVisible(true);
                textField.requestFocus();
                textField.setOnAction(event -> {
                    GraphicsContext gc = canvas.getGraphicsContext2D();
                    gc.setFont(Font.font(controller.getBrushSize() * 3));
                    gc.fillText(textField.getText(), textField.getLayoutX(), textField.getLayoutY());
                    textField.clear();
                    textField.setVisible(false);
                });
            } else {
                controller.handleMousePressed(e.getX(), e.getY());
            }
        });

        canvas.setOnMouseDragged(e -> {
            controller.handleMouseDragged(e.getX(), e.getY());
        });

        canvas.setOnMouseReleased(e -> {
            controller.handleMouseReleased();
        });

        // Add mouse handlers for media elements
        canvasContainer.setOnMousePressed(e -> {
            if (!controller.isTextMode()) {
                controller.handleMediaDragStart(e.getX(), e.getY());
            }
        });

        canvasContainer.setOnMouseDragged(e -> {
            if (!controller.isTextMode()) {
                controller.handleMediaDrag(e.getX(), e.getY());
            }
        });

        canvasContainer.setOnMouseReleased(e -> {
            if (!controller.isTextMode()) {
                controller.handleMediaDragEnd();
            }
        });
    }

    private VBox createLeftToolbar() {
        VBox leftToolbar = new VBox(10);
        leftToolbar.getStyleClass().add("left-toolbar");
        leftToolbar.setPrefWidth(150);
        leftToolbar.setMinWidth(150);

        // Drawing tools
        Label toolsLabel = new Label("Tools");
        toolsLabel.getStyleClass().add("section-label");

        ToggleGroup toolGroup = new ToggleGroup();
        ToggleButton penBtn = createToggleToolButton("Draw", "pen", toolGroup);
        ToggleButton eraserBtn = createToggleToolButton("Eraser", "eraser", toolGroup);
        ToggleButton textBtn = createToggleToolButton("Text", "text", toolGroup);

        // Color picker
        Label colorLabel = new Label("Color");
        colorLabel.getStyleClass().add("section-label");
        ColorPicker colorPicker = new ColorPicker(controller.getCurrentColor());
        colorPicker.setPrefWidth(140);
        colorPicker.setOnAction(e -> controller.setColor(colorPicker.getValue()));

        // Brush size
        Label sizeLabel = new Label("Brush Size");
        sizeLabel.getStyleClass().add("section-label");
        Slider sizeSlider = new Slider(1, 50, controller.getBrushSize());
        sizeSlider.setShowTickLabels(true);
        sizeSlider.setShowTickMarks(true);
        sizeSlider.setMajorTickUnit(10);
        sizeSlider.setMinorTickCount(5);
        sizeSlider.setPrefWidth(140);
        sizeSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                controller.setBrushSize(newVal.doubleValue()));

        // Media buttons
        Button addImageBtn = createToolButton("Add Image", "image");
        addImageBtn.setPrefWidth(140);
        addImageBtn.setOnAction(e -> addImage());

        Button addVideoBtn = createToolButton("Add Video", "video");
        addVideoBtn.setPrefWidth(140);
        addVideoBtn.setOnAction(e -> addVideo());

        Button addAudioBtn = createToolButton("Add Audio", "audio");
        addAudioBtn.setPrefWidth(140);
        addAudioBtn.setOnAction(e -> addAudio());

        // Clear button
        Button clearBtn = createToolButton("Clear Canvas", "clear");
        clearBtn.setPrefWidth(140);
        clearBtn.setOnAction(e -> {
            controller.clearCanvas();
            canvasContainer.getChildren().removeIf(node -> !(node instanceof Canvas) && !(node instanceof TextField));
        });

        // Save options
        ComboBox<String> saveFormat = new ComboBox<>();
        saveFormat.getItems().addAll("PNG", "JPG", "GIF", "BMP");
        saveFormat.setValue("PNG");
        saveFormat.setPrefWidth(140);

        Button saveBtn = createToolButton("Save", "save");
        saveBtn.setPrefWidth(140);
        saveBtn.setOnAction(e -> {
            try {
                controller.saveCanvas((Stage) root.getScene().getWindow(), saveFormat.getValue());
            } catch (Exception ex) {
                showAlert("Error saving file: " + ex.getMessage());
            }
        });

        leftToolbar.getChildren().addAll(
                toolsLabel,
                penBtn, eraserBtn, textBtn,
                colorLabel, colorPicker,
                sizeLabel, sizeSlider,
                addImageBtn, addVideoBtn, addAudioBtn,
                saveFormat, saveBtn,
                clearBtn
        );

        return leftToolbar;
    }

    private HBox createTopToolbar() {
        HBox topToolbar = new HBox(10);
        topToolbar.getStyleClass().add("top-toolbar");
        topToolbar.setPadding(new javafx.geometry.Insets(5));

        // File operations
        Button openBtn = createToolButton("Open", "open");
        openBtn.setOnAction(e -> {
            try {
                controller.openFile((Stage) root.getScene().getWindow());
            } catch (Exception ex) {
                showAlert("Error opening file: " + ex.getMessage());
            }
        });

        // Edit operations
        Button undoBtn = createToolButton("Undo", "undo");
        undoBtn.setOnAction(e -> {
            try {
                controller.undo();
            } catch (Exception ex) {
                showAlert("Nothing to undo");
            }
        });

        Button redoBtn = createToolButton("Redo", "redo");
        redoBtn.setOnAction(e -> {
            try {
                controller.redo();
            } catch (Exception ex) {
                showAlert("Nothing to redo");
            }
        });

        // Add separators for visual grouping
        Separator fileSeparator = new Separator();
        fileSeparator.setOrientation(javafx.geometry.Orientation.VERTICAL);

        Separator editSeparator = new Separator();
        editSeparator.setOrientation(javafx.geometry.Orientation.VERTICAL);

        topToolbar.getChildren().addAll(
                openBtn,
                fileSeparator,
                undoBtn, redoBtn,
                editSeparator
        );

        return topToolbar;
    }

    private void addImage() {
        Image image = controller.loadImage((Stage) root.getScene().getWindow());
        if (image != null) {
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(500);
            imageView.setLayoutX(canvas.getWidth()/2 - 100);
            imageView.setLayoutY(canvas.getHeight()/2 - 100);

            imageView.setPickOnBounds(true);
            imageView.setMouseTransparent(false);

            WhiteboardController.MediaElement mediaElement =
                    new WhiteboardController.MediaElement(imageView, 0, 0, null);
            controller.addMediaElement(mediaElement);
            canvasContainer.getChildren().add(imageView);
        }
    }

    private void addVideo() {
        MediaPlayer mediaPlayer = controller.loadMedia((Stage) root.getScene().getWindow(), "video");
        if (mediaPlayer != null) {
            MediaView mediaView = new MediaView(mediaPlayer);
            mediaView.setPreserveRatio(true);
            mediaView.setFitWidth(550);
            mediaView.setLayoutX(canvas.getWidth()/2 - 125);
            mediaView.setLayoutY(canvas.getHeight()/2 - 125);

            // Make video view draggable
            mediaView.setPickOnBounds(true);
            mediaView.setMouseTransparent(false);

            // Add media controls
            HBox controls = createMediaControls(mediaPlayer);
            controls.setLayoutX(mediaView.getLayoutX());
            controls.setLayoutY(mediaView.getLayoutY() + mediaView.getFitHeight() + 5);

            WhiteboardController.MediaElement mediaElement =
                    new WhiteboardController.MediaElement(mediaView, 0, 0, mediaPlayer);
            controller.addMediaElement(mediaElement);
            canvasContainer.getChildren().addAll(mediaView, controls);
            mediaPlayer.play();
        }
    }

    private void addAudio() {
        MediaPlayer mediaPlayer = controller.loadMedia((Stage) root.getScene().getWindow(), "audio");
        if (mediaPlayer != null) {
            // Create controls for audio (no visual element on canvas)
            HBox controls = createMediaControls(mediaPlayer);
            controls.setLayoutX(200);
            controls.setLayoutY(200);

            canvasContainer.getChildren().add(controls);
            mediaPlayer.play();
        }
    }

    private HBox createMediaControls(MediaPlayer mediaPlayer) {
        Button playBtn = new Button("▶");
        playBtn.getStyleClass().add("control-button");
        playBtn.setOnAction(e -> mediaPlayer.play());

        Button pauseBtn = new Button("⏸");
        pauseBtn.getStyleClass().add("control-button");
        pauseBtn.setOnAction(e -> mediaPlayer.pause());

        Button stopBtn = new Button("⏹");
        stopBtn.getStyleClass().add("control-button");
        stopBtn.setOnAction(e -> mediaPlayer.stop());

        Slider volumeSlider = new Slider(0, 1, mediaPlayer.getVolume());
        volumeSlider.valueProperty().bindBidirectional(mediaPlayer.volumeProperty());

        Button deleteBtn = new Button("X");
        deleteBtn.getStyleClass().add("delete-button");
        deleteBtn.setOnAction(e -> {
            mediaPlayer.stop();
            canvasContainer.getChildren().removeIf(node ->
                    node instanceof HBox && ((HBox)node).getChildren().contains(playBtn));
        });

        return new HBox(5, playBtn, pauseBtn, stopBtn, volumeSlider, deleteBtn);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private ToggleButton createToggleToolButton(String text, String type, ToggleGroup group) {
        ToggleButton btn = new ToggleButton(text);
        btn.getStyleClass().add(type + "-button");
        btn.setToggleGroup(group);
        btn.setPrefWidth(140);
        btn.setSelected(type.equals("pen"));

        btn.setOnAction(e -> {
            if (btn.isSelected()) {
                controller.setTool(type);
            }
        });

        return btn;
    }

    private Button createToolButton(String text, String type) {
        Button btn = new Button(text);
        btn.getStyleClass().add(type + "-button");
        return btn;
    }

    public BorderPane getRoot() {
        return root;
    }
}
