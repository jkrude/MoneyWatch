package com.jkrude.UI;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXColorPicker;
import com.jfoenix.controls.JFXTextField;
import com.jkrude.category.CategoryNode;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class NewCategoryDialog implements Initializable {


  @FXML
  private JFXColorPicker picker;
  @FXML
  private JFXTextField inputField;
  @FXML
  private JFXButton applyBtn;
  @FXML
  private JFXButton cancelBtn;

  private boolean wasApplied;


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    applyBtn.disableProperty().bind(inputField.textProperty().isEmpty());
  }

  public static Optional<CategoryNode> showAndGet() {
    URL fxmlResource = TransactionTablePopUp.class
        .getResource("/com/jkrude/UI/NewCategoryDialog.fxml");

    FXMLLoader loader = new FXMLLoader();
    Stage stage = StageSetter.setupStage(loader, fxmlResource);
    stage.setResizable(false);
    stage.setTitle("Create a new category");
    NewCategoryDialog controller = loader.getController();

    controller.applyBtn.setOnAction(action -> {
      controller.wasApplied = true;
      stage.close();
    });
    controller.cancelBtn.setOnAction(action -> stage.close());

    stage.showAndWait();

    Color color = controller.picker.getValue();
    String name = controller.inputField.getText();
    if (!controller.wasApplied || name == null || name.isBlank() || color == null) {
      return Optional.empty();
    }
    CategoryNode categoryNode = new CategoryNode(name);
    categoryNode.setColor(color);
    return Optional.of(categoryNode);
  }

}
