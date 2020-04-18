package com.jkrude.controller;

import com.jkrude.material.AlertBox;
import com.jkrude.material.Model;
import com.jkrude.material.Model.ScnCntrlPair;
import com.jkrude.material.PersistenceManager;
import com.jkrude.material.Profile;
import java.io.IOException;
import java.net.URL;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public abstract class ParentController {

  static protected Model model = new Model();
  static private boolean wasInitialised = false;
  protected static final URL startScene = ParentController.class
      .getResource("/MainScene/start.fxml");
  protected static final URL lineChartScene = ParentController.class
      .getResource("/MainScene/lineChart.fxml");
  protected static final URL pieChartScene = ParentController.class
      .getResource("/MainScene/pieChart.fxml");
  protected static final URL categoryEditor = ParentController.class
      .getResource("/MainScene/categoryEditor.fxml");

  protected abstract void checkIntegrity();

  public static void init(Stage primaryStage) {
    if (wasInitialised) {
      throw new IllegalStateException();
    } else {
      // Get preset Categories form TestData
      loadConfig();

      URL[] initialScenes = {startScene, lineChartScene, pieChartScene, categoryEditor};
      for (URL fxmlURL : initialScenes) {
        try {
          // Load Scene with FXMLLoader.
          FXMLLoader fxmlLoader = new FXMLLoader(fxmlURL);
          Scene scene = new Scene(fxmlLoader.load());
          // Save the controller and scene.
          ParentController controller = fxmlLoader.getController();
          model.getLoadedFxmlFiles().put(fxmlURL, new ScnCntrlPair(scene, controller));

        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      // Set currScene to startScene.
      model.setCurrScenePair(model.getLoadedFxmlFiles().get(startScene));
      primaryStage.setTitle("Money Watch");
      primaryStage.setScene(model.getLoadedFxmlFiles().get(startScene).getScene());
      primaryStage.show();
      wasInitialised = true;
    }
  }

  protected static void goTo(URL url, ActionEvent actionEvent) {
    // Get the stage for the actionEvent
    Stage stage = getStageForActionEvent(actionEvent);
    try {
      if (model.getCurrScenePair() == null) {
        throw new IllegalStateException();
      }

      Model.ScnCntrlPair scnCntrlPair = model.getLoadedFxmlFiles().get(url);
      // Prepare scene.
      scnCntrlPair.getController().checkIntegrity();
      // Switch to scene.
      stage.setScene(scnCntrlPair.getScene());

      model.getLastSceneStack().push(model.getCurrScenePair());
      model.setCurrScenePair(scnCntrlPair);
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
      Model.ScnCntrlPair lastScenePair = model.getLastSceneStack().pop();
      //TODO integrityCheck necessary?
      lastScenePair.getController().checkIntegrity();
      stage.setScene(lastScenePair.getScene());
      model.setCurrScenePair(lastScenePair);
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
    model.setProfile(new Profile());
    PersistenceManager.load(model);
  }
  public static void stop() {
    PersistenceManager.save(model);
  }
}
