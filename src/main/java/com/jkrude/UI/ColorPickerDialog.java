package com.jkrude.UI;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXColorPicker;
import com.jfoenix.controls.JFXDialogLayout;
import java.util.Optional;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ColorPickerDialog {

  private Stage stage;
  private JFXDialogLayout layout;
  private JFXColorPicker picker;
  private JFXButton cancelBtn;
  private JFXButton applyBtn;

  private Label header;

  private Color color;

  public ColorPickerDialog(Color defaultColor) {
    this();
    picker.setValue(defaultColor);
  }

  public ColorPickerDialog() {

    stage = new Stage();
    layout = new JFXDialogLayout();
    picker = new JFXColorPicker();
    cancelBtn = new JFXButton("Cancel");
    applyBtn = new JFXButton("Apply");
    header = new Label("Select color.");

    init();
    style();
  }

  private void init() {
    stage.initModality(Modality.APPLICATION_MODAL);
    stage.setAlwaysOnTop(true);
    stage.setWidth(300);
    stage.setHeight(200);
    stage.setTitle("Color picker");
    stage.setResizable(false);
    stage.setScene(new Scene(layout));
    layout.setHeading(header);
    layout.setBody(picker);
    layout.setActions(cancelBtn, applyBtn);
    cancelBtn.setOnAction(action -> close());
    applyBtn.setOnAction(actionEvent -> {
      color = picker.getValue();
      close();
    });
  }

  private void style() {
    layout.getStylesheets().add("css/base.css");
    picker.setStyle("-fx-background-color: -fx-def-background");
    layout.setStyle("-fx-background-color: -fx-def-background");
    header.setStyle("-fx-text-fill: white");
    cancelBtn.setStyle("-fx-background-color: -fx-def-background");
    cancelBtn.setStyle("-fx-text-fill: white");
    applyBtn.setStyle("-fx-background-color: -fx-def-background");
    applyBtn.setStyle("-fx-text-fill: white");

  }


  public Optional<Color> showAndWait() {
    stage.showAndWait();
    return Optional.ofNullable(color);
  }

  private void close() {
    stage.close();
  }

}
