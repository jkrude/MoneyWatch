package com.jkrude.controller;


import com.jkrude.material.Camt;
import com.jkrude.material.Camt.ListType;
import com.jkrude.material.PieCategory;
import com.jkrude.material.PieCategory.Entry;
import java.util.Optional;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;


public class CategoryEditorController extends AbstractController {

  @FXML
  private Button backButton;
  @FXML
  private ListView<PieCategory> listView;
  @FXML
  private Label categoryNameLabel;
  @FXML
  private TableView<PieCategory.Entry> table;
  @FXML
  private TableColumn<PieCategory.Entry, String> typeColumn;
  @FXML
  private TableColumn<PieCategory.Entry, String> patternColumn;
  @FXML
  private ChoiceBox<Camt.ListType> typeChoices;
  @FXML
  private TextField patternInputField;
  @FXML
  private Button addCategoryButton;
  @FXML
  private TextField categoryNameInputField;

  @FXML
  public void initialize() {

    typeChoices.setItems(
        FXCollections.observableArrayList(ListType.IBAN, ListType.USAGE, ListType.OTHER_PARTY));
    backButton.setOnAction(AbstractController::goBack);

    // Setup listView
    listView.itemsProperty()
        .bindBidirectional(AbstractController.model.getProfile().getCategoriesProperty());

    //TODO
    // if(categories != null){
    listView.getSelectionModel().selectFirst();
    table.setItems(listView.getSelectionModel().getSelectedItem().getIdentifierList());
    categoryNameLabel.setText(listView.getSelectionModel().getSelectedItem().getName());
    //}

    // Set name for listView entries
    listView.setCellFactory(
        callback ->
            new ListCell<>() {
              @Override
              protected void updateItem(PieCategory item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                  setText(item.getName());
                  //Set contextMenu
                  setContextMenu(getContextMenuForLVCell(this));
                  emptyProperty().addListener(
                      (obs, wasEmpty, isNowEmpty) -> {
                        if(isNowEmpty){
                          setContextMenu(null);
                        }else{
                          setContextMenu(getContextMenuForLVCell(this));
                        }
                      }
                  );
                } else {
                  setText(null);
                }
              }
            }
    );
    // Listener if an item was selected
    listView.getSelectionModel().selectedItemProperty().addListener(
        (ObservableValue<? extends PieCategory> ov, PieCategory oldVal,
            PieCategory newVal) -> {
          categoryNameLabel.setText(newVal.getName());
          table.itemsProperty().unbindBidirectional(oldVal.getIdentifierProperty());
          table.itemsProperty().bindBidirectional(newVal.getIdentifierProperty());
        }
    );

    // Setup table
    typeColumn.setCellValueFactory(
        callback -> new SimpleObjectProperty<>(callback.getValue().getType().toString()));
    typeColumn.prefWidthProperty().bind(table.widthProperty().divide(2));
    patternColumn.setCellValueFactory(new PropertyValueFactory<>("pattern"));
    patternColumn.prefWidthProperty().bind(table.widthProperty().divide(2));
  }

  public void addEntryToCategory(ActionEvent event) {
    PieCategory.Entry entry = new Entry(patternInputField.getText(),
        typeChoices.getSelectionModel().getSelectedItem());

    //TODO
    // Error if entry already in List -> equals method necessary in PieCategory.Entry
    // Input validation
    table.getItems().add(entry);
  }

  public void addCategory(ActionEvent event) {
    //TODO input validation
    PieCategory category = new PieCategory(categoryNameInputField.getText());
    listView.getItems().add(category);
    listView.getSelectionModel().select(listView.getItems().size() - 1);
  }

  private ContextMenu getContextMenuForLVCell(ListCell<PieCategory> cell) {
    ContextMenu contextMenu = new ContextMenu();
    MenuItem mIRename = new MenuItem("Rename");
    mIRename.setOnAction(event -> newNameDialog(cell));
    MenuItem mIDelete = new MenuItem("Delete");
    mIDelete.setOnAction(event -> cell.getListView().getItems().remove(cell.getItem()));

    contextMenu.getItems().addAll(mIRename,mIDelete);
    return contextMenu;
  }

  private void newNameDialog(ListCell<PieCategory> cell) {
    TextInputDialog textInputDialog = new TextInputDialog("New Name");
    textInputDialog.setHeaderText("");
    textInputDialog.setTitle("Change the name");
    Optional<String> result = textInputDialog.showAndWait();
    if (result.isPresent() && !result.get().isBlank()) {
      cell.setText(result.get());
    }
  }

  @Override
  protected void checkIntegrity() {
  }
}
