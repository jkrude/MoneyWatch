package com.jkrude.material;

import java.util.Optional;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AlertBox {

  public static void displayGeneric(String title, Node node) {
    displayGeneric(title, node, 250, 140);
  }

  public static void displayGeneric(String title, Node node, double minWidth, double minHeight) {
    Stage window = new Stage();
    //Block events to other windows
    window.initModality(Modality.APPLICATION_MODAL);
    window.setTitle(title);
    window.setMinWidth(minWidth);
    window.setMinHeight(minHeight);

    Button closeButton = new Button("Close");
    closeButton.setOnAction(e -> window.close());

    VBox layout = new VBox(10);
    layout.getChildren().addAll(node, closeButton);
    layout.setAlignment(Pos.CENTER);

    //Display window and wait for it to be closed before returning
    Scene scene = new Scene(layout);
    window.setScene(scene);
    window.showAndWait();
  }

  public static class AlertBuilder {

    String title;
    String header;
    String msg;
    AlertType type;
    String cancelButtonText;
    String applyButtonText;
    double minWidth = Double.NaN;
    double minHeight = Double.NaN;
    boolean hasCancelOption = false;
    boolean noApply = false;

    private AlertBuilder() {
    }

    public static AlertBuilder alert(AlertType type) {
      var builder = new AlertBuilder();
      builder.type = type;
      return builder;
    }

    public AlertBuilder setTitle(String title) {
      this.title = title;
      return this;
    }

    public AlertBuilder setHeader(String header) {
      this.header = header;
      return this;
    }

    public AlertBuilder setMessage(String message) {
      this.msg = message;
      return this;
    }

    public AlertBuilder setCancelButtonText(String cancelButtonText) {
      this.cancelButtonText = cancelButtonText;
      this.hasCancelOption = true;
      return this;
    }

    public AlertBuilder setApplyButtonText(String applyButtonText) {
      this.applyButtonText = applyButtonText;
      return this;
    }

    public AlertBuilder setMinWidth(double minWidth) {
      this.minWidth = minWidth;
      return this;
    }

    public AlertBuilder setMinHeight(double minHeight) {
      this.minHeight = minHeight;
      return this;
    }

    public AlertBuilder hasCancelOption() {
      this.hasCancelOption = true;
      return this;
    }

    public AlertBuilder noApplyButton() {
      this.noApply = true;
      return this;
    }

    public Alert build() {

      Alert alert = new Alert(this.type);
      if (this.title != null) {
        alert.setTitle(title);
      }
      if (header != null) {
        alert.setHeaderText(header);
      }
      if (this.msg != null) {
        alert.setContentText(this.msg);
      }
      if (!this.noApply) {
        if (this.applyButtonText != null) {
          alert.getDialogPane().getButtonTypes()
              .add(new ButtonType(this.applyButtonText, ButtonData.APPLY));
        } else {
          alert.getDialogPane().getButtonTypes().add(ButtonType.APPLY);
        }
      }
      if (this.hasCancelOption) {
        if (this.cancelButtonText != null) {
          alert.getDialogPane().getButtonTypes()
              .add(new ButtonType(this.cancelButtonText, ButtonData.CANCEL_CLOSE));
        } else {
          alert.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        }
      }
      if (!Double.isNaN(this.minHeight)) {
        alert.getDialogPane().setMinHeight(this.minHeight);
      }
      if (!Double.isNaN(this.minWidth)) {
        alert.getDialogPane().setMinWidth(this.minWidth);
      }
      return alert;
    }

    public Optional<ButtonType> buildAndShow() {
      return this.build().showAndWait();
    }
  }

}
