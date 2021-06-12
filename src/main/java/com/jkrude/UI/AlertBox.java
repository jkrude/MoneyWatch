package com.jkrude.UI;

import com.jfoenix.controls.JFXButton;
import java.net.URL;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AlertBox {

  private static final SVGPath warningPath;
  private static final SVGPath errorPath;
  private static final SVGPath informationPath;
  private static final SVGPath successPath;


  static {
    warningPath = new SVGPath();
    warningPath.setFill(Paint.valueOf("#ffca7e"));
    warningPath.setContent(
        "M12 5.99L19.53 19H4.47L12 5.99M12 2L1 21h22L12 2zm1 14h-2v2h2v-2zm0-6h-2v4h2v-4z");

    errorPath = new SVGPath();
    errorPath.setFill(Paint.valueOf("#f45347"));
    errorPath.setContent(
        "M11 15h2v2h-2zm0-8h2v6h-2zm.99-5C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8z");

    informationPath = new SVGPath();
    informationPath.setFill(Paint.valueOf("#349ff3"));
    informationPath.setContent(
        "M11 7h2v2h-2zm0 4h2v6h-2zm1-9C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z");

    successPath = new SVGPath();
    successPath.setFill(Paint.valueOf("#62b966"));
    successPath.setContent(
        "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm4.59-12.42L10 14.17l-2.59-2.58L6 13l4 4 8-8z");
  }

  @FXML
  private JFXButton defaultBtn;
  @FXML
  private Label headerBtn;
  @FXML
  private Label messageBtn;
  @FXML
  private SVGPath graphic;


  public void setType(AlertType alertType) {
    switch (alertType) {
      case NONE:
        // Use same as information
      case INFORMATION:
        graphic.setContent(informationPath.getContent());
        graphic.setFill(informationPath.getFill());
        break;
      case WARNING:
        graphic.setContent(warningPath.getContent());
        graphic.setFill(warningPath.getFill());
        break;
      case CONFIRMATION:
        graphic.setContent(successPath.getContent());
        graphic.setFill(successPath.getFill());
        break;
      case ERROR:
        graphic.setContent(errorPath.getContent());
        graphic.setFill(errorPath.getFill());
        break;
    }

  }

  public static class AlertBuilder {

    private static final URL fxmlResource = TransactionTablePopUp.class
        .getResource("/com/jkrude/UI/AlertBuilder.fxml");

    private final Stage stage;
    private final AlertBox alert;


    public AlertBuilder(AlertType alertType) {
      FXMLLoader loader = new FXMLLoader();
      this.stage = StageSetter.setupStage(loader, fxmlResource);
      this.alert = loader.getController();
      this.alert.defaultBtn.setOnAction(action -> stage.close());
      this.alert.setType(alertType);
      this.stage.setMinHeight(320);
      this.stage.setMinWidth(500);
      this.stage.initModality(Modality.WINDOW_MODAL);
      this.stage.setAlwaysOnTop(true);
    }

    public AlertBuilder() {
      this(AlertType.WARNING);
    }

    public AlertBuilder setMessage(String message) {
      alert.messageBtn.setText(message);
      return this;
    }

    public AlertBuilder setHeader(String heading) {
      alert.headerBtn.setText(heading);
      return this;
    }

    public AlertBuilder setDefaultButtonText(String text) {
      alert.defaultBtn.setText(text);
      return this;
    }

    public AlertBuilder setMinWidthHeight(double minWidth, double minHeight) {
      stage.setMinWidth(minWidth);
      stage.setMinHeight(minHeight);
      return this;
    }

    public void showAndWait() {
      stage.showAndWait();
    }

    public static void displayGeneric(String title, Node node) {
      displayGeneric(title, node, 250, 140);
    }

    public static void displayGeneric(String title, Node node, double minWidth, double minHeight) {
      Stage window = new Stage();
      // Block events to other windows
      window.initModality(Modality.APPLICATION_MODAL);
      window.setTitle(title);
      window.setMinWidth(minWidth);
      window.setMinHeight(minHeight);

      JFXButton closeButton = new JFXButton("Close");
      closeButton.setOnAction(e -> window.close());

      VBox layout = new VBox(10);
      layout.getChildren().addAll(node, closeButton);
      layout.setAlignment(Pos.CENTER);

      // Display window and wait for it to be closed before returning
      Scene scene = new Scene(layout);
      scene.getStylesheets().add("css/base.css");
      window.setScene(scene);
      window.showAndWait();
    }
  }
}