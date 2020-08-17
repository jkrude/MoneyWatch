package com.jkrude.material.UI;

import com.jkrude.material.AlertBox;
import com.jkrude.material.Camt;
import com.jkrude.material.Camt.ListType;
import com.jkrude.material.Rule;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.util.Pair;

public class RuleDialog {

  private Rule generatedRule;
  private final ObservableList<ListType> typeChoiceList = FXCollections
      .observableArrayList(Camt.ListType.values());
  Dialog<Rule> internalDialog;
  ListView<CustomHBox> listView;

  public RuleDialog() {
    internalDialog = new Dialog<>();
    internalDialog.setTitle("Regel-Editor");
    internalDialog.setHeaderText(null);
    listView = new ListView<>();
    listView.prefWidthProperty().bind(internalDialog.getDialogPane().widthProperty());
    internalDialog.getDialogPane().setContent(listView);
    internalDialog.setResizable(true);
    internalDialog.setWidth(600);
    internalDialog.getDialogPane().maxWidth(800);
    internalDialog.setHeight(400);
    internalDialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);
    // Validate input.
    Button btnApply = (Button) internalDialog.getDialogPane().lookupButton(ButtonType.APPLY);
    btnApply.addEventFilter(ActionEvent.ACTION, event -> {
      if (!validateAndGenerate(listView)) {
        event.consume();
      }
    });

    internalDialog.setResultConverter(buttonType -> {
      if (buttonType == ButtonType.APPLY) {
        return generatedRule;
      }
      return null;
    });

  }

  public Optional<Rule> editRuleShowAndWait(Set<Pair<ListType, String>> idPairs) {
    fillListFromRule(idPairs);
    return internalDialog.showAndWait();
  }

  public Optional<Rule> editRuleShowAndWait(Rule rule) {
    return editRuleShowAndWait(rule.getIdentifierPairs());
  }

  public Optional<Rule> showAndWait() {
    fillList(listView);
    return internalDialog.showAndWait();
  }

  private void fillList(ListView<CustomHBox> listView) {
    for (ListType type : typeChoiceList) {
      listView.getItems().add(new CustomHBox(type));
    }
  }

  private void fillListFromRule(Set<Pair<ListType, String>> idPairs) {
    Map<ListType, CustomHBox> map = new HashMap<>();
    idPairs.forEach(
        pair -> map.put(pair.getKey(), new CustomHBox(pair.getKey(), pair.getValue()))
    );

    for (ListType type : typeChoiceList) {
      listView.getItems().add(map.getOrDefault(type, new CustomHBox(type)));
    }
  }

  private boolean validateAndGenerate(ListView<CustomHBox> listView) {
    Set<Pair<ListType, String>> generatingSet =
        listView.getItems().stream()
            .filter(box -> !box.checkBox.isDisabled())
            .filter(box -> box.checkBox.isSelected())
            .map(box -> new Pair<>(box.type, box.textField.getText()))
            .collect(Collectors.toSet());
    if (generatingSet.isEmpty()) {
      AlertBox.showAlert(
          "Keine Eingabe!",
          "Mindestens ein Feld muss ausgewählt und aktiv sein.",
          "Um den Dialog zu schließen klicken Sie <Cancel>.",
          AlertType.WARNING
      );
      return false;
    }
    try {
      // TODO: Add note
      generatedRule = Rule.RuleFactory.generate(generatingSet, "");
      return true;
    } catch (ParseException e) {
      AlertBox
          .showAlert(
              "Fehlerhafte eingabe!",
              "Eine Eingabe hat nicht dem Format entsprochen",
              "",
              AlertType.WARNING);
      return false;
    } catch (NumberFormatException e) {
      AlertBox.showAlert(
          "Fehlerhafte Eingabe!",
          "Der Geldbetrag hat nicht dem Format XX.XX oder einer Ganzzahl entsprochen",
          "",
          AlertType.WARNING);
      return false;
    }
  }

  private static class CustomHBox extends HBox {

    public CheckBox checkBox;
    public ListType type;
    public Label typeLabel;
    public TextField textField;

    private CustomHBox() {
    }

    public CustomHBox(ListType type) {
      super(10); // Spacing
      this.type = type;
      checkBox = new CheckBox();
      typeLabel = new Label(type.getTranslation());
      textField = new TextField();
      textField.setPromptText(getTextHint(type));
      checkBox.disableProperty().bind(Bindings
          .createBooleanBinding(() -> textField.getText().trim().isEmpty(),
              textField.textProperty()));
      getChildren().addAll(checkBox, typeLabel, textField);
    }

    public CustomHBox(ListType key, String value) {
      this(key);
      textField.setText(value);
      checkBox.setSelected(true);
    }

    private String getTextHint(ListType type) {
      switch (type) {
        case ACCOUNT_IBAN:
        case IBAN:
          return "DE12345678901234567890";
        case TRANSFER_DATE:
        case VALIDATION_DATE:
          return "01.01.1980";
        case TRANSFER_SPECIFICATION:
          return "Kartenzahlung";
        case USAGE:
          return "Spesen";
        case CREDITOR_ID:
          return "DE31ZZZ00000563123";
        case MANDATE_REFERENCE:
          return "5LFJ224TP5YA0";
        case CUSTOMER_REFERENCE_END_TO_END:
          return "1234567890123 PP.7515.PP PAYPAL";
        case COLLECTION_REFERENCE:
          return "1234567890-12345678901234";
        case DEBIT_ORIGINAL_AMOUNT:
          return "10.50€";
        case BACK_DEBIT:
          return "";
        case OTHER_PARTY:
          return "Max Mustermann";
        case BIC:
          return "BELADEBEXXX";
        case AMOUNT:
          return "10,01€";
        case INFO:
          return "Umsatz gebucht";
      }
      throw new IllegalArgumentException();
    }
  }

}
