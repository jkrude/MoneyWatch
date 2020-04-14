package com.jkrude.material.UI;

import com.jkrude.material.AlertBox;
import com.jkrude.material.Camt;
import com.jkrude.material.Camt.ListType;
import com.jkrude.material.Rule;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;

public class RuleDialog {

  @FXML
  private ChoiceBox<Camt.ListType> defaultCB;
  @FXML
  private ChoiceBox<ListType> firstAndCB;
  @FXML
  private TextField defaultTF;
  @FXML
  private ToggleButton firstAndBtn;
  @FXML
  private TextField firstAndTF;
  @FXML
  private ChoiceBox<ListType> secondAndCB;
  @FXML
  private ToggleButton secondAndBtn;
  @FXML
  private TextField secondAndTF;
  @FXML
  private Button addRuleBtn;

  public static final URL fxmlResource = RuleDialog.class.getClassLoader()
      .getResource("ruleDialog.fxml");

  @FXML
  private void initialize() {
    ObservableList<ListType> typeChoiceList = FXCollections
        .observableArrayList(Camt.ListType.values());
    defaultCB.setItems(typeChoiceList);

    // The n+1th entries will be activated if the n-th and was pressed
    // Therefore the n+1th are bound to th n-th toggle-button
    firstAndCB.setItems(typeChoiceList);
    addListenerForCB(firstAndCB, defaultCB);
    firstAndCB.visibleProperty().bind(firstAndBtn.selectedProperty());
    firstAndTF.visibleProperty().bind(firstAndBtn.selectedProperty());

    secondAndBtn.visibleProperty().bind(firstAndBtn.selectedProperty());
    addListenerForCB(secondAndCB, firstAndCB);
    secondAndCB.visibleProperty().bind(secondAndBtn.selectedProperty());
    secondAndTF.visibleProperty().bind(secondAndBtn.selectedProperty());
  }

  private static Stage setup(FXMLLoader loader) {
    if (fxmlResource != null) {
      Parent pane;
      try {
        pane = loader.load();
        Stage stage = new Stage();
        Scene scene = new Scene(pane);
        stage.setScene(scene);
        return stage;
      } catch (IOException e) {
        // TODO
        e.printStackTrace();
        throw new IllegalStateException(e);
      }
    } else {
      throw new IllegalStateException("FXML file was null");
    }
  }

  public static void show(final Consumer<Rule> callback) {
    FXMLLoader loader = new FXMLLoader(fxmlResource);
    Stage stage = setup(loader);
    RuleDialog controller = loader.getController();
    controller.addRuleBtn.setOnAction(event -> {
      callback.accept(controller.addRule());
      stage.close();
    });
    stage.showAndWait();
  }

  public static void showAndEdit(final Consumer<Rule> callback,
      Iterator<Entry<ListType, String>> entryIterator) {
    FXMLLoader loader = new FXMLLoader(fxmlResource);
    Stage stage = setup(loader);
    RuleDialog controller = loader.getController();
    controller.setupForEditing(entryIterator);

    controller.addRuleBtn.setOnAction(event -> {
      stage.close();
      callback.accept(controller.addRule());
    });
    stage.showAndWait();
  }

  private void setupForEditing(Iterator<Entry<ListType, String>> entryIterator) {
    if (entryIterator.hasNext()) {
      var entry = entryIterator.next();
      defaultCB.getSelectionModel().select(entry.getKey());
      defaultTF.setText(entry.getValue());
    }
    if (entryIterator.hasNext()) {
      firstAndBtn.setSelected(true);
      var entry = entryIterator.next();
      firstAndCB.getSelectionModel().select(entry.getKey());
      firstAndTF.setText(entry.getValue());
    }
    if (entryIterator.hasNext()) {
      secondAndBtn.setSelected(true);
      var entry = entryIterator.next();
      secondAndCB.getSelectionModel().select(entry.getKey());
      secondAndTF.setText(entry.getValue());
    }
    addRuleBtn.setText("Bestätigen");
  }

  private static void addListenerForCB(ChoiceBox<ListType> observingChoiceBox,
      ChoiceBox<ListType> observedChoiceBox) {
    observedChoiceBox.getSelectionModel().selectedItemProperty().addListener(
        (ObservableValue<? extends ListType> obsValue, ListType oldV, ListType newV) -> {
          observingChoiceBox
              .setItems(observedChoiceBox.getItems().filtered(type1 -> !type1.equals(newV)));
        });
  }

  @FXML
  private Rule addRule() {
    //TODO
    // Error if entry already in List -> equals method necessary in PieCategory.Entry
    // Input validation
    try {
      Map<ListType, String> inputMap = new HashMap<>();
      if (!defaultCB.getSelectionModel().isEmpty()) {
        inputMap.put(defaultCB.getSelectionModel().getSelectedItem(), defaultTF.getText());
      }
      if (firstAndBtn.isSelected() && !firstAndCB.getSelectionModel().isEmpty()) {
        inputMap.put(firstAndCB.getSelectionModel().getSelectedItem(), firstAndTF.getText());
        if (secondAndBtn.isSelected() && !secondAndCB.getSelectionModel().isEmpty()) {
          inputMap.put(secondAndCB.getSelectionModel().getSelectedItem(), secondAndTF.getText());
        }
        firstAndBtn.setSelected(false);
        secondAndBtn.setSelected(false);
      }

      if (inputMap.isEmpty()) {
        AlertBox.showAlert("Fehlende Eingabe", "Kein Typ ausgewählt", "", AlertType.WARNING);
        return null;
      } else {
        return Rule.RuleFactory.generate(inputMap, "");
      }
    } catch (ParseException e) {
      e.printStackTrace();
      AlertBox.showAlert("Fehlerhafte Eingabe", "Das Datum war nicht im Format mm.dd.yy",
          "Eingabe: " + defaultTF
              .getText(),
          AlertType.ERROR);
      return null;
    }
  }

}
