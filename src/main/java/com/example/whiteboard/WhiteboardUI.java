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
    private VBox mediaPanel;
    private WhiteboardController controller;

    public WhiteboardUI(WhiteboardController controller) {
        this.controller = controller;
        initializeUI();
    }

    private void initializeUI() {
        root = new BorderPane();
        root.getStyleClass().add("root");

        // Create canvas
        canvas = new Canvas(800, 600);
        controller.setGraphicsContext(canvas.getGraphicsContext2D());

        // Setup drawing handlers
        setupCanvasHandlers();

        // Create left toolbar (vertical)
        VBox leftToolbar = createLeftToolbar();

        // Create media panel
        mediaPanel = new VBox(10);
        mediaPanel.getStyleClass().add("media-panel");
        ScrollPane mediaScroll = new ScrollPane(mediaPanel);
        mediaScroll.setFitToWidth(true);
        mediaScroll.setPrefWidth(250);

        // Text input field
        textField = new TextField();
        textField.setVisible(false);
        textField.setOnAction(e -> {
            if (controller.isTextMode()) {
                GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.setFont(Font.font(controller.getBrushSize() * 3));
                gc.fillText(textField.getText(), textField.getLayoutX(), textField.getLayoutY());
                textField.clear();
                textField.setVisible(false);
            }
        });

        // Create main content area
        StackPane canvasContainer = new StackPane(canvas, textField);
        canvasContainer.getStyleClass().add("canvas-container");

        // Create right panel with canvas and media
        SplitPane rightPanel = new SplitPane();
        rightPanel.getItems().addAll(canvasContainer, mediaScroll);
        rightPanel.setDividerPositions(0.1);

        // Create top toolbar (for media and save options)
        HBox topToolbar = createTopToolbar();

        // Set up main layout
        BorderPane mainContent = new BorderPane();
        mainContent.setTop(topToolbar);
        mainContent.setCenter(rightPanel);

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
            }
            else
            {
                controller.handleMousePressed(e.getX(), e.getY());
            }
        });

        canvas.setOnMouseDragged(e -> {
            controller.handleMouseDragged(e.getX(), e.getY());
        });

        canvas.setOnMouseReleased(e -> {
            controller.handleMouseReleased();
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

        // Clear button
        Button clearBtn = createToolButton("Clear Canvas", "clear");
        clearBtn.setPrefWidth(140);
        clearBtn.setOnAction(e -> {
            controller.clearCanvas();
            mediaPanel.getChildren().clear();
        });

        leftToolbar.getChildren().addAll(
                toolsLabel,
                penBtn, eraserBtn, textBtn,
                colorLabel, colorPicker,
                sizeLabel, sizeSlider,
                clearBtn
        );

        return leftToolbar;
    }

    private HBox createTopToolbar() {
        HBox topToolbar = new HBox(10);
        topToolbar.getStyleClass().add("top-toolbar");
        topToolbar.setPadding(new javafx.geometry.Insets(5));

        // Media buttons
        Button addImageBtn = createToolButton("Add Image", "image");
        addImageBtn.setOnAction(e -> addImage());

        Button addVideoBtn = createToolButton("Add Video", "video");
        addVideoBtn.setOnAction(e -> addVideo());

        Button addAudioBtn = createToolButton("Add Audio", "audio");
        addAudioBtn.setOnAction(e -> addAudio());

        // Save options
        ComboBox<String> saveFormat = new ComboBox<>();
        saveFormat.getItems().addAll("PNG", "JPG", "GIF", "BMP");
        saveFormat.setValue("PNG");
        saveFormat.setPrefWidth(80);

        Button saveBtn = createToolButton("Save", "save");
        saveBtn.setOnAction(e -> {
            try {
                controller.saveCanvas((Stage) root.getScene().getWindow(), saveFormat.getValue());
            } catch (Exception ex) {
                showAlert("Error saving file: " + ex.getMessage());
            }
        });

        topToolbar.getChildren().addAll(
                addImageBtn, addVideoBtn, addAudioBtn,
                new Separator(),
                new Label("Format:"), saveFormat, saveBtn
        );

        return topToolbar;
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

    private void addImage() {
        Image image = controller.loadImage((Stage) root.getScene().getWindow());
        if (image != null) {
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(200);

            Slider sizeSlider = new Slider(0, 800, 500);
            sizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                imageView.setFitWidth(newVal.doubleValue());
            });

            VBox container = createMediaContainer(imageView, sizeSlider);
            mediaPanel.getChildren().add(container);
        }
    }

    private void addVideo() {
        MediaPlayer mediaPlayer = controller.loadMedia((Stage) root.getScene().getWindow(), "video");
        if (mediaPlayer != null) {
            MediaView mediaView = new MediaView(mediaPlayer);
            mediaView.setPreserveRatio(true);
            mediaView.setFitWidth(250);

            Slider sizeSlider = new Slider(100, 500, 250);
            sizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                mediaView.setFitWidth(newVal.doubleValue());
            });

            VBox container = createMediaContainer(mediaView, sizeSlider);
            container.getChildren().add(createMediaControls(mediaPlayer));
            mediaPanel.getChildren().add(container);

            mediaPlayer.play();
        }
    }

    private void addAudio() {
        MediaPlayer mediaPlayer = controller.loadMedia((Stage) root.getScene().getWindow(), "audio");
        if (mediaPlayer != null) {
            VBox container = new VBox(5);
            container.getStyleClass().add("media-item");

            Button deleteBtn = new Button("Delete");
            deleteBtn.getStyleClass().add("delete-button");
            deleteBtn.setOnAction(e -> {
                mediaPlayer.stop();
                mediaPanel.getChildren().remove(container);
            });

            container.getChildren().addAll(
                    new Label("Audio Track"),
                    createMediaControls(mediaPlayer),
                    deleteBtn
            );
            mediaPanel.getChildren().add(container);

            mediaPlayer.play();
        }
    }

    private VBox createMediaContainer(javafx.scene.Node view, Slider sizeSlider) {
        VBox container = new VBox(5);
        container.getStyleClass().add("media-item");

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("delete-button");
        deleteBtn.setOnAction(e -> mediaPanel.getChildren().remove(container));

        container.getChildren().addAll(view, sizeSlider, deleteBtn);
        return container;
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

        return new HBox(5, playBtn, pauseBtn, stopBtn, volumeSlider);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public BorderPane getRoot() {
        return root;
    }
}