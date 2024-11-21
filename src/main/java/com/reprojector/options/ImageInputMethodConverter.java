package com.reprojector.options;

import javafx.util.StringConverter;
import java.util.ResourceBundle;

public class ImageInputMethodConverter extends StringConverter<ImageInputMethod> {
    @Override
    public String toString(ImageInputMethod mapProjection) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("labels/Display");
        return resourceBundle.getString(mapProjection.name());
    }

    @Override
    public ImageInputMethod fromString(String s) {
        return null;
    }
}
