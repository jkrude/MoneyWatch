package com.jkrude.material.UI;

import com.jkrude.controller.ParentController;
import com.jkrude.material.Camt.CamtEntry;
import com.jkrude.material.Camt.ListType;
import com.jkrude.material.Money;
import java.util.Date;
import java.util.List;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Callback;

public class TableStageController {

  public TableView<CamtEntry> table;
  // Columns
  public TableColumn<CamtEntry, String> accountIban;
  public TableColumn<CamtEntry, Date> transferDate;
  public TableColumn<CamtEntry, String> validationDate;
  public TableColumn<CamtEntry, String> transferSpecification;
  public TableColumn<CamtEntry, String> usage;
  public TableColumn<CamtEntry, String> creditorId;
  public TableColumn<CamtEntry, String> mandateReference;
  public TableColumn<CamtEntry, String> customerReferenceRndToEnd;
  public TableColumn<CamtEntry, String> collectionReference;
  public TableColumn<CamtEntry, String> debitOriginalAmount;
  public TableColumn<CamtEntry, String> backDebit;
  public TableColumn<CamtEntry, String> otherParty;
  public TableColumn<CamtEntry, String> iban;
  public TableColumn<CamtEntry, String> bic;
  public TableColumn<CamtEntry, Money> amount;
  public TableColumn<CamtEntry, String> info;
  public Button cancelBtn;
  private SimpleIntegerProperty activatedColumns = new SimpleIntegerProperty(4);
  private SimpleIntegerProperty columnWidth;


  @FXML
  public void initialize() {
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
    transferDate.setCellValueFactory(callback -> new SimpleObjectProperty<Date>(
        callback.getValue().getDate()));
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
    table.setContextMenu(contextMenu);
  }

  public void setItems(List<CamtEntry> tableItems) {
    table.getItems().addAll(tableItems);
  }

  public void close(ActionEvent event){
    Stage stage = ParentController.getStageForActionEvent(event);
    stage.close();
  }


}
