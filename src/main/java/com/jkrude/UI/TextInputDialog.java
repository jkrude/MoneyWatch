package com.jkrude.UI;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import java.net.URL;
import java.util.Optional;
import java.util.function.Consumer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class TextInputDialog {

  @FXML
  private Label header;
  @FXML
  private JFXTextField inputField;
  @FXML
  private JFXButton applyBtn;
  @FXML
  private JFXButton cancelBtn;

  private Consumer<String> onApply;
  private Runnable onCancel;
  private String result;

  private void onApply(Stage parent) {
    parent.close();
    result = inputField.getText();
    if (onApply != null) {
      onApply.accept(result);
    }
  }

  private void onCancel(Stage parent) {
    parent.close();
    if (onCancel != null) {
      onCancel.run();
    }
  }


  public static class Builder {

    private final TextInputDialog textInputDialog;
    private final Stage stage;
    private static final URL fxmlResource = TransactionTablePopUp.class
        .getResource("/com/jkrude/UI/TextInputDialog.fxml");

    public Builder(String title) {
      FXMLLoader loader = new FXMLLoader();
      this.stage = StageSetter.setupStage(loader, fxmlResource);
      this.stage.setTitle(title);
      textInputDialog = loader.getController();
      textInputDialog.header.setText(title);
      textInputDialog.applyBtn.setOnAction(action -> textInputDialog.onApply(stage));
      textInputDialog.cancelBtn.setOnAction(actionEvent -> textInputDialog.onCancel(stage));
      // Close on ESC and confirm on Enter
      textInputDialog.inputField.setOnKeyReleased(keyEvent -> {
        if (keyEvent.getCode() == KeyCode.ENTER) {
          textInputDialog.onApply(stage);
        }
      });
      stage.getScene().setOnKeyReleased(keyEvent -> {
        if (keyEvent.getCode() == KeyCode.ESCAPE) {
          textInputDialog.onCancel(stage);
        }
      });
    }

    public Builder setHint(String hint) {
      textInputDialog.inputField.setPromptText(hint);
      return this;
    }

    public Builder setText(String text) {
      textInputDialog.inputField.setText(text);
      return this;
    }

    public Builder setOnApply(Consumer<String> onApply) {
      textInputDialog.onApply = onApply;
      return this;
    }

    public Builder setOnCancel(Runnable onCancel) {
      textInputDialog.onCancel = onCancel;
      return this;
    }

    public void showAndWait() {
      this.stage.showAndWait();
    }

    public Optional<String> showAndGet() {
      this.stage.showAndWait();
      return Optional.ofNullable(textInputDialog.result);
    }
  }

}
