package com.jkrude.material;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AlertBox {
  
  public static void showAlert(String title, String header, String msg,Alert.AlertType type){
    Alert alert = new Alert(type);
    alert.setTitle(title);
    if(header!=null){
      alert.setHeaderText(header);
    }
    alert.setContentText(msg);
    alert.showAndWait();
  }

  public static void displayGeneric(String title, Node node) {
    displayGeneric(title, node, 250,140);
  }

  public static void displayGeneric(String title, Node node, int minWidth, int minHeight){
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
}
