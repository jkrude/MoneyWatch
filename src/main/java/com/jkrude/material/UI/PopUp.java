package com.jkrude.material.UI;

import java.io.IOException;
import java.net.URL;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public interface PopUp {

  static Stage setupStage(FXMLLoader loader, URL fxmlResource) {
    if (fxmlResource != null) {
      loader.setLocation(fxmlResource);
      Parent pane;
      try {
        pane = loader.load();
        Stage stage = new Stage();
        Scene scene = new Scene(pane);
        stage.setScene(scene);
        return stage;
      } catch (IOException e) {
        // TODO
        e.printStackTrace();
        throw new IllegalStateException(e);
      }
    } else {
      throw new IllegalStateException("FXML file was null");
    }
  }
}
