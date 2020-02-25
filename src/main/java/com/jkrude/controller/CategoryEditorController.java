package com.jkrude.controller;


import com.jkrude.material.AlertBox;
import com.jkrude.material.Camt;
import com.jkrude.material.Camt.ListType;
import com.jkrude.material.PieCategory;
import com.jkrude.material.Rule;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;


public class CategoryEditorController extends ParentController {

  public ListView<Rule> ruleLV;
  public ToggleButton secondAndBtn;
  public ToggleButton firstAndBtn;
  public TextField secondAndTF;
  public TextField firstAndTF;
  public ChoiceBox<ListType> firstAndCB;
  public ChoiceBox<ListType> secondAndCB;
  @FXML
  private Button backButton;
  @FXML
  private ListView<PieCategory> categoryLV;
  @FXML
  private Label categoryNameLabel;
  @FXML
  private ChoiceBox<Camt.ListType> defaultCB;
  @FXML
  private TextField defaultTF;
  @FXML
  private TextField categoryNameInputField;

  @FXML
  public void initialize() {

    setupAddRuleLayout();
    backButton.setOnAction(ParentController::goBack);

    // Setup data-source
    categoryLV.itemsProperty()
        .bindBidirectional(ParentController.model.getProfile().getCategoriesProperty());
    // Set placeholder
    categoryLV.setPlaceholder(new Label("No categories configured."));
    ruleLV.setPlaceholder(new Label("No category selected yet or category is empty"));

    // Initialize first selected entry
    if (categoryLV.getItems() != null && !categoryLV.getItems().isEmpty()) {
      categoryLV.getSelectionModel().selectFirst();
      ruleLV.setItems(categoryLV.getSelectionModel().getSelectedItem().getIdentifierList());
      categoryNameLabel.setText(categoryLV.getSelectionModel().getSelectedItem().getName().get());
    } else {
      categoryNameLabel.setText("");
    }

    // Set name for listView entries
    categoryLV.setCellFactory(
        callback ->
            new ListCell<>() {
              @Override
              protected void updateItem(PieCategory item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                  textProperty().bind(item.getName());
                  //Set contextMenu
                  setContextMenu(getContextMenuForLVCell(this));
                  emptyProperty().addListener(
                      (obs, wasEmpty, isNowEmpty) -> {
                        if (isNowEmpty) {
                          setContextMenu(null);
                        } else {
                          setContextMenu(getContextMenuForLVCell(this));
                        }
                      }
                  );
                } else {
                  textProperty().unbind();
                  setText(null);
                }
              }
            }
    );
    // Listener if an item was selected
    categoryLV.getSelectionModel().selectedItemProperty().addListener(
        (ObservableValue<? extends PieCategory> ov, PieCategory oldVal,
            PieCategory newVal) -> {
          categoryNameLabel.setText(newVal.getName().get());
          ruleLV.itemsProperty().unbindBidirectional(oldVal.getIdentifierProperty());
          ruleLV.itemsProperty().bindBidirectional(newVal.getIdentifierProperty());
        }
    );

    ruleLV.setCellFactory(callback -> new RuleCell());
  }


  private void setupAddRuleLayout() {
    ObservableList<ListType> typeChoiceList = FXCollections
        .observableArrayList(ListType.IBAN, ListType.USAGE, ListType.OTHER_PARTY);
    defaultCB.setItems(typeChoiceList);

    firstAndCB.setItems(typeChoiceList);
    addListenerForCB(firstAndCB, defaultCB);
    firstAndCB.visibleProperty().bind(firstAndBtn.selectedProperty());
    firstAndTF.visibleProperty().bind(firstAndBtn.selectedProperty());

    secondAndBtn.visibleProperty().bind(firstAndBtn.selectedProperty());
    addListenerForCB(secondAndCB, firstAndCB);
    secondAndCB.visibleProperty().bind(secondAndBtn.selectedProperty());
    secondAndTF.visibleProperty().bind(secondAndBtn.selectedProperty());
  }

  private void addListenerForCB(ChoiceBox<ListType> observingChoiceBox,
      ChoiceBox<ListType> observedChoiceBox) {
    observedChoiceBox.getSelectionModel().selectedItemProperty().addListener(
        (ObservableValue<? extends ListType> obsValue, ListType oldV, ListType newV) -> {
            observingChoiceBox
                .setItems(observedChoiceBox.getItems().filtered(type1 -> !type1.equals(newV)));
        });
  }

  public void addEntryToCategory(ActionEvent event) {
    try {
      Map<ListType, String> inputMap = new HashMap<>();
      inputMap.put(defaultCB.getSelectionModel().getSelectedItem(), defaultTF.getText());
      if (firstAndBtn.isSelected()) {
        inputMap.put(firstAndCB.getSelectionModel().getSelectedItem(), firstAndTF.getText());
        if (secondAndBtn.isSelected()) {
          inputMap.put(secondAndCB.getSelectionModel().getSelectedItem(), secondAndTF.getText());
        }
        firstAndBtn.setSelected(false);
      }
      Rule rule = Rule.RuleFactory.generate(inputMap, "");
      ruleLV.getItems().add(rule);
    } catch (ParseException e) {
      e.printStackTrace();
      AlertBox.showAlert("Fehlerhafte Eingabe", "Das Datum war nicht im Format mm.dd.yy",
          "Eingabe: " + defaultTF
              .getText(),
          AlertType.ERROR);
    }
    //TODO
    // Error if entry already in List -> equals method necessary in PieCategory.Entry
    // Input validation
  }

  public void addCategory(ActionEvent event) {
    //TODO input validation
    PieCategory category = new PieCategory(categoryNameInputField.getText());
    categoryLV.getItems().add(category);
    categoryLV.getSelectionModel().select(categoryLV.getItems().size() - 1);
  }

  private ContextMenu getContextMenuForLVCell(ListCell<PieCategory> cell) {
    ContextMenu contextMenu = new ContextMenu();
    MenuItem mIRename = new MenuItem("Rename");
    mIRename.setOnAction(event -> newNameDialog(cell));
    MenuItem mIDelete = new MenuItem("Delete");
    mIDelete.setOnAction(event -> cell.getListView().getItems().remove(cell.getItem()));

    contextMenu.getItems().addAll(mIRename, mIDelete);
    return contextMenu;
  }

  private void newNameDialog(ListCell<PieCategory> cell) {
    TextInputDialog textInputDialog = new TextInputDialog("New Name");
    textInputDialog.setHeaderText("");
    textInputDialog.setTitle("Change the name");
    Optional<String> result = textInputDialog.showAndWait();
    if (result.isPresent() && !result.get().isBlank()) {
      // Binded biderectional
      cell.getItem().getName().set(result.get());
    }
  }

  @Override
  protected void checkIntegrity() {
  }

  private static class RuleCell extends ListCell<Rule> {

    HBox hbox = new HBox();
    Label label = new Label("(empty)");
    Pane pane = new Pane();

    public RuleCell() {
      super();
      hbox.getChildren().addAll(label, pane);
      HBox.setHgrow(pane, Priority.ALWAYS);
    }

    @Override
    protected void updateItem(Rule rule, boolean empty) {
      super.updateItem(rule, empty);
      if (empty) {
        setGraphic(null);
      } else {
        StringBuilder stringBuilder = new StringBuilder();
        rule.getIdentifierMap()
            .forEach((key, value) -> stringBuilder.append(key).append(": ").append(value).append(", "));
        stringBuilder.delete(stringBuilder.lastIndexOf(","),stringBuilder.length()-1);
        label.setText(stringBuilder.toString());
        setGraphic(hbox);
      }
    }
  }

}
