package com.jkrude.material.UI;

import com.jkrude.material.Money;
import com.jkrude.material.TransactionContainer.Transaction;
import com.jkrude.material.Utility;
import java.net.URL;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

public class TransactionTablePopUp {

  public TableView<Transaction> table;
  // Columns
  @FXML
  private TableColumn<Transaction, String> accountIban;
  @FXML
  private TableColumn<Transaction, String> transferDate;
  @FXML
  private TableColumn<Transaction, String> validationDate;
  @FXML
  private TableColumn<Transaction, String> transferSpecification;
  @FXML
  private TableColumn<Transaction, String> usage;
  @FXML
  private TableColumn<Transaction, String> creditorId;
  @FXML
  private TableColumn<Transaction, String> mandateReference;
  @FXML
  private TableColumn<Transaction, String> customerReferenceRndToEnd;
  @FXML
  private TableColumn<Transaction, String> collectionReference;
  @FXML
  private TableColumn<Transaction, String> debitOriginalAmount;
  @FXML
  private TableColumn<Transaction, String> backDebit;
  @FXML
  private TableColumn<Transaction, String> otherParty;
  @FXML
  private TableColumn<Transaction, String> iban;
  @FXML
  private TableColumn<Transaction, String> bic;
  @FXML
  private TableColumn<Transaction, Money> amount;
  @FXML
  private TableColumn<Transaction, String> info;

  public Button closeBtn;

  private SimpleIntegerProperty activatedColumns = new SimpleIntegerProperty(5);
  private SimpleIntegerProperty columnWidth;

  // Constructor needs to be public (FXML) but should NOT be used
  public TransactionTablePopUp() {
  }

  @FXML
  private void initialize() {
    // Use all available space evenly across the columns
    columnWidth = new SimpleIntegerProperty();
    columnWidth.bind(table.widthProperty().divide(activatedColumns));
    table.getColumns().forEach(
        transactionTableColumn -> transactionTableColumn.prefWidthProperty().bind(columnWidth));

    accountIban.setCellValueFactory(callback -> new SimpleStringProperty(
        callback.getValue().getAccountIban()));
    transferDate.setCellValueFactory(callback -> new SimpleStringProperty(
        Utility.dateFormatter.format(callback.getValue().getDate())));
    validationDate.setCellValueFactory(callback -> new SimpleStringProperty(
        Utility.dateFormatter.format(callback.getValue().getValidationDate())));
    transferSpecification.setCellValueFactory(callback -> new SimpleStringProperty(
        callback.getValue().getTransferSpecification()));
    usage.setCellValueFactory(callback -> new SimpleStringProperty(
        callback.getValue().getUsage()));
    creditorId.setCellValueFactory(callback -> new SimpleStringProperty(
        callback.getValue().getCreditorId()));
    mandateReference.setCellValueFactory(callback -> new SimpleStringProperty(
        callback.getValue().getMandateReference()));
    customerReferenceRndToEnd.setCellValueFactory(callback -> new SimpleStringProperty(
        callback.getValue().getCustomerReference()));
    collectionReference.setCellValueFactory(callback -> new SimpleStringProperty(
        callback.getValue().getCollectionReference()));
    debitOriginalAmount.setCellValueFactory(callback -> new SimpleStringProperty(
        callback.getValue().getDebitOriginalAmount()));
    backDebit.setCellValueFactory(callback -> new SimpleStringProperty(
        callback.getValue().getBackDebit()));
    otherParty.setCellValueFactory(callback -> new SimpleStringProperty(
        callback.getValue().getOtherParty()));
    iban.setCellValueFactory(callback -> new SimpleStringProperty(
        callback.getValue().getIban()));
    bic.setCellValueFactory(callback -> new SimpleStringProperty(
        callback.getValue().getBic()));
    amount.setCellValueFactory(callback -> new SimpleObjectProperty<>(
        callback.getValue().getMoneyAmount()));
    info.setCellValueFactory(callback -> new SimpleStringProperty(
        callback.getValue().getInfo()));

    amount.setCellFactory(getColoredCellFactory());

    /*ContextMenu contextMenu = new ContextMenu();

    ComboBox<ListType> possibleColumns = new ComboBox<>();
    possibleColumns.getItems().addAll(ListType.values());
    MenuItem choiceItem = new MenuItem("Aktivierte Spalten");
    choiceItem.setGraphic(possibleColumns);
    contextMenu.getItems().add(choiceItem);
    //FIXME
    table.setContextMenu(contextMenu);*/
  }

  private Callback<TableColumn<Transaction, Money>, TableCell<Transaction, Money>> getColoredCellFactory() {
    // Set the color of the text depending on amount > 0 ?
    return new Callback<>() {
      @Override
      public TableCell<Transaction, Money> call(
          TableColumn<Transaction, Money> moneyTableColumn) {
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


  public static class Builder {

    private TransactionTablePopUp ttp;
    private Stage stage;

    private static final URL fxmlResource = TransactionTablePopUp.class
        .getResource("/PopUp/camtEntryAsTable.fxml");

    private Builder() {
      FXMLLoader loader = new FXMLLoader();
      stage = StageSetter.setupStage(loader, fxmlResource);
      ttp = loader.getController();
      ttp.closeBtn.setOnAction(event -> stage.close());
    }

    public static Builder initSet(final ObservableList<Transaction> tableData) {
      Builder b = new Builder();
      b.ttp.table.setItems(tableData);
      return b;
    }

    public static Builder initBind(final ObjectProperty<ObservableList<Transaction>> tableData) {
      Builder b = new Builder();
      b.ttp.table.itemsProperty().bind(tableData);
      return b;
    }

    public Builder setTitle(String categoryName) {
      stage.setTitle(categoryName);
      return this;
    }

    public Builder setContextMenu(Callback<TableRow<Transaction>, ContextMenu> menuGenerator) {
      ttp.table.setRowFactory(
          new Callback<>() {
            @Override
            public TableRow<Transaction> call(TableView<Transaction> transactionTableView) {
              final TableRow<Transaction> row = new TableRow<>();
              ContextMenu contextMenu = menuGenerator.call(row);
              row.contextMenuProperty().bind(
                  Bindings.when(Bindings.isNotNull(row.itemProperty())).then(contextMenu)
                      .otherwise((ContextMenu) null));
              return row;
            }
          }
      );
      return this;
    }

    public void showAndWait() {
      stage.initModality(Modality.APPLICATION_MODAL);
      stage.showAndWait();
    }
  }

}
