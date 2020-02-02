package com.jkrude.controller;


import com.jkrude.material.Camt;
import com.jkrude.material.Camt.ListType;
import com.jkrude.material.PieCategory;
import com.jkrude.material.PieCategory.Entry;
import com.jkrude.test.TestData;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;


public class CategoryEditorController extends AbstractController {

  @FXML
  public Button backButton;
  @FXML
  public ListView<PieCategory> listView;
  @FXML
  public Label categoryName;
  @FXML
  public TableView<PieCategory.Entry> table;
  @FXML
  public TableColumn<PieCategory.Entry, String> typeColumn;
  @FXML
  public TableColumn<PieCategory.Entry, String> patternColumn;
  @FXML
  public ChoiceBox<Camt.ListType> typeChoices;
  @FXML
  public TextField patternInputField;

  private ObservableList<Entry> entriesForCategory;


  @FXML
  public void initialize() {

    typeChoices.setItems(
        FXCollections.observableArrayList(ListType.IBAN, ListType.USAGE, ListType.OTHER_PARTY));
    backButton.setOnAction(AbstractController::goBack);

    // Setup listView
    ObservableList<PieCategory> items = FXCollections
        .observableArrayList(TestData.getProfile().getPieCategories());

    listView.setItems(items);
    //TODO if(items != null){
    listView.getSelectionModel().select(0);
    entriesForCategory = FXCollections
        .observableArrayList(listView.getSelectionModel().getSelectedItem().getIdentifierList());
    //}
    // Set name for listView entries
    listView.setCellFactory(
        callback ->
            new ListCell<>() {
              @Override
              protected void updateItem(PieCategory item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                  setText(item.getName());
                } else {
                  setText(null);
                }
              }
            }
    );
    listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    // Listener if an item was selected
    listView.getSelectionModel().selectedItemProperty().addListener(
        (ObservableValue<? extends PieCategory> ov, PieCategory oldVal,
            PieCategory newVal) -> {
          categoryName.setText(newVal.getName());
          entriesForCategory = FXCollections.observableArrayList(newVal.getIdentifierList());
        }
    );
    categoryName.setText(items.get(0).getName());

    // Setup table
    typeColumn.setCellValueFactory(
        callback -> new SimpleObjectProperty<>(callback.getValue().getType().toString()));
    typeColumn.prefWidthProperty().bind(table.widthProperty().divide(2));
    patternColumn.setCellValueFactory(new PropertyValueFactory<>("pattern"));
    patternColumn.prefWidthProperty().bind(table.widthProperty().divide(2));
    table.setItems(entriesForCategory);
  }

  public void addEntryToCategory(ActionEvent event) {
    PieCategory.Entry entry = new Entry(patternInputField.getText(),
        typeChoices.getSelectionModel().getSelectedItem());

    //TODO
    // Error if entry already in List -> equals method necessary in PieCategory.Entry
    // Input validation
    entriesForCategory.add(entry);
  }


  @Override
  protected void checkIntegrity() {
  }
}
