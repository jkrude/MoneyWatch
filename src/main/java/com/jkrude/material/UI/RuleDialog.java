package com.jkrude.material.UI;

import com.jkrude.material.AlertBox;
import com.jkrude.material.Camt;
import com.jkrude.material.Camt.CamtEntry;
import com.jkrude.material.Camt.ListType;
import com.jkrude.material.Rule;
import com.jkrude.material.Rule.RuleFactory;
import java.net.URL;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;

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

  public static final URL fxmlResource = RuleDialog.class
      .getResource("/PopUp/ruleDialog.fxml");

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

  public static void show(final Consumer<Rule> callback) {
    FXMLLoader loader = new FXMLLoader(fxmlResource);
    Stage stage = PopUp.setupStage(loader, fxmlResource);
    RuleDialog controller = loader.getController();
    controller.addRuleBtn.setOnAction(event -> {
      callback.accept(controller.addRule());
      stage.close();
    });
    stage.showAndWait();
  }

  public static void showAndEdit(final Consumer<Rule> callback,
      Iterator<Pair<ListType, String>> entryIterator) {
    FXMLLoader loader = new FXMLLoader(fxmlResource);
    Stage stage = PopUp.setupStage(loader, fxmlResource);
    RuleDialog controller = loader.getController();
    controller.setupForEditing(entryIterator);

    controller.addRuleBtn.setOnAction(event -> {
      callback.accept(controller.addRule());
      stage.close();
    });
    stage.showAndWait();
  }

  private void setupForEditing(Iterator<Pair<ListType, String>> entryIterator) {
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
        (ObservableValue<? extends ListType> obsValue, ListType oldV, ListType newV) -> observingChoiceBox
            .setItems(observedChoiceBox.getItems().filtered(type1 -> !type1.equals(newV))));
  }

  public static Optional<Set<ListType>> chooseRelevantField(CamtEntry camtEntry) {
    Dialog<Set<ListType>> dialog = new Dialog<>();
    dialog.setWidth(200);
    dialog.setWidth(600);
    VBox vBox = new VBox();
    vBox.setSpacing(16);
    Set<ListType> results = new HashSet<>();
    for (ListType l : Camt.ListType.values()) {
      CheckBox c = new CheckBox(l.getTranslation());
      c.selectedProperty().addListener(
          ((observableValue, oldValue, newValue) -> {
            if (newValue) { // if selected
              results.add(l);
            } else {
              results.remove(l);
            }
          })
      );
      vBox.getChildren().add(c);
    }
    dialog.getDialogPane().setContent(vBox);
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);
    dialog.setResultConverter(
        callback -> {
          if (callback.getButtonData().isCancelButton()) {
            return null;
          } else {
            return results;
          }
        }
    );
    return dialog.showAndWait();
  }


  @FXML
  private Rule addRule() {
    // Input validation and other checks should be done in the calling method.
    try {
      Set<Pair<ListType, String>> inputIdentifier = new HashSet<>();
      if (!defaultCB.getSelectionModel().isEmpty()) {
        inputIdentifier
            .add(new Pair<>(defaultCB.getSelectionModel().getSelectedItem(), defaultTF.getText()));
      }
      if (firstAndBtn.isSelected() && !firstAndCB.getSelectionModel().isEmpty()) {
        inputIdentifier.add(
            new Pair<>(firstAndCB.getSelectionModel().getSelectedItem(), firstAndTF.getText()));
        if (secondAndBtn.isSelected() && !secondAndCB.getSelectionModel().isEmpty()) {
          inputIdentifier.add(
              new Pair<>(secondAndCB.getSelectionModel().getSelectedItem(), secondAndTF.getText()));
        }
        firstAndBtn.setSelected(false);
        secondAndBtn.setSelected(false);
      }

      if (inputIdentifier.isEmpty()) {
        AlertBox.showAlert("Fehlende Eingabe", "Kein Typ ausgewählt", "", AlertType.WARNING);
        return null;
      } else {
        return RuleFactory.generate(inputIdentifier, "");
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
