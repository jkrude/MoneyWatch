package com.jkrude.controller;

import com.jkrude.main.Main;
import com.jkrude.main.Main.UsableScene;
import com.jkrude.material.AlertBox.AlertBuilder;
import com.jkrude.transaction.TransactionContainer;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ResourceBundle;
import java.util.Scanner;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class StartView implements FxmlView<StartViewModel>, Initializable, Prepareable {


  @FXML
  private Button goToSunburstBtn;
  @FXML
  private Button goToTimeLineBtn;

  @InjectViewModel
  StartViewModel viewModel;


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

    goToSunburstBtn.disableProperty()
        .bind(viewModel.hasNoActiveDataProperty());
    goToTimeLineBtn.disableProperty()
        .bind(viewModel.hasNoActiveDataProperty());
  }

  @Override
  public void prepare() { // No preparation necessary.
  }

  public File openFileBrowser() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("CSV import");
    fileChooser.getExtensionFilters().add(
        new ExtensionFilter("CSV", "*.CSV"));
    fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
    return fileChooser
        .showOpenDialog(new Stage());
  }

  public void loadFile() {
    File file = openFileBrowser();
    if (file == null) {
      // No file selected. Dialog was canceled.
      return;
    } else if (!file.canRead()) {
      AlertBuilder.alert(AlertType.ERROR)
          .setTitle("Error")
          .setHeader("Import failed.")
          .setMessage("File couldn't not be opened.")
          .buildAndShow();
    }
    TransactionContainer transactionContainer;
    try (Scanner sc = new Scanner(file, StandardCharsets.ISO_8859_1)) {

      try {
        transactionContainer = new TransactionContainer(sc);
      } catch (IllegalArgumentException | ParseException e) {
        AlertBuilder.alert(AlertType.ERROR)
            .setTitle("Error")
            .setMessage(e.getMessage())
            .buildAndShow();
        return;
      }

      viewModel.addTransactionData(transactionContainer);
    } catch (IOException e) {
      AlertBuilder.alert(AlertType.ERROR)
          .setTitle("Error")
          .setMessage("File could not be opened")
          .buildAndShow();
    }
  }

  @FXML
  private void goToTimeLine() {
    Main.goTo(UsableScene.TIMELINE);
  }

  @FXML
  private void goToSunburst() {
    Main.goTo(UsableScene.SUNBURST);
  }

  @FXML
  private void goToCategoryEditor() {
    Main.goTo(UsableScene.CATEGORY_EDITOR);
  }
}
