package com.jkrude.UI;

import java.io.IOException;
import java.net.URL;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class StageSetter {

  public static Stage setupStage(FXMLLoader loader, URL fxmlResource) {
    loader.setLocation(fxmlResource);
    Parent parent;
    try {
      parent = loader.load();
      Stage stage = new Stage();
      Scene scene = new Scene(parent);
      stage.setScene(scene);
      return stage;
    } catch (IOException e) {
      // TODO
      e.printStackTrace();
      throw new IllegalStateException(e);
    }
  }
}
