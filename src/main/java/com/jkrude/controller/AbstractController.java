package com.jkrude.controller;

import com.jkrude.material.AlertBox;
import com.jkrude.material.Model;
import com.jkrude.material.Model.MapValue;
import com.jkrude.test.TestData;
import java.io.IOException;
import java.net.URL;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public abstract class AbstractController {

  static protected Model model = new Model();
  static private boolean wasInitialised = false;

  protected abstract void checkIntegrity();

  public static void init(Stage primaryStage) {
    if (wasInitialised) {
      throw new IllegalStateException();
    } else {
      // Get preset Categories form TestData
      loadConfig();

      String[] fxmlFiles = {"startScene", "lineChartScene", "pieChartScene", "categoryEditor"};
      for (String fxmlFile : fxmlFiles) {
        URL fxmlURL;
        try {
          // Load Scene with FXMLLoader.
          fxmlURL = AbstractController.class.getClassLoader().getResource(fxmlFile + ".fxml");
          FXMLLoader fxmlLoader = new FXMLLoader(fxmlURL);
          Scene scene = new Scene(fxmlLoader.load());
          // Save the controller and scene.
          AbstractController controller = fxmlLoader.getController();
          model.getLoadedFxmlFiles().put(fxmlFile, new MapValue(scene, controller));

        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      // Set currScene to startScene.
      model.setCurrScene(model.getLoadedFxmlFiles().get("startScene").getScene());
      primaryStage.setTitle("Money Watch");
      primaryStage.setScene(model.getLoadedFxmlFiles().get("startScene").getScene());
      primaryStage.show();
      wasInitialised = true;
    }
  }

  protected static void goTo(String pathToFxml, ActionEvent actionEvent) {
    // Get the stage for the actionEvent
    Stage stage = getStageForActionEvent(actionEvent);
    try {
      if (model.getCurrScene() == null) {
        throw new IllegalStateException();
      }

      Model.MapValue mapValue = model.getLoadedFxmlFiles().get(pathToFxml);
      // Prepare scene.
      mapValue.getController().checkIntegrity();
      // Switch to scene.
      stage.setScene(mapValue.getScene());

      model.getLastSceneStack().push(model.getCurrScene());
      model.setCurrScene(mapValue.getScene());
    } catch (IllegalStateException e) {
      AlertBox.showAlert("Fatal Error", "Unable to go to page!", " Could not load fxml file.",
          AlertType.ERROR);
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
      AlertBox.showAlert("Fatal Error", "Unknown source.", "", AlertType.ERROR);
    }
  }

  protected static void goBack(ActionEvent actionEvent) {
    Stage stage = getStageForActionEvent(actionEvent);
    try {
      Scene lastScene = model.getLastSceneStack().pop();
      //TODO integrityCheck necessary?
      stage.setScene(lastScene);
      model.setCurrScene(lastScene);
    } catch (IllegalStateException e) {
      AlertBox.showAlert("Fatal Error", "Unable to go to page!", " Could not load fxml file.",
          AlertType.ERROR);
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
      AlertBox.showAlert("Fatal Error", "Unknown source.", "", AlertType.ERROR);
    }
  }

  public static Stage getStageForActionEvent(ActionEvent event) {
    return (Stage) ((Node) event.getSource()).getScene()
        .getWindow();
  }

  private static void loadConfig() {
    model.setProfile(TestData.getProfile());
  }
}
