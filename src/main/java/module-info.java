module com.reprojector {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.swing;

    opens com.reprojector to javafx.fxml;
    exports com.reprojector;
    exports com.reprojector.options;
    opens com.reprojector.options to javafx.fxml;
    exports com.reprojector.helpers;
    opens com.reprojector.helpers to javafx.fxml;
    exports com.reprojector.imagetoglobecalculations;
    opens com.reprojector.imagetoglobecalculations to javafx.fxml;
}