package com.jkrude.UI;

import com.jfoenix.controls.JFXButton;
import com.jkrude.main.Main;
import com.jkrude.main.Main.UsableScene;
import com.jkrude.material.Model;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

public class NavigationRail implements Initializable {


  @FXML
  private JFXButton sunburst;
  @FXML
  private JFXButton timeline;
  @FXML
  private JFXButton categoryEditor;
  @FXML
  private JFXButton data;
  private UsableScene current;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    // Active charts if data was set.
    Model.getInstance().activeDataProperty().addListener(
        (observableValue, oldV, newV) -> {
          if (newV == null) {
            sunburst.setDisable(true);
            timeline.setDisable(true);
          } else {
            // Only activate if not already shown.
            if (current != UsableScene.SUNBURST) {
              sunburst.setDisable(false);
            }
            if (current != UsableScene.TIMELINE) {
              timeline.setDisable(false);
            }
          }
        });
    // Initially for data.
    if (Model.getInstance().activeDataProperty().isNull().get()) {
      sunburst.setDisable(true);
      timeline.setDisable(true);
    }
  }

  public void setCurrent(UsableScene next) {
    if (current != null) {
      getButton(current).setDisable(false);
    }
    this.current = next;
    getButton(next).setDisable(true);

  }

  private JFXButton getButton(UsableScene scene) {
    switch (scene) {
      case DATA:
        return data;
      case TIMELINE:
        return timeline;
      case SUNBURST:
        return sunburst;
      case CATEGORY_EDITOR:
        return categoryEditor;
    }
    throw new IllegalArgumentException("Passed unknown scene: " + scene.name());
  }

  @FXML
  private void goToSunburst(ActionEvent actionEvent) {
    Main.goTo(UsableScene.SUNBURST);
  }

  @FXML
  private void goToTimeLine(ActionEvent actionEvent) {
    Main.goTo(UsableScene.TIMELINE);
  }

  @FXML
  private void goToCategoryEditor(ActionEvent actionEvent) {
    Main.goTo(UsableScene.CATEGORY_EDITOR);
  }

  @FXML
  private void goToImport(ActionEvent actionEvent) {
    Main.goTo(UsableScene.DATA);
  }
}
