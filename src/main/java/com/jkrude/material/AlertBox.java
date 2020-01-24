package com.jkrude.material;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AlertBox {

  public static void display(String title, String message) {
    Stage window = new Stage();

    //Block events to other windows
    window.initModality(Modality.APPLICATION_MODAL);
    window.setTitle(title);
    window.setMinWidth(250);
    window.setMinHeight(140);

    Label label = new Label();
    label.setText(message);
    Button closeButton = new Button("Close this window");
    closeButton.setOnAction(e -> window.close());

    VBox layout = new VBox(10);
    layout.getChildren().addAll(label, closeButton);
    layout.setAlignment(Pos.CENTER);

    //Display window and wait for it to be closed before returning
    Scene scene = new Scene(layout);
    window.setScene(scene);
    window.showAndWait();
  }

  public static void showAlert(String title, String header, String msg,Alert.AlertType type){
    Alert alert = new Alert(type);
    alert.setTitle(title);
    if(header!=null){
      alert.setHeaderText(header);
    }
    alert.setContentText(msg);
    alert.showAndWait();
  }
}
