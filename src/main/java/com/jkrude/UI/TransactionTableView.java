package com.jkrude.UI;


import com.jkrude.material.Money;
import com.jkrude.material.Utility;
import com.jkrude.transaction.ExtendedTransaction;
import com.jkrude.transaction.Transaction.TransactionField;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.util.Callback;

public class TransactionTableView implements Initializable {


  @FXML protected TableView<ExtendedTransaction> table;
  // Columns
  @FXML protected TableColumn<ExtendedTransaction, String> isActive;
  @FXML protected TableColumn<ExtendedTransaction, String> accountIban;
  @FXML protected TableColumn<ExtendedTransaction, String> transferDate;
  @FXML protected TableColumn<ExtendedTransaction, String> validationDate;
  @FXML protected TableColumn<ExtendedTransaction, String> transferSpecification;
  @FXML protected TableColumn<ExtendedTransaction, String> usage;
  @FXML protected TableColumn<ExtendedTransaction, String> creditorId;
  @FXML protected TableColumn<ExtendedTransaction, String> mandateReference;
  @FXML protected TableColumn<ExtendedTransaction, String> customerReferenceRndToEnd;
  @FXML protected TableColumn<ExtendedTransaction, String> collectionReference;
  @FXML protected TableColumn<ExtendedTransaction, String> debitOriginalAmount;
  @FXML protected TableColumn<ExtendedTransaction, String> backDebit;
  @FXML protected TableColumn<ExtendedTransaction, String> otherParty;
  @FXML protected TableColumn<ExtendedTransaction, String> iban;
  @FXML protected TableColumn<ExtendedTransaction, String> bic;
  @FXML protected TableColumn<ExtendedTransaction, Money> amount;
  @FXML protected TableColumn<ExtendedTransaction, String> info;


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    // Use all available space evenly across the columns

    table.setPlaceholder(new Label("No Transactions matching."));

    isActive.setCellValueFactory(callback -> {
      SimpleStringProperty stringProp = new SimpleStringProperty();
          stringProp.bind(Bindings.when(callback.getValue().isActiveProperty()).then("Active")
              .otherwise("Ignored"));
          return stringProp;
        }
    );
    accountIban.setCellValueFactory(callback -> new SimpleStringProperty(
        callback.getValue().getBaseTransaction().getAccountIban()));
    transferDate.setCellValueFactory(callback -> new SimpleStringProperty(
        Utility.DATE_TIME_FORMATTER.format(callback.getValue().getBaseTransaction().getDate())));
    validationDate.setCellValueFactory(callback -> new SimpleStringProperty(
        Utility.DATE_TIME_FORMATTER
            .format(callback.getValue().getBaseTransaction().getValidationDate())));
    transferSpecification.setCellValueFactory(callback -> new SimpleStringProperty(
        callback.getValue().getBaseTransaction().getTransferSpecification()));
    usage.setCellValueFactory(callback -> new SimpleStringProperty(
        callback.getValue().getBaseTransaction().getUsage()));
    creditorId.setCellValueFactory(callback -> new SimpleStringProperty(
        callback.getValue().getBaseTransaction().getCreditorId()));
    mandateReference.setCellValueFactory(callback -> new SimpleStringProperty(
        callback.getValue().getBaseTransaction().getMandateReference()));
    customerReferenceRndToEnd.setCellValueFactory(callback -> new SimpleStringProperty(
        callback.getValue().getBaseTransaction().getCustomerReference()));
    collectionReference.setCellValueFactory(callback -> new SimpleStringProperty(
        callback.getValue().getBaseTransaction().getCollectionReference()));
    debitOriginalAmount.setCellValueFactory(callback -> new SimpleStringProperty(
        callback.getValue().getBaseTransaction().getDebitOriginalAmount()));
    backDebit.setCellValueFactory(callback -> new SimpleStringProperty(
        callback.getValue().getBaseTransaction().getBackDebit()));
    otherParty.setCellValueFactory(callback -> new SimpleStringProperty(
        callback.getValue().getBaseTransaction().getOtherParty()));
    iban.setCellValueFactory(callback -> new SimpleStringProperty(
        callback.getValue().getBaseTransaction().getIban()));
    bic.setCellValueFactory(callback -> new SimpleStringProperty(
        callback.getValue().getBaseTransaction().getBic()));
    amount.setCellValueFactory(callback -> new SimpleObjectProperty<>(
        callback.getValue().getBaseTransaction().getMoneyAmount()));
    info.setCellValueFactory(callback -> new SimpleStringProperty(
        callback.getValue().getBaseTransaction().getInfo()));

    for (TableColumn<ExtendedTransaction, String> extendedTransactionStringTableColumn : Arrays.asList(
        isActive, accountIban, transferDate, validationDate, transferSpecification, usage, creditorId,
        mandateReference, customerReferenceRndToEnd, collectionReference, debitOriginalAmount, backDebit,
        otherParty, iban, bic, info)) {
      extendedTransactionStringTableColumn.setMinWidth(30);
      extendedTransactionStringTableColumn.setCellFactory(ToolTippedTableCell.forTableColumn());
    }
    amount.setMinWidth(15);

    amount.setCellFactory(getColoredCellFactory());

    table.setRowFactory(
        extendedTransactionTableView -> {
          final TableRow<ExtendedTransaction> row = new TableRow<>();
          row.contextMenuProperty().bind(
              Bindings.when(Bindings.isNotNull(row.itemProperty()))
                  .then(TransactionTableView.defaultContextMenu(row))
                  .otherwise((ContextMenu) null));
          return row;
        }
    );

  }

  // Control which columns should be shown initially.
  public void setVisible(boolean isActive, TransactionField... fields) {
    this.isActive.setVisible(isActive);
    for (var field : TransactionField.values()) {
      getColumn(field).setVisible(false);
    }
    for (var visible : fields) {
      getColumn(visible).setVisible(true);
    }
  }

  private Callback<
      TableColumn<ExtendedTransaction, Money>,
      TableCell<ExtendedTransaction, Money>>
  getColoredCellFactory() {
    // Set the color of the text depending on amount > 0 ?
    return new Callback<>() {
      @Override
      public TableCell<ExtendedTransaction, Money> call(
          TableColumn<ExtendedTransaction, Money> moneyTableColumn) {
        return new ToolTippedTableCell<>() {
          @Override
          public void updateItem(Money money, boolean empty) {
            super.updateItem(money, empty);
            if (!empty) {
              setText(money.toString());
              setStyle("-fx-text-fill: " + (money.isPositive() ? "-fx-def-good" : "-fx-def-bad"));
            } else {
              setText(null);
            }
          }
        };
      }
    };
  }

  public static ContextMenu defaultContextMenu(TableRow<ExtendedTransaction> row) {
    ContextMenu contextMenu = new ContextMenu();
    MenuItem ignoreTransaction = new MenuItem("Ignore/Activate transaction");
    ignoreTransaction.setOnAction(event -> row.getItem().switchActive());
    contextMenu.getItems().add(ignoreTransaction);
    return contextMenu;
  }

  public boolean getIsActiveColumnShown() {
    return isActive.visibleProperty().get();
  }

  public void setIsActiveColumnShown(boolean isActiveColumnShown) {
    this.isActive.visibleProperty().set(isActiveColumnShown);
  }

  public void setContextMenu(
      Callback<TableRow<ExtendedTransaction>, ContextMenu> contextMenuCallback) {
    table.setRowFactory(
        extendedTransactionTableView -> {
          final TableRow<ExtendedTransaction> row = new TableRow<>();
          row.contextMenuProperty().bind(
              Bindings.when(Bindings.isNotNull(row.itemProperty()))
                  .then(contextMenuCallback.call(row))
                  .otherwise((ContextMenu) null));
          return row;
        }
    );
  }

  public ObjectProperty<ObservableList<ExtendedTransaction>> itemsProperty() {
    return table.itemsProperty();
  }

  private TableColumn<ExtendedTransaction, ?> getColumn(TransactionField field) {
    switch (field) {

      case ACCOUNT_IBAN:
        return this.accountIban;
      case TRANSFER_DATE:
        return this.transferDate;
      case VALIDATION_DATE:
        return this.validationDate;
      case TRANSFER_SPECIFICATION:
        return this.transferSpecification;
      case USAGE:
        return this.usage;
      case CREDITOR_ID:
        return this.creditorId;
      case MANDATE_REFERENCE:
        return this.mandateReference;
      case CUSTOMER_REFERENCE_END_TO_END:
        return this.customerReferenceRndToEnd;
      case COLLECTION_REFERENCE:
        return this.collectionReference;
      case DEBIT_ORIGINAL_AMOUNT:
        return this.debitOriginalAmount;
      case BACK_DEBIT:
        return this.backDebit;
      case OTHER_PARTY:
        return this.otherParty;
      case IBAN:
        return this.iban;
      case BIC:
        return this.bic;
      case AMOUNT:
        return this.amount;
      case INFO:
        return this.info;
    }
    throw new IllegalArgumentException("Passed unknown field: " + field.name());
  }

}
