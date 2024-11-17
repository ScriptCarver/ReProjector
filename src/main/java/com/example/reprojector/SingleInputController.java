package com.example.reprojector;

import com.example.reprojector.options.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class SingleInputController {
    @FXML
    private HBox parentBox;
    @FXML
    private TextField centralLatitude;
    @FXML
    private TextField centralLongitude;
    @FXML
    private TextField latitudeStretch;
    @FXML
    private TextField longitudeStretch;
    @FXML
    private TextField rotation;
    @FXML
    private ChoiceBox<ImageInputMethod> imageInputMethodChoiceBox;
    @FXML
    private Button imageChoosingButton;
    @FXML
    private ImageView previewImage;

    private SingleInputImage singleInput;

    public void initialize() {
        singleInput = WorldScaleData.addNewInput(this);
        setUpInputMethodChoiceBox();
        setUpUIHeight();

        centralLatitude.promptTextProperty().setValue(String.valueOf(Defaults.CENTRAL_LATITUDE));
        centralLongitude.promptTextProperty().setValue(String.valueOf(Defaults.CENTRAL_LONGITUDE));
        latitudeStretch.promptTextProperty().setValue(String.valueOf(Math.toDegrees(Defaults.LATITUDE_STRETCH)));
        longitudeStretch.promptTextProperty().setValue(String.valueOf(Math.toDegrees(Defaults.LONGITUDE_STRETCH)));
        rotation.promptTextProperty().setValue(String.valueOf(Math.toDegrees(Defaults.ROTATION)));

        imageInputMethodChoiceBox.valueProperty().addListener((observableValue, oldV, newV) -> {
            singleInput.setImageInputMethod(newV);
            WorldScaleData.setListChangedToTrue();
        });
    }

    public void mapAngleFieldsToParameters(){
        singleInput.setCentralLatitude(textToAngleTranslation(centralLatitude.getText()));
        singleInput.setCentralLongitude(textToAngleTranslation(centralLongitude.getText()));
        singleInput.setAngleStretch(textToAngleTranslation(latitudeStretch.getText()),
                textToAngleTranslation(longitudeStretch.getText()));
        singleInput.setRotation(textToAngleTranslation(rotation.getText()));
    }

    private void setUpInputMethodChoiceBox() {
        imageInputMethodChoiceBox.getItems().addAll(
                ImageInputMethod.values()
        );
        imageInputMethodChoiceBox.setConverter(new ImageInputMethodConverter());
        imageInputMethodChoiceBox.setValue(Defaults.IMAGE_INPUT_METHOD);
        singleInput.setImageInputMethod(imageInputMethodChoiceBox.getValue());
    }

    private void setUpUIHeight() {
        previewImage.fitHeightProperty().bind(parentBox.heightProperty());
    }

    public void onClickChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg"));
        File file = fileChooser.showOpenDialog(imageChoosingButton.getScene().getWindow());
        if (file != null) {
            try {
                BufferedImage inputImage = ImageIO.read(file);
                previewImage.setImage(SwingFXUtils.toFXImage(inputImage, null));
                singleInput.setSourceImage(inputImage);
                singleInput.setAngleStretch(textToAngleTranslation(latitudeStretch.getText()),
                        textToAngleTranslation(longitudeStretch.getText()));
                WorldScaleData.setListChangedToTrue();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void deleteElement() {
        WorldScaleData.removeInput(this);
        VBox vBox = (VBox) parentBox.getParent();
        vBox.getChildren().remove(parentBox);
    }

    private static double textToAngleTranslation(String text) {
        text = text.trim();
        if (text.isEmpty()) {
            return 0.0F;
        }
        double angle;
        if (text.contains("N") || text.contains("S") ||
                text.contains("W") || text.contains("E")) {
            angle = (float) Math.toRadians(
                    Float.parseFloat(
                            text.substring(0, text.length() - 1).trim()
                    )
            );
        } else {
            angle = (float) Math.toRadians(
                    Float.parseFloat(text)
            );
        }
        if (text.contains("S") || text.contains("W")) {
            angle *= -1;
        }
        return angle;
    }
}
