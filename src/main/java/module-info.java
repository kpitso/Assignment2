module com.example.whiteboardapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires javafx.swing;


    exports com.example.whiteboard;
    opens com.example.whiteboard to javafx.fxml;
}