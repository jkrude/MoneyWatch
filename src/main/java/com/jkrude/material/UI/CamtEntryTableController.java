package com.jkrude.material.UI;

import com.jkrude.material.Camt.CamtEntry;
import com.jkrude.material.Camt.ListType;
import com.jkrude.material.Money;
import com.jkrude.material.PieCategory;
import com.jkrude.material.Utility;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Pair;

public class CamtEntryTableController {

  public TableView<CamtEntry> table;
  // Columns
  @FXML
  private TableColumn<CamtEntry, String> accountIban;
  @FXML
  private TableColumn<CamtEntry, String> transferDate;
  @FXML
  private TableColumn<CamtEntry, String> validationDate;
  @FXML
  private TableColumn<CamtEntry, String> transferSpecification;
  @FXML
  private TableColumn<CamtEntry, String> usage;
  @FXML
  private TableColumn<CamtEntry, String> creditorId;
  @FXML
  private TableColumn<CamtEntry, String> mandateReference;
  @FXML
  private TableColumn<CamtEntry, String> customerReferenceRndToEnd;
  @FXML
  private TableColumn<CamtEntry, String> collectionReference;
  @FXML
  private TableColumn<CamtEntry, String> debitOriginalAmount;
  @FXML
  private TableColumn<CamtEntry, String> backDebit;
  @FXML
  private TableColumn<CamtEntry, String> otherParty;
  @FXML
  private TableColumn<CamtEntry, String> iban;
  @FXML
  private TableColumn<CamtEntry, String> bic;
  @FXML
  private TableColumn<CamtEntry, Money> amount;
  @FXML
  private TableColumn<CamtEntry, String> info;

  public Button closeBtn;

  private SimpleIntegerProperty activatedColumns = new SimpleIntegerProperty(4);
  private SimpleIntegerProperty columnWidth;


  protected FXMLLoader loader;
  protected Stage stage;

  // Make Constructor private so it has to be build
  public CamtEntryTableController() {
  }

  @FXML
  private void initialize() {
    columnWidth = new SimpleIntegerProperty();
    columnWidth.bind(table.widthProperty().divide(activatedColumns));
    accountIban.setVisible(false);
    accountIban.setVisible(false);
    validationDate.setVisible(false);
    transferSpecification.setVisible(false);
    creditorId.setVisible(false);
    mandateReference.setVisible(false);
    customerReferenceRndToEnd.setVisible(false);
    collectionReference.setVisible(false);
    debitOriginalAmount.setVisible(false);
    backDebit.setVisible(false);
    bic.setVisible(false);
    info.setVisible(false);

    accountIban.prefWidthProperty().bind(columnWidth);
    transferDate.setCellValueFactory(callback -> new SimpleStringProperty(
        Utility.dateFormatter.format(callback.getValue().getDate())));
    validationDate.setCellValueFactory(callback -> new SimpleStringProperty(
        callback.getValue().getDataPoint().getValidationDate()));
    transferSpecification.setCellValueFactory(callback -> new SimpleStringProperty(
        callback.getValue().getDataPoint().getTransferSpecification()));
    usage.setCellValueFactory(callback -> new SimpleStringProperty(
        callback.getValue().getDataPoint().getUsage()));
    creditorId.setCellValueFactory(callback -> new SimpleStringProperty(
        callback.getValue().getDataPoint().getCreditorId()));
    mandateReference.setCellValueFactory(callback -> new SimpleStringProperty(
        callback.getValue().getDataPoint().getMandateReference()));
    customerReferenceRndToEnd.setCellValueFactory(callback -> new SimpleStringProperty(
        callback.getValue().getDataPoint().getCustomerReference()));
    collectionReference.setCellValueFactory(callback -> new SimpleStringProperty(
        callback.getValue().getDataPoint().getCollectionReference()));
    debitOriginalAmount.setCellValueFactory(callback -> new SimpleStringProperty(
        callback.getValue().getDataPoint().getDebitOriginalAmount()));
    backDebit.setCellValueFactory(callback -> new SimpleStringProperty(
        callback.getValue().getDataPoint().getBackDebit()));
    otherParty.setCellValueFactory(callback -> new SimpleStringProperty(
        callback.getValue().getDataPoint().getOtherParty()));
    iban.setCellValueFactory(callback -> new SimpleStringProperty(
        callback.getValue().getDataPoint().getIban()));
    bic.setCellValueFactory(callback -> new SimpleStringProperty(
        callback.getValue().getDataPoint().getBic()));
    amount.setCellValueFactory(callback -> new SimpleObjectProperty<Money>(
        callback.getValue().getDataPoint().getAmount()));
    info.setCellValueFactory(callback -> new SimpleStringProperty(
        callback.getValue().getDataPoint().getInfo()));

    amount.setCellFactory(
        new Callback<>() {
          @Override
          public TableCell<CamtEntry, Money> call(
              TableColumn<CamtEntry, Money> camtEntryMoneyTableColumn) {
            return new TableCell<CamtEntry, Money>() {
              @Override
              protected void updateItem(Money money, boolean empty) {
                super.updateItem(money, empty);
                if (!empty) {
                  setText(money.toString());
                  if (money.getAmount().compareTo(BigDecimal.ZERO) < 0) {
                    setTextFill(Color.RED);
                  } else {
                    setTextFill(Color.GREEN);
                  }
                } else {
                  setText(null);
                }
              }
            };
          }
        });

    ContextMenu contextMenu = new ContextMenu();

    ComboBox<ListType> possibleColumns = new ComboBox<>();
    /*possibleColumns.setCellFactory(new Callback<ListView<ListType>, ListCell<ListType>>() {
      @Override
      public ListCell<ListType> call(ListView<ListType> listTypeListView) {
        return new ListCell<>() {
          @Override
          protected void updateItem(ListType item, boolean empty) {
            super.updateItem(item, empty);
            if(item != null && !empty){
              RadioButton radioButton = new RadioButton();;
              radioButton.setText(item.toString());
              setGraphic(radioButton);
              radioButton.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observableValue,
                    Boolean aBoolean, Boolean t1) {

                }
              }
              (ObservableValue<? extends Boolean> observableValue, Boolean oldV, Boolean newV) -> {
                if(newV){
                  observableValue.getValue().
                }
              }
              );
            }else{
              setGraphic(null);
            }
          }
        };
      }
    }*/
    possibleColumns.getItems().addAll(ListType.values());
    MenuItem choiceItem = new MenuItem("Aktivierte Spalten");
    choiceItem.setGraphic(possibleColumns);
    contextMenu.getItems().add(choiceItem);
    //FIXME
    // table.setContextMenu(contextMenu);
  }


  public void showAndWait() {
    if (stage.getScene() != null) {
      stage.showAndWait();
    } else {
      throw new IllegalStateException("Stage was null");
    }
  }

  public CamtEntryTableController setContextMenu(ObservableList<PieCategory> categories) {
    if (table != null) {
      table.setRowFactory(
          new Callback<TableView<CamtEntry>, TableRow<CamtEntry>>() {
            @Override
            public TableRow<CamtEntry> call(TableView<CamtEntry> camtEntryTableView) {
              final TableRow<CamtEntry> row = new TableRow<>();
              ContextMenu contextMenu = new ContextMenu();
              Menu catChoices = new Menu("Als Regel Hinzufügen");
              for (PieCategory category : categories) {
                MenuItem menuItem = new MenuItem(category.getName().get());

                menuItem.setOnAction(event -> {
                  CamtEntry entry = row.getItem();
                  Set<Pair<ListType, String>> pairs = new HashSet<>();
                  Optional<Set<ListType>> r = RuleDialog.chooseRelevantField(entry);
                  if (r.isPresent()) {
                    Set<ListType> selectedListTypes = r.get();
                    pairs = entry.getSelectedFields(selectedListTypes);
                    RuleDialog.showAndEdit(
                        category::addRule,
                        pairs.iterator()
                    );
                  }
                });
                catChoices.getItems().add(menuItem);
              }
              contextMenu.getItems().add(catChoices);
              row.contextMenuProperty().bind(
                  Bindings.when(Bindings.isNotNull(row.itemProperty())).then(contextMenu)
                      .otherwise((ContextMenu) null));
              return row;
            }
          }
      );
    } else {
      throw new IllegalStateException("Table was null");
    }
    return this;
  }

  public CamtEntryTableController setCloseCallback(Consumer<CamtEntryTableController> consumer) {
    this.closeBtn.setOnAction(event -> {
      consumer.accept(this);
      this.stage.close();
    });
    return this;
  }

}
