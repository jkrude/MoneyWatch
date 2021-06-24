package com.jkrude.UI;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import com.jkrude.category.Rule;
import com.jkrude.transaction.Transaction.TransactionField;
import java.net.URL;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.animation.PauseTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;


public class RuleDialog implements Initializable {

  @FXML private JFXTextField infoField;
  @FXML private JFXTextField accountIbanField;
  @FXML private JFXCheckBox accountIbanBox;
  @FXML private JFXCheckBox transferDateBox;
  @FXML private JFXCheckBox validationDateBox;
  @FXML private JFXCheckBox transferSpecificationBox;
  @FXML private JFXCheckBox creditorIdBox;
  @FXML private JFXCheckBox mandateReferenceBox;
  @FXML private JFXCheckBox customerReferenceEndToEndBox;
  @FXML private JFXCheckBox collectionReferenceBox;
  @FXML private JFXCheckBox debitOriginalAmountBox;
  @FXML private JFXTextField transferDateField;
  @FXML private JFXTextField validationDateField;
  @FXML private JFXTextField transferSpecificationField;
  @FXML private JFXTextField creditorIdField;
  @FXML private JFXTextField mandateReferenceField;
  @FXML private JFXTextField customerReferenceEndToEndField;
  @FXML private JFXTextField collectionReferenceField;
  @FXML private JFXTextField debitOriginalAmountField;
  @FXML private JFXTextField backDebitField;
  @FXML private JFXTextField bicField;
  @FXML private JFXTextField amountField;
  @FXML private JFXCheckBox bicBox;
  @FXML private JFXCheckBox amountBox;
  @FXML private JFXCheckBox infoBox;
  @FXML private JFXCheckBox backDebitBox;
  @FXML private JFXTextArea noteArea;
  @FXML private JFXTextField ibanField;
  @FXML private JFXTextField usageField;
  @FXML private JFXTextField otherPartyField;
  @FXML private JFXCheckBox checkAllBox;
  @FXML private JFXCheckBox ibanBox;
  @FXML private JFXCheckBox usageBox;
  @FXML private JFXCheckBox otherPartyBox;
  @FXML private JFXButton cancelBtn;
  @FXML private JFXButton applyBtn;
  @FXML private Label errorLabel;

  private Rule generatedRule;
  private PauseTransition errorDelay;


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

    validateFxml();

    checkAllBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue,
          Boolean newValue) {
        if (oldValue != null && newValue != null && oldValue != newValue) {
          for (var field : TransactionField.values()) {
            getCheckBox(field).setSelected(newValue);
          }
        }
      }
    });
    Tooltip tlp = new Tooltip("(Un)check all");
    tlp.setShowDelay(Duration.seconds(1));
    checkAllBox.setTooltip(tlp);

    applyBtn.addEventFilter(ActionEvent.ACTION, new EventHandler<Event>() {
      // Only works if validation is successful.
      @Override
      public void handle(Event event) {
        if (!validateAndGenerate()) {
          RuleDialog.this.generatedRule = null;
          event.consume();
        }
      }
    });

    // Assert that generatedRule is null.
    cancelBtn.addEventHandler(ActionEvent.ACTION, event -> RuleDialog.this.generatedRule = null);

    errorDelay = new PauseTransition(Duration.seconds(5));
  }

  private void validateFxml() {
    for (var field : TransactionField.values()) {
      if (getTextField(field) == null || getCheckBox(field) == null) {
        throw new IllegalStateException("Rule dialog active but not all elements inflated.");
      }
    }
  }

  private void showError(String error) {
    errorDelay.stop();
    errorLabel.setDisable(false);
    errorLabel.setVisible(true);
    errorLabel.setText(error);

    errorDelay.setOnFinished(event -> {
      errorLabel.setVisible(false);
      errorLabel.setDisable(true);
    });
    errorDelay.playFromStart();
  }

  private void fillGridFromRule(Map<TransactionField, String> idMap) {
    idMap.forEach((key, value) -> getTextField(key).setText(value));
  }

  private JFXTextField getTextField(TransactionField field) {
    switch (field) {
      case ACCOUNT_IBAN:
        return this.accountIbanField;
      case TRANSFER_DATE:
        return this.transferDateField;
      case VALIDATION_DATE:
        return this.validationDateField;
      case TRANSFER_SPECIFICATION:
        return this.transferSpecificationField;
      case USAGE:
        return this.usageField;
      case CREDITOR_ID:
        return this.creditorIdField;
      case MANDATE_REFERENCE:
        return this.mandateReferenceField;
      case CUSTOMER_REFERENCE_END_TO_END:
        return this.customerReferenceEndToEndField;
      case COLLECTION_REFERENCE:
        return this.collectionReferenceField;
      case DEBIT_ORIGINAL_AMOUNT:
        return this.debitOriginalAmountField;
      case BACK_DEBIT:
        return this.backDebitField;
      case OTHER_PARTY:
        return this.otherPartyField;
      case IBAN:
        return this.ibanField;
      case BIC:
        return this.bicField;
      case AMOUNT:
        return this.amountField;
      case INFO:
        return this.infoField;
    }
    throw new IllegalArgumentException(field + "could not be catched in switch");
  }

  private JFXCheckBox getCheckBox(TransactionField field) {
    switch (field) {
      case ACCOUNT_IBAN:
        return this.accountIbanBox;
      case TRANSFER_DATE:
        return this.transferDateBox;
      case VALIDATION_DATE:
        return this.validationDateBox;
      case TRANSFER_SPECIFICATION:
        return this.transferSpecificationBox;
      case USAGE:
        return this.usageBox;
      case CREDITOR_ID:
        return this.creditorIdBox;
      case MANDATE_REFERENCE:
        return this.mandateReferenceBox;
      case CUSTOMER_REFERENCE_END_TO_END:
        return this.customerReferenceEndToEndBox;
      case COLLECTION_REFERENCE:
        return this.collectionReferenceBox;
      case DEBIT_ORIGINAL_AMOUNT:
        return this.debitOriginalAmountBox;
      case BACK_DEBIT:
        return this.backDebitBox;
      case OTHER_PARTY:
        return this.otherPartyBox;
      case IBAN:
        return this.ibanBox;
      case BIC:
        return this.bicBox;
      case AMOUNT:
        return this.amountBox;
      case INFO:
        return this.infoBox;
    }
    throw new IllegalArgumentException(field + "could not be catched in switch");
  }

  private boolean validateAndGenerate() {
    Map<TransactionField, String> generatingMap = new HashMap<>();
    for (TransactionField field : TransactionField.values()) {
      String text = getTextField(field).getText();
      if (getCheckBox(field).isSelected() && !text.isBlank()) {
        generatingMap.put(field, text);
      }
    }
    if (generatingMap.isEmpty()) {
      showError("At least one field must not be empty and checked.");
      return false;
    }

    String extraNote = noteArea.getText();

    try {
      this.generatedRule = Rule.RuleBuilder.fromMap(generatingMap).addNote(extraNote).build();
      return true;
    } catch (ParseException e) {
      showError("An entry did not match its format");
      return false;
    } catch (NumberFormatException e) {
      showError("The amount did not match the format (X / X.Y / Z EUR / Z €)");
      return false;
    }
  }

  public static class Builder {

    private static final URL FXM_RESOURCE = RuleDialog.class
        .getResource("/com/jkrude/UI/RuleDialog.fxml");

    private final Stage stage;
    private final RuleDialog controller;

    public Builder() {
      FXMLLoader loader = new FXMLLoader();
      stage = StageSetter.setupStage(loader, FXM_RESOURCE);
      stage.initModality(Modality.APPLICATION_MODAL);
      stage.setMinHeight(830);
      stage.setMinWidth(600);
      stage.setAlwaysOnTop(true);
      controller = loader.getController();
      controller.cancelBtn.setOnAction(action -> stage.close());
      controller.applyBtn.setOnAction(action -> stage.close());
    }

    public Builder editRule(Map<TransactionField, String> idMap) {
      controller.fillGridFromRule(idMap);
      return this;
    }

    public Builder editRule(Rule rule) {
      return editRule(rule.getIdentifierPairs());
    }

    public Builder initiallySelected(TransactionField... selected) {
      for (var field : selected) {
        controller.getCheckBox(field).setSelected(true);
      }
      return this;
    }

    public Optional<Rule> showAndWait() {
      stage.showAndWait();
      return Optional.ofNullable(controller.generatedRule);
    }
  }

}
