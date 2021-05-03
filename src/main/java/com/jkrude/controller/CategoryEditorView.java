package com.jkrude.controller;

import com.jkrude.category.CategoryNode;
import com.jkrude.category.Rule;
import com.jkrude.main.Main;
import com.jkrude.material.AlertBox.AlertBuilder;
import com.jkrude.material.UI.ColorPickerDialog;
import com.jkrude.material.UI.RuleDialog;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Callback;

public class CategoryEditorView implements FxmlView<CategoryEditorViewModel>, Initializable,
    Prepareable {

  @FXML
  private AnchorPane rulePane;
  @FXML
  private Button addRuleBtn;
  @FXML
  private Button editRuleBtn;
  @FXML
  private Button deleteRuleBtn;
  @FXML
  private ListView<Rule> ruleView;
  @FXML
  private TreeView<CategoryNode> categoryTreeView;
  @FXML
  private SimpleBooleanProperty invalidatedProperty;


  @InjectViewModel
  private CategoryEditorViewModel viewModel;
  private final InvalidationListener invalidator = (observable -> invalidatedProperty.set(true));


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    invalidatedProperty = new SimpleBooleanProperty(false);
    rulePane.visibleProperty().bind(categoryTreeView.getSelectionModel().selectedItemProperty()
        .isNotNull());
    ruleView.setPlaceholder(new Label("This category has no rules yet"));
    ruleView.setCellFactory(ruleListView -> new RuleCell());
    addRuleBtn.disableProperty().bind(categoryTreeView.getSelectionModel().selectedItemProperty()
        .isNull());
    editRuleBtn.disableProperty()
        .bind(ruleView.getSelectionModel().selectedItemProperty()
            .isNull());
    deleteRuleBtn.disableProperty()
        .bind(ruleView.getSelectionModel().selectedItemProperty()
            .isNull());
    // TreeView::cellFactory: Bind text to name and set dynamic contextMenu
    categoryTreeView.setCellFactory(new Callback<>() {
      @Override
      public TreeCell<CategoryNode> call(TreeView<CategoryNode> categoryNodeTreeView) {
        return new TreeCell<>() {
          @Override
          public void updateItem(CategoryNode item, boolean isEmpty) {
            super.updateItem(item, isEmpty);
            if (isEmpty) {
              setContextMenu(null);
              textProperty().unbind();
              textProperty().set("");
              setGraphic(null);
            } else {
              setContextMenu(CategoryEditorView.getCMForCategory(this));
              textProperty().unbind();
              textProperty().bind(item.nameProperty());
              Circle c = new Circle(5);
              c.fillProperty().bind(item.colorProperty());
              setGraphic(c);
            }
          }
        };
      }
    });
    // Bin ruleView to rules of selected item.
    categoryTreeView.getSelectionModel().selectedItemProperty().addListener(
        (observed, oldValue, newValue) -> {
          if (oldValue != null) {
            //FIXME: unbind works in mysterious ways and does not unbind because it was never bound
            ruleView.itemsProperty().unbindBidirectional(oldValue.getValue().rulesRO());
          }
          if (newValue != null) {
            ruleView.itemsProperty()
                .bindBidirectional(newValue.getValue().rulesRO());
          }
        });
    // Populate categoryTreeView.
    viewModel.setTreeViewItems(categoryTreeView, invalidator);
  }

  @Override
  public void prepare() {
    if (invalidatedProperty != null && invalidatedProperty.get()) {
      viewModel.setTreeViewItems(categoryTreeView, invalidator);
      invalidatedProperty.set(false);
      categoryTreeView.getSelectionModel().clearSelection();
    }
  }

  /*
   * Category
   */
  private static ContextMenu getCMForCategory(TreeCell<CategoryNode> cell) {
    ContextMenu cm = new ContextMenu();
    MenuItem iRename = new MenuItem("Rename");
    iRename.setOnAction(actionEvent -> newNameDialog(cell));
    MenuItem iAddChild = new MenuItem("Add subcategory");
    iAddChild.setOnAction(actionEvent -> addSubCategory(cell));
    MenuItem iRemove = new MenuItem("Delete");
    iRemove.setOnAction(actionEvent -> removeCategory(cell));
    MenuItem iColor = new MenuItem("Change color");
    iColor.setOnAction(actionEvent -> changeColor(cell.getItem()));
    // If cell != root => add #changeParent.
    if (cell.getTreeItem().getParent() != null) {
      MenuItem iMove = new MenuItem("Change parent");
      iMove.setOnAction(actionEvent -> changeParent(cell));
      cm.getItems().add(iMove);
    }
    cm.getItems().addAll(iRename, iAddChild, iRemove, iColor);
    return cm;
  }

  private static void changeColor(CategoryNode node) {
    var optColor = ColorPickerDialog.showAndWait();
    optColor.ifPresent(node::setColor);
  }

  private static void newNameDialog(TreeCell<CategoryNode> cell) {
    TextInputDialog textInputDialog = new TextInputDialog("New name");
    textInputDialog.setHeaderText("");
    textInputDialog.setTitle("Change the name here");
    textInputDialog.getEditor().setText(cell.getItem().getName());
    Optional<String> result = textInputDialog.showAndWait();
    if (result.isPresent()) {
      if (result.get().isBlank()) {
        showAlertForEmptyInput();
        newNameDialog(cell);
        // Only rename if it is new name.
      } else if (!cell.getItem().toString().equals(result.get())) {
        cell.getItem().nameProperty().set(result.get());
      }
    }
  }

  private static void addSubCategory(TreeCell<CategoryNode> cell) {
    CategoryNode node = cell.getItem();
    if (node == null) {
      throw new NullPointerException();
    }
    TextInputDialog textInputDialog = new TextInputDialog("New Category");
    textInputDialog.setTitle("Set category-name");
    Optional<String> result = textInputDialog.showAndWait();
    if (result.isEmpty()) {
      return;
    }
    if (node.childNodesRO().stream().map(CategoryNode::getName).anyMatch(result.get()::equals)) {
      AlertBuilder.alert(AlertType.WARNING)
          .setTitle("Category already existing!")
          .setHeader("The chosen name is already used")
          .setMessage("Please choose a different name")
          .buildAndShow();
      return;
    }
    var optColor = ColorPickerDialog.showAndWait();
    CategoryNode newCategory = new CategoryNode(result.get());
    newCategory.setColor(optColor.orElse(Color.DEEPPINK));
    node.addCategory(newCategory);
  }

  private static void removeCategory(TreeCell<CategoryNode> cell) {
    // Get parent TreeItem -> get parent::item(CategoryNode) -> remove cell::item(CategoryNode).
    cell.getTreeItem().getParent().getValue().removeCategory(cell.getItem());
  }

  private static void changeParent(TreeCell<CategoryNode> cell) {
    CategoryNode node = cell.getItem();
    CategoryNode parent = cell.getTreeItem().getParent().getValue();
    ChoiceDialog<DrawableCategoryNode> choiceDialog = new ChoiceDialog<>(
        new DrawableCategoryNode(parent),
        node.getRoot().streamCollapse()
            .filter(categoryNode -> categoryNode != node)
            .map(DrawableCategoryNode::new)
            .collect(Collectors.toList()));
    Optional<DrawableCategoryNode> optCat = choiceDialog.showAndWait();
    if (optCat.isPresent() && !optCat.get().getNode().equals(node)) {
      optCat.get().getNode().addCategory(node);
      parent.removeCategory(node);
    }
  }

  /*
   * Rule
   */
  private void addRule() {
    CategoryNode node = categoryTreeView.getSelectionModel().getSelectedItem().getValue();
    assert node != null;
    new RuleDialog()
        .showAndWait()
        .ifPresent(node::addRule);
  }

  private void replaceRule() {
    Rule currentRule = ruleView.getSelectionModel().getSelectedItem();
    int idx = ruleView.getSelectionModel().getSelectedIndex();
    assert currentRule != null;
    new RuleDialog()
        .editRuleShowAndWait(currentRule)
        .ifPresent(newRule -> ruleView.getItems().set(idx, newRule));
  }

  private void removeRule() {
    assert ruleView.getSelectionModel().getSelectedItem() != null;
    assert categoryTreeView.getSelectionModel().getSelectedItem() != null;
    Rule currentRule = ruleView.getSelectionModel().getSelectedItem();
    categoryTreeView.getSelectionModel().getSelectedItem().getValue().removeRule(
        currentRule
    );
  }

  private static ListCell<CategoryNode> categoryConverter(
      ListView<CategoryNode> categoryNodeListView) {
    return new ListCell<>() {
      @Override
      protected void updateItem(CategoryNode categoryNode, boolean isEmpty) {
        super.updateItem(categoryNode, isEmpty);
        if (isEmpty) {
          setText(null);
        } else {
          setText(categoryNode.getName());
        }
      }
    };
  }

  private static void showAlertForEmptyInput() {
    AlertBuilder.alert(AlertType.INFORMATION)
        .setTitle("Incorrect input!")
        .setHeader("The name can not be empty")
        .buildAndShow();
  }

  @FXML
  private void goBack() {
    Main.goBack();
  }

  @FXML
  private void addRuleAction(ActionEvent event) {
    addRule();
  }

  @FXML
  private void editRuleAction(ActionEvent event) {
    replaceRule();
  }

  @FXML
  private void deleteRuleAction(ActionEvent event) {
    removeRule();
  }

  public static class RuleCell extends ListCell<Rule> {

    @Override
    protected void updateItem(Rule rule, boolean isEmpty) {
      super.updateItem(rule, isEmpty);
      if (isEmpty) {
        setText(null);
        setContextMenu(null);
      } else {
        // TODO: beautify rule visualisation
        StringBuilder stringBuilder = new StringBuilder();
        rule.getIdentifierPairs()
            .forEach(
                pair -> stringBuilder.append(pair.getKey()).append(": ").append(pair.getValue())
                    .append(", "));
        stringBuilder.delete(stringBuilder.lastIndexOf(","), stringBuilder.length() - 1);
        setText(stringBuilder.toString());
      }
    }
  }

  private static class DrawableCategoryNode {

    private final CategoryNode node;

    public DrawableCategoryNode(CategoryNode node) {
      this.node = node;
    }

    public CategoryNode getNode() {
      return node;
    }

    @Override
    public String toString() {
      return node.getName();
    }
  }

}
