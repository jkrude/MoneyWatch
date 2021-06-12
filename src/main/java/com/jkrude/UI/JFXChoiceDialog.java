package com.jkrude.UI;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import java.net.URL;
import java.util.Optional;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class JFXChoiceDialog<R> {


  @FXML
  private JFXButton defaultBtn;
  @FXML
  private JFXButton cancelBtn;
  @FXML
  private Label header;
  @FXML
  private JFXComboBox<R> comboBox;

  public static class Builder<R> {

    private final Stage stage;
    private final JFXChoiceDialog<R> dialog;
    private R result;

    private static final URL fxmlResource = TransactionTablePopUp.class
        .getResource("/com/jkrude/UI/JFXChoiceDialog.fxml");

    public Builder() {
      FXMLLoader loader = new FXMLLoader();
      this.stage = StageSetter.setupStage(loader, fxmlResource);
      this.dialog = loader.getController();
      this.dialog.defaultBtn.setOnAction(action -> {
        result = dialog.comboBox.getValue();
        stage.close();
      });
      this.dialog.cancelBtn.setOnAction(actionEvent -> stage.close());

      this.stage.setMinHeight(250);
      this.stage.setMinWidth(350);
      this.stage.initModality(Modality.APPLICATION_MODAL);
      this.stage.setAlwaysOnTop(true);
    }

    public Builder<R> setOptions(ObservableList<R> options) {
      dialog.comboBox.setItems(options);
      return this;
    }

    public Builder<R> setDefaultChoice(R defaultChoice) {
      dialog.comboBox.getSelectionModel().select(defaultChoice);
      return this;
    }

    public Builder<R> setStringConverter(StringConverter<R> converter) {
      dialog.comboBox.setConverter(converter);
      return this;
    }

    public Builder<R> setHeader(String header) {
      dialog.header.setText(header);
      return this;
    }

    public Builder<R> setMinWidthHeight(double minWidth, double minHeight) {
      stage.setMinWidth(minWidth);
      stage.setMinHeight(minHeight);
      return this;
    }

    public Optional<R> showAndWait() {
      stage.showAndWait();
      return Optional.ofNullable(result);
    }

  }


}
