package com.reprojector.options;

import javafx.util.StringConverter;
import java.util.ResourceBundle;

public class MapProjectionConverter extends StringConverter<MapProjection> {
    @Override
    public String toString(MapProjection mapProjection) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("labels/Display");
        return resourceBundle.getString(mapProjection.name());
    }

    @Override
    public MapProjection fromString(String s) {
        return null;
    }
}
