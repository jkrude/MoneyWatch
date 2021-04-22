package com.jkrude.material.UI;

import com.jkrude.material.Money;
import com.jkrude.material.Utility;
import com.jkrude.transaction.ExtendedTransaction;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;
import javafx.util.Callback;

public class TransactionTableView implements Initializable {

  @FXML
  protected TableView<ExtendedTransaction> table;
  // Columns
  @FXML
  protected TableColumn<ExtendedTransaction, String> isActive;
  @FXML
  protected TableColumn<ExtendedTransaction, String> accountIban;
  @FXML
  protected TableColumn<ExtendedTransaction, String> transferDate;
  @FXML
  protected TableColumn<ExtendedTransaction, String> validationDate;
  @FXML
  protected TableColumn<ExtendedTransaction, String> transferSpecification;
  @FXML
  protected TableColumn<ExtendedTransaction, String> usage;
  @FXML
  protected TableColumn<ExtendedTransaction, String> creditorId;
  @FXML
  protected TableColumn<ExtendedTransaction, String> mandateReference;
  @FXML
  protected TableColumn<ExtendedTransaction, String> customerReferenceRndToEnd;
  @FXML
  protected TableColumn<ExtendedTransaction, String> collectionReference;
  @FXML
  protected TableColumn<ExtendedTransaction, String> debitOriginalAmount;
  @FXML
  protected TableColumn<ExtendedTransaction, String> backDebit;
  @FXML
  protected TableColumn<ExtendedTransaction, String> otherParty;
  @FXML
  protected TableColumn<ExtendedTransaction, String> iban;
  @FXML
  protected TableColumn<ExtendedTransaction, String> bic;
  @FXML
  protected TableColumn<ExtendedTransaction, Money> amount;
  @FXML
  protected TableColumn<ExtendedTransaction, String> info;


  protected final SimpleIntegerProperty activatedColumns = new SimpleIntegerProperty(6);
  protected SimpleIntegerProperty columnWidth;

  protected BooleanProperty isActiveColumnShown;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    // Use all available space evenly across the columns
    this.columnWidth = new SimpleIntegerProperty();
    this.isActiveColumnShown = new SimpleBooleanProperty(true);
    this.columnWidth.bind(table.widthProperty().divide(activatedColumns));
    table.getColumns().forEach(
        transactionTableColumn -> transactionTableColumn.prefWidthProperty().bind(columnWidth));

    isActive.setCellValueFactory(callback -> {
          SimpleStringProperty stringProp = new SimpleStringProperty();
          stringProp.bind(Bindings.when(callback.getValue().isActiveProperty()).then("Active")
              .otherwise("Ignored"));
          return stringProp;
        }
    );
    isActive.visibleProperty().bind(isActiveColumnShown);
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

  private Callback<TableColumn<ExtendedTransaction, Money>, TableCell<ExtendedTransaction, Money>> getColoredCellFactory() {
    // Set the color of the text depending on amount > 0 ?
    return new Callback<>() {
      @Override
      public TableCell<ExtendedTransaction, Money> call(
          TableColumn<ExtendedTransaction, Money> moneyTableColumn) {
        return new TableCell<>() {
          @Override
          protected void updateItem(Money money, boolean empty) {
            super.updateItem(money, empty);
            if (!empty) {
              setText(money.toString());
              setTextFill(money.isPositive() ? Color.GREEN : Color.RED);
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
    return isActiveColumnShown.get();
  }

  public BooleanProperty isActiveColumnShown() {
    return isActiveColumnShown;
  }

  public void setIsActiveColumnShown(boolean isActiveColumnShown) {
    this.isActiveColumnShown.set(isActiveColumnShown);
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
}
