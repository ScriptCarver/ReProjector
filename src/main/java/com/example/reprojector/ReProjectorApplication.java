package com.example.reprojector;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class ReProjectorApplication extends Application {

    @Override
    public void start(Stage mainStage) throws IOException {
        WorldScaleData.init();
        URL url = ReProjectorApplication.class.getResource("/main-view.fxml");
        Locale locate = Locale.getDefault();
        ResourceBundle resourceBundle = ResourceBundle.getBundle("labels/Display", locate);
        assert url != null;
        mainStage = FXMLLoader.load(url, resourceBundle);

        //Closing stage
        mainStage.setOnCloseRequest(
                t -> {
                    Platform.exit();
                    System.exit(0);
                }
        );
        mainStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}