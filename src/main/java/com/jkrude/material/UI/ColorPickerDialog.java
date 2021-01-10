package com.jkrude.material.UI;

import java.util.Optional;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Dialog;
import javafx.scene.paint.Color;

public class ColorPickerDialog {

  public static Optional<Color> showAndWait() {
    Dialog<Color> colorPickerDialog = new Dialog<>();
    ColorPicker picker = new ColorPicker(Color.DEEPPINK);
    colorPickerDialog.setTitle("Pick a color");
    var pane = colorPickerDialog.getDialogPane();
    pane.getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);
    pane.setPadding(new Insets(32, 32, 4, 32));
    pane.setMinWidth(70);
    pane.setMinHeight(70);
    picker.prefWidthProperty().bind(pane.widthProperty());
    picker.prefHeightProperty().bind(pane.heightProperty());
    pane.setContent(picker);
    colorPickerDialog.setResizable(true);
    colorPickerDialog.setResultConverter(buttonType -> {
      if (buttonType == ButtonType.APPLY) {
        return picker.getValue();
      }
      return null;
    });
    return colorPickerDialog.showAndWait();
  }
}
