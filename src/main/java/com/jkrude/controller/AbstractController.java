package com.jkrude.controller;

import com.jkrude.material.AlertBox;
import com.jkrude.material.Model;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public abstract class AbstractController {

  static protected Model model = new Model();

  protected void goTo(String pathToFxml, ActionEvent actionEvent) {
    Stage stageTheEventSourceNodeBelongs = (Stage) ((Node) actionEvent.getSource()).getScene()
        .getWindow();

    try {
      FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(pathToFxml));
      stageTheEventSourceNodeBelongs.setScene(new Scene(loader.load()));
    } catch (IOException | IllegalStateException e) {
      AlertBox.showAlert("Fatal Error", "Unable to go to page!"," Could not load fxml file.",
          AlertType.ERROR);
      e.printStackTrace();
    } catch (Exception e){
      e.printStackTrace();
      AlertBox.showAlert("Fatal Error", "Unknown source.","",AlertType.ERROR);
    }

  }
}
