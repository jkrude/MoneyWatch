package com.jkrude.UI;

import com.jfoenix.controls.JFXButton;
import com.jkrude.main.Main;
import com.jkrude.main.Main.UsableScene;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class NavigationRail {


  @FXML
  private JFXButton sunburst;
  @FXML
  private JFXButton timeline;
  @FXML
  private JFXButton categoryEditor;
  @FXML
  private JFXButton data;

  public void setCurrent(UsableScene current) {
    switch (current) {
      case DATA:
        data.setDisable(true);
        break;
      case TIMELINE:
        timeline.setDisable(true);
        break;
      case SUNBURST:
        sunburst.setDisable(true);
        break;
      case CATEGORY_EDITOR:
        categoryEditor.setDisable(true);
        break;
    }
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
