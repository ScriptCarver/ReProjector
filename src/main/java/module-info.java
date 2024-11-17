module com.example.reprojector {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.swing;

    opens com.example.reprojector to javafx.fxml;
    exports com.example.reprojector;
    exports com.example.reprojector.options;
    opens com.example.reprojector.options to javafx.fxml;
    exports com.example.reprojector.helpers;
    opens com.example.reprojector.helpers to javafx.fxml;
    exports com.example.reprojector.imagetoglobecalculations;
    opens com.example.reprojector.imagetoglobecalculations to javafx.fxml;
}