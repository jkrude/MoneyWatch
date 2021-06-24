package com.jkrude.controller;

import com.jkrude.UI.AlertBox.AlertBuilder;
import com.jkrude.transaction.TransactionContainer;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Scanner;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class DataView implements FxmlView<DataViewModel>, Prepareable {


  @InjectViewModel
  DataViewModel viewModel;


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
      new AlertBuilder(AlertType.ERROR)
          .setHeader("Import failed.")
          .setMessage("File couldn't not be opened.")
          .showAndWait();
    }
    TransactionContainer transactionContainer;
    try (Scanner sc = new Scanner(file, StandardCharsets.ISO_8859_1)) {

      try {
        transactionContainer = new TransactionContainer(sc);
      } catch (IllegalArgumentException | ParseException e) {
        new AlertBuilder(AlertType.ERROR)
            .setMessage(e.getMessage())
            .showAndWait();
        return;
      }

      viewModel.addTransactionData(transactionContainer);
    } catch (IOException e) {
      new AlertBuilder()
          .setMessage("File could not be opened")
          .showAndWait();
    }
  }
}
