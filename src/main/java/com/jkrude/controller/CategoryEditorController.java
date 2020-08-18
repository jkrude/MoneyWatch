package com.jkrude.controller;


import com.jkrude.main.Main;
import com.jkrude.material.AlertBox;
import com.jkrude.material.Model;
import com.jkrude.material.PieCategory;
import com.jkrude.material.Rule;
import com.jkrude.material.UI.RuleDialog;
import java.util.Optional;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;


public class CategoryEditorController extends Controller {

  public ListView<Rule> ruleLV;

  @FXML
  private Button backButton;

  @FXML
  private Button ruleBtn;

  @FXML
  private ListView<PieCategory> categoryLV;
  @FXML
  private Label categoryNameLabel;
  @FXML
  private TextField categoryNameInputField;

  @Override
  public void prepare() {
  }

  @FXML
  public void initialize() {

    backButton.setOnAction(e -> Main.goBack());

    // Setup data-source
    categoryLV.itemsProperty()
        .bindBidirectional(Model.getInstance().getProfile().getCategoriesProperty());
    // Set placeholder
    categoryLV.setPlaceholder(new Label("No categories configured."));
    ruleLV.setPlaceholder(new Label("No category selected yet or category is empty"));

    // Initialize first selected entry
    if (categoryLV.getItems() != null && !categoryLV.getItems().isEmpty()) {
      categoryLV.getSelectionModel().selectFirst();
      ruleLV.setItems(categoryLV.getSelectionModel().getSelectedItem().getRulesRO());
      categoryNameLabel.setText(categoryLV.getSelectionModel().getSelectedItem().getName().get());
    } else {
      categoryNameLabel.setText("");
    }

    // Set name for listView entries
    categoryLV.setCellFactory(
        callback ->
            new ListCell<>() {
              @Override
              protected void updateItem(PieCategory category, boolean empty) {
                super.updateItem(category, empty);
                if (category != null && !empty) {
                  textProperty().bind(category.getName());
                  //Set contextMenu
                  setContextMenu(getCMForCategoryLVCell(this));
                } else {
                  setContextMenu(null);
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

  @FXML
  private void newRuleDialog() {
    Optional<Rule> result = new RuleDialog().showAndWait();
    if (result.isPresent()) {
      if (ruleLV.getItems().contains(result.get())) {
        showAlertForExistingRule();
      } else {
        ruleLV.getItems().add(result.get());
      }
    }
  }

  @FXML
  private void addCategory() {
    // Check if the input is empty
    String inText = categoryNameInputField.getText();
    if (inText.isBlank()) {
      showAlertForEmptyInput();
      return;
    }
    PieCategory category = new PieCategory(categoryNameInputField.getText());
    categoryLV.getItems().add(category);
    // Clear leftovers
    categoryLV.getSelectionModel().select(categoryLV.getItems().size() - 1);
    categoryNameInputField.clear();
  }

  private ContextMenu getCMForCategoryLVCell(ListCell<PieCategory> cell) {
    ContextMenu contextMenu = new ContextMenu();
    MenuItem mIRename = new MenuItem("Rename");
    mIRename.setOnAction(event -> newNameDialog(cell));
    MenuItem mIDelete = new MenuItem("Delete");
    mIDelete.setOnAction(event -> cell.getListView().getItems().remove(cell.getItem()));
    contextMenu.getItems().addAll(mIRename, mIDelete);
    return contextMenu;
  }

  private void newNameDialog(ListCell<PieCategory> cell) {
    TextInputDialog textInputDialog = new TextInputDialog("New name");
    textInputDialog.setHeaderText("");
    textInputDialog.setTitle("Change the name here");
    textInputDialog.getEditor().setText(cell.getItem().getName().get());
    Optional<String> result = textInputDialog.showAndWait();
    if (result.isPresent()) {
      if (result.get().isBlank()) {
        showAlertForEmptyInput();
        newNameDialog(cell);
      } else if (!cell.getItem().getName().get()
          .equals(result.get())) {
        // Binded bidirectional
        cell.getItem().getName().set(result.get());
      }
    }
  }

  private ContextMenu getCMForRuleLVCell(ListCell<Rule> cell) {
    ContextMenu contextMenu = new ContextMenu();
    MenuItem mIEdit = new MenuItem("Edit");
    mIEdit.setOnAction(event -> editRuleDialog(cell));
    MenuItem mIDelete = new MenuItem("Remove");
    mIDelete.setOnAction(event -> cell.getListView().getItems().remove(cell.getItem()));
    contextMenu.getItems().addAll(mIEdit, mIDelete);
    return contextMenu;
  }

  private void editRuleDialog(ListCell<Rule> cell) {
    Optional<Rule> optRule = new RuleDialog().editRuleShowAndWait(cell.getItem());
    if (optRule.isPresent() && !optRule.get().equals(cell.getItem())) {
      Rule rule = optRule.get();
      if (ruleLV.getItems().contains(rule)) {
        showAlertForExistingRule();
      }
      ruleLV.getItems().remove(cell.getItem());
      ruleLV.getItems().add(rule);
    }
  }

  private void showAlertForExistingRule() {
    AlertBox.showAlert("Error!", "Rule already exists", "", AlertType.ERROR);
  }

  private void showAlertForEmptyInput() {
    AlertBox.showAlert("Incorrect input!", "The name can not be empty", "",
        AlertType.INFORMATION);
  }

  private class RuleCell extends ListCell<Rule> {

    HBox hbox = new HBox();
    Label label = new Label("(empty)");
    Pane pane = new Pane();

    public RuleCell() {
      super();
      label.setFont(Font.font("Libre Sans", 14));
      hbox.getChildren().addAll(label, pane);
      HBox.setHgrow(pane, Priority.ALWAYS);
    }

    @Override
    protected void updateItem(Rule rule, boolean empty) {
      super.updateItem(rule, empty);
      if (rule != null && !empty) {
        StringBuilder stringBuilder = new StringBuilder();
        rule.getIdentifierPairs()
            .forEach(
                pair -> stringBuilder.append(pair.getKey()).append(": ").append(pair.getValue())
                    .append(", "));
        stringBuilder.delete(stringBuilder.lastIndexOf(","), stringBuilder.length() - 1);
        label.setText(stringBuilder.toString());
        setGraphic(hbox);
        setContextMenu(getCMForRuleLVCell(this));

      } else {
        setContextMenu(null);
        setGraphic(null);
      }
    }
  }

}
