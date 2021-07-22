package com.jkrude.controller;

import com.jfoenix.controls.JFXButton;
import com.jkrude.UI.AlertBox.AlertBuilder;
import com.jkrude.UI.TransactionTableView;
import com.jkrude.transaction.TransactionContainer;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Scanner;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class DataView implements FxmlView<DataViewModel>, Prepareable, Initializable {


  @FXML
  private Label optionsLabel;
  @FXML
  private JFXButton addBtn;
  @FXML
  private HBox bankOptions;
  @FXML
  private Accordion datasetAccordion;

  @InjectViewModel
  private DataViewModel viewModel;
  private final Map<TransactionContainer, TitledPane> dataMap;

  public DataView() {
    dataMap = new HashMap<>();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    addBtn.managedProperty().bind(addBtn.visibleProperty());
    addBtn.visibleProperty().bind(bankOptions.visibleProperty().not());
    addBtn.setOnAction(action -> {
      bankOptions.setVisible(true);
    });
    addBtn.contentDisplayProperty().bind(Bindings.when(viewModel.hasNoActiveDataProperty()).then(
        ContentDisplay.BOTTOM).otherwise(ContentDisplay.GRAPHIC_ONLY));
    bankOptions.managedProperty().bind(bankOptions.visibleProperty());
    optionsLabel.visibleProperty().bind(bankOptions.visibleProperty());
    optionsLabel.managedProperty().bind(optionsLabel.visibleProperty());

    datasetAccordion.managedProperty().bind(datasetAccordion.visibleProperty());
    datasetAccordion.visibleProperty().bind(viewModel.hasNoActiveDataProperty().not());
    // Could be any control instead off addBtn
    addBtn.getParent().setOnKeyPressed(keyEvent -> {
      if (keyEvent.getCode() == KeyCode.ESCAPE) {
        bankOptions.setVisible(false);
      }
    });

  }

  @Override
  public void prepare() { // No preparation necessary
    for (var container : viewModel.getTransactionContainer()) {
      if (!dataMap.containsKey(container)) {
        addDataset(container);
      }
    }
    //TODO check for removal too
  }

  private void addDataset(TransactionContainer container) {
    TitledPane pane = new TitledPane();
    try {
      FXMLLoader loader = new FXMLLoader(
          getClass().getResource("/com/jkrude/UI/TransactionTableView.fxml"));
      Parent parent = loader.load();
      TransactionTableView controller = loader.getController();
      pane.setContent(parent);
      controller.itemsProperty().set(container.getSource());
      pane.setText(container.getName());
      datasetAccordion.getPanes().add(pane);
      dataMap.put(container, pane);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void removeDataset(TransactionContainer container) {
    datasetAccordion.getPanes().remove(dataMap.get(container));
    dataMap.remove(container);
  }

  @FXML
  private void importNewContainer() {
    Optional<TransactionContainer> optContainer = loadFile();
    bankOptions.setVisible(false);
    optContainer.ifPresent(container -> {
      viewModel.addTransactionData(container);
      addDataset(container);
    });
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

  public Optional<TransactionContainer> loadFile() {
    File file = openFileBrowser();
    if (file == null) {
      // No file selected. Dialog was canceled.
      return Optional.empty();
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
        return Optional.of(transactionContainer);
      } catch (IllegalArgumentException | ParseException e) {
        new AlertBuilder(AlertType.ERROR)
            .setMessage(e.getMessage())
            .showAndWait();
        return Optional.empty();
      }
    } catch (IOException e) {
      new AlertBuilder()
          .setMessage("File could not be opened")
          .showAndWait();
    }
    return Optional.empty();
  }
}
