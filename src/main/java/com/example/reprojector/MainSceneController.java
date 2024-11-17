package com.example.reprojector;

import com.example.reprojector.options.Defaults;
import com.example.reprojector.options.MapProjection;
import com.example.reprojector.options.MapProjectionConverter;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class MainSceneController {
    @FXML
    public TextField outputWidth;
    @FXML
    public TextField outputHeight;
    @FXML
    private StackPane stackPane;
    @FXML
    private VBox inputsVBox;
    @FXML
    private VBox globeParent;
    @FXML
    private Sphere globe;
    @FXML
    private ColorPicker fillColorPicker;
    @FXML
    private ImageView finalProjection;
    @FXML
    private ChoiceBox<MapProjection> projectionChoice;
    @FXML
    private TabPane mainTabPane;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private Button exportProjectionButton;

    private double startX, startY, startZ;
    private double moveStartAngleX, moveStartAngleY;
    private final DoubleProperty angleX = new SimpleDoubleProperty(0);
    private final DoubleProperty angleY = new SimpleDoubleProperty(0);

    public void initialize() {
        addGlobeResizingListeners();
        addGlobeMouseDraggingListeners();
        stackPane.widthProperty().addListener((a, b, c) -> progressIndicator.setMaxWidth(c.doubleValue()));
        stackPane.heightProperty().addListener((a, b, c) -> progressIndicator.setMaxHeight(c.doubleValue()));
        addColorPickerListener();
        projectionChoice.getItems().addAll(
                Arrays.stream(MapProjection.values()).filter(a -> a != MapProjection.UV).toList()
        );
        projectionChoice.setConverter(new MapProjectionConverter());
        projectionChoice.setValue(Defaults.MAP_PROJECTION);
        projectionChoice.valueProperty().addListener((obs, oldV, newV) ->
                WorldScaleData.setMapProjection(projectionChoice.getValue()));
        WorldScaleData.setMapProjection(projectionChoice.getValue());

        mainTabPane.widthProperty().addListener((a, b, c) ->
                finalProjection.fitWidthProperty().set(c.intValue()));
        mainTabPane.heightProperty().addListener((a, b, c) ->
                finalProjection.fitWidthProperty().set(c.intValue()));
    }

    private void addGlobeResizingListeners() {
        ChangeListener<Number> globeBoxListener = (observableValue, oValue, nValue) ->
                globe.setRadius(Math.min(globeParent.getHeight(), globeParent.getWidth()) / 2.5);
        globeParent.heightProperty().addListener(globeBoxListener);
        globeParent.widthProperty().addListener(globeBoxListener);
    }

    private void addGlobeMouseDraggingListeners() {
        Rotate xRotate = new Rotate(0, Rotate.X_AXIS);
        Rotate yRotate = new Rotate(0, Rotate.Y_AXIS);
        globe.getTransforms().addAll(xRotate, yRotate);

        xRotate.angleProperty().bind(angleX);
        yRotate.angleProperty().bind(angleY);

        globe.setOnMouseEntered(event -> globe.setCursor(Cursor.HAND));

        globe.setOnMousePressed(event -> {
            globe.setCursor(Cursor.CLOSED_HAND);
            startX = event.getSceneX() - globe.getLayoutX();
            startY = event.getSceneY() - globe.getLayoutY();
            startZ = Math.sqrt(Math.pow(globe.getRadius(), 2) - Math.pow(startX, 2) - Math.pow(startY, 2));
            moveStartAngleX = angleX.get();
            moveStartAngleY = angleY.get();
        });

        globe.setOnMouseDragged(event -> {
            double newX = event.getSceneX() - globe.getLayoutX();
            double newY = event.getSceneY() - globe.getLayoutY();
            double newZ = Math.sqrt(Math.pow(globe.getRadius(), 2) - Math.pow(newX, 2) - Math.pow(newY, 2));
            if (!Double.isNaN(newZ)) {
                double angleChangeX = Math.toDegrees(Math.atan2(newY, newZ) - Math.atan2(startY, startZ));
                double angleChangeY = Math.toDegrees(Math.atan2(newX, newZ) - Math.atan2(startX, startZ));
                angleX.set(moveStartAngleX + angleChangeX);
                angleY.set(moveStartAngleY - angleChangeY);
            }
        });

        globe.setOnMouseReleased(event -> globe.setCursor(Cursor.HAND));
    }

    private void addColorPickerListener() {
        ChangeListener<Color> fillColorListener = (observableValue, oldValue, newValue) -> WorldScaleData.setFillColor(fillColorPicker.getValue());
        fillColorPicker.valueProperty().addListener(fillColorListener);
    }

    public void changeTab() {
        mainTabPane.setDisable(true);
        progressIndicator.setVisible(true);
        Thread thread = new Thread(() -> {
            mapWorldScaleInputToClass();
            angleX.set(0);
            angleY.set(0);
            WorldScaleData.recalculate();
            BufferedImage globeImage = WorldScaleData.getUvMap();
            BufferedImage projectionImage = WorldScaleData.getProjectionImage();
            WorldScaleData.makeListChangedFalse();
            WorldScaleData.makeFillColorChangedFalse();
            if (globeImage != null) {
                PhongMaterial material = new PhongMaterial();
                material.setDiffuseMap(SwingFXUtils.toFXImage(globeImage, null));
                globe.setMaterial(material);
            }
            if (projectionImage != null) {
                finalProjection.setImage(SwingFXUtils.toFXImage(projectionImage, null));
            }
            progressIndicator.setVisible(false);
            mainTabPane.setDisable(false);
        });
        thread.start();
    }

    private void mapWorldScaleInputToClass() {
        if (!outputWidth.getText().equals("")) {
            WorldScaleData.setProjectionImageWidth(Integer.parseInt(outputWidth.getText()));
        }
        if (!outputHeight.getText().equals("")) {
            WorldScaleData.setProjectionImageHeight(Integer.parseInt(outputHeight.getText()));
        }
        WorldScaleData.setMapProjection(projectionChoice.getValue());
    }

    public void addNewInput() {
        try {
            URL url = ReProjectorApplication.class.getResource("/single-input.fxml");
            ResourceBundle resourceBundle = ResourceBundle.getBundle("/Display");
            assert url != null;
            HBox singleInput = FXMLLoader.load(url, resourceBundle);
            inputsVBox.getChildren().add(singleInput);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exportMapProjection() throws IOException {
        BufferedImage projectionImage = WorldScaleData.getProjectionImage();
        if (projectionImage != null) {
            FileChooser exportFileChooser = new FileChooser();
            exportFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Images", "*.png"));
            File exportFile = exportFileChooser.showSaveDialog(exportProjectionButton.getScene().getWindow());
            if (exportFile != null) {
                ImageIO.write(projectionImage, "png", exportFile);
                exportFile.createNewFile();
            }
        }
    }

}