package com.jkrude.UI;

import com.jkrude.material.Model;
import com.jkrude.material.PropertyFilteredList;
import com.jkrude.transaction.ExtendedTransaction;
import com.jkrude.transaction.Transaction.TransactionField;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

public class TransactionTabs implements Initializable {


  @FXML private TransactionTableView ttvIgnoredController;
  @FXML private TransactionTableView ttvAllController;

  private final Model globalModel;
  private final SimpleObjectProperty<ObservableList<ExtendedTransaction>> allTransactions;
  private final SimpleObjectProperty<PropertyFilteredList<ExtendedTransaction>> ignoredTransactions;


  public TransactionTabs() {
    this.globalModel = Model.getInstance();
    allTransactions = new SimpleObjectProperty<>();
    if (this.globalModel.getActiveData() != null) {
      allTransactions.set(globalModel.getActiveData().getSource());
    }
    globalModel.activeDataProperty().addListener(
        observable -> allTransactions.set(globalModel.getActiveData().getSource()));

    ignoredTransactions = new SimpleObjectProperty<>(
        new PropertyFilteredList<>(ExtendedTransaction::isActiveProperty,
            et -> et.isActiveProperty().not().get()));
    if (globalModel.getActiveData() != null) {
      ignoredTransactions.get().setAll(globalModel.getActiveData().getSource());
    }
    globalModel.activeDataProperty().addListener(
        observable -> ignoredTransactions.get().setAll(globalModel.getActiveData().getSourceRO()));

  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    this.ttvIgnoredController.itemsProperty().bind(this.ignoredTransactions);
    this.ttvIgnoredController
        .setVisible(false, TransactionField.TRANSFER_DATE, TransactionField.USAGE,
            TransactionField.AMOUNT, TransactionField.IBAN);

    this.ttvAllController.itemsProperty().bind(this.allTransactions);

  }
}
