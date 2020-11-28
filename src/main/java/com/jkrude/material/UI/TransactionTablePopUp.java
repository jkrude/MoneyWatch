package com.jkrude.material.UI;

import com.jkrude.material.Money;
import com.jkrude.material.Utility;
import com.jkrude.transaction.ExtendedTransaction;
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

  public TableView<ExtendedTransaction> table;
  // Columns
  @FXML
  private TableColumn<ExtendedTransaction, String> isActive;
  @FXML
  private TableColumn<ExtendedTransaction, String> accountIban;
  @FXML
  private TableColumn<ExtendedTransaction, String> transferDate;
  @FXML
  private TableColumn<ExtendedTransaction, String> validationDate;
  @FXML
  private TableColumn<ExtendedTransaction, String> transferSpecification;
  @FXML
  private TableColumn<ExtendedTransaction, String> usage;
  @FXML
  private TableColumn<ExtendedTransaction, String> creditorId;
  @FXML
  private TableColumn<ExtendedTransaction, String> mandateReference;
  @FXML
  private TableColumn<ExtendedTransaction, String> customerReferenceRndToEnd;
  @FXML
  private TableColumn<ExtendedTransaction, String> collectionReference;
  @FXML
  private TableColumn<ExtendedTransaction, String> debitOriginalAmount;
  @FXML
  private TableColumn<ExtendedTransaction, String> backDebit;
  @FXML
  private TableColumn<ExtendedTransaction, String> otherParty;
  @FXML
  private TableColumn<ExtendedTransaction, String> iban;
  @FXML
  private TableColumn<ExtendedTransaction, String> bic;
  @FXML
  private TableColumn<ExtendedTransaction, Money> amount;
  @FXML
  private TableColumn<ExtendedTransaction, String> info;

  public Button closeBtn;

  private final SimpleIntegerProperty activatedColumns = new SimpleIntegerProperty(6);
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


  public static class Builder {

    private final TransactionTablePopUp ttp;
    private final Stage stage;

    private static final URL fxmlResource = TransactionTablePopUp.class
        .getResource("/PopUp/camtEntryAsTable.fxml");

    private Builder() {
      FXMLLoader loader = new FXMLLoader();
      stage = StageSetter.setupStage(loader, fxmlResource);
      ttp = loader.getController();
      ttp.closeBtn.setOnAction(event -> stage.close());
    }

    public static Builder initSet(final ObservableList<ExtendedTransaction> tableData) {
      Builder b = new Builder();
      b.ttp.table.setItems(tableData);
      return b;
    }

    public static Builder initBind(
        final ObjectProperty<? extends ObservableList<ExtendedTransaction>> tableData) {
      Builder b = new Builder();
      b.ttp.table.itemsProperty().bind(tableData);
      return b;
    }

    public Builder setTitle(String categoryName) {
      stage.setTitle(categoryName);
      return this;
    }

    public Builder setContextMenu(
        Callback<TableRow<ExtendedTransaction>, ContextMenu> menuGenerator) {
      ttp.table.setRowFactory(
          new Callback<TableView<com.jkrude.transaction.ExtendedTransaction>, TableRow<com.jkrude.transaction.ExtendedTransaction>>() {
            @Override
            public TableRow<ExtendedTransaction> call(
                TableView<ExtendedTransaction> transactionTableView) {
              final TableRow<ExtendedTransaction> row = new TableRow<>();
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
