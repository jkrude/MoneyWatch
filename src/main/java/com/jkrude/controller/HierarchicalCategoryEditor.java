package com.jkrude.controller;

import com.jkrude.category.CategoryNode;
import com.jkrude.category.Rule;
import com.jkrude.main.Main;
import com.jkrude.material.AlertBox;
import com.jkrude.material.Model;
import com.jkrude.material.UI.RuleDialog;
import com.jkrude.material.Utility;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

public class HierarchicalCategoryEditor extends Controller {

  @FXML
  private ListView<Rule> ruleView;
  @FXML
  private TreeView<CategoryNode> categoryTreeView;
  @FXML
  private SimpleBooleanProperty invalidatedProperty;


  @FXML
  public void initialize() {
    invalidatedProperty = new SimpleBooleanProperty(false);
    ruleView.setPlaceholder(getRuleViewPlaceholder());
    ruleView.setCellFactory(ruleListView -> new RuleCell());
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
            } else {
              setContextMenu(HierarchicalCategoryEditor.getCMForCategory(this));
              textProperty().unbind();
              textProperty().bind(item.nameProperty());
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
    setTreeViewItems();
  }

  @Override
  public void prepare() {
    if (invalidatedProperty != null && invalidatedProperty.get()) {
      setTreeViewItems();
      invalidatedProperty.set(false);
      categoryTreeView.getSelectionModel().clearSelection();
    }
  }

  @FXML
  private void goBack(ActionEvent actionEvent) {
    Main.goBack();
  }

  private Node getRuleViewPlaceholder() {
    VBox box = new VBox();
    box.setOpaqueInsets(new Insets(32));
    box.setAlignment(Pos.CENTER);
    Label label = new Label("This category has no rules yet");
    Button addRule = new Button("Create first rule.");
    addRule.setOnAction(this::addRuleIfCategorySelected);
    box.getChildren().addAll(label, addRule);
    return box;
  }

  private void addRuleIfCategorySelected(ActionEvent event) {
    TreeItem<CategoryNode> selectedCategory = categoryTreeView.getSelectionModel()
        .getSelectedItem();
    if (selectedCategory == null) {
      AlertBox.showAlert("Error", "No Category selected", null, AlertType.ERROR);
      return;
    }
    CategoryNode category = selectedCategory.getValue();
    addRule(category);
  }

  private void setTreeViewItems() {
    categoryTreeView.setRoot(mapToTreeItems(Model.getInstance().getProfile().getRootCategory()));
    categoryTreeView.getRoot().getValue().streamCollapse().forEach(
        categoryNode -> categoryNode.addListener(observable -> invalidatedProperty.set(true))
    );
  }

  private static ContextMenu getCMForCategory(TreeCell<CategoryNode> cell) {
    ContextMenu cm = new ContextMenu();
    MenuItem iRename = new MenuItem("Rename");
    iRename.setOnAction(actionEvent -> newNameDialog(cell));
    MenuItem iAddChild = new MenuItem("Add subcategory");
    iAddChild.setOnAction(actionEvent -> addSubCategory(cell));
    MenuItem iRemove = new MenuItem("Delete");
    iRemove.setOnAction(actionEvent -> removeCategory(cell));
    // If cell != root => add #changeParent.
    if (cell.getTreeItem().getParent() != null) {
      MenuItem iMove = new MenuItem("Change parent");
      iMove.setOnAction(actionEvent -> changeParent(cell));
      cm.getItems().add(iMove);
    }
    cm.getItems().addAll(iRename, iAddChild, iRemove);
    return cm;
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
      AlertBox.showAlert("Category already existing!", "The chosen name is already used",
          "Please choose a different name", AlertType.WARNING);
      return;
    }
    CategoryNode newCategory = new CategoryNode(result.get());
    node.addCategory(newCategory);
  }

  private static void removeCategory(TreeCell<CategoryNode> cell) {
    // Get parent TreeItem -> get parent::item(CategoryNode) -> remove cell::item(CategoryNode).
    cell.getTreeItem().getParent().getValue().removeCategory(cell.getItem());
  }

  private static void changeParent(TreeCell<CategoryNode> cell) {
    CategoryNode node = cell.getItem();
    CategoryNode parent = cell.getTreeItem().getParent().getValue();
    // TODO: set ChoiceDialog::Labels::Text to CategoryNode::Name
    ChoiceDialog<CategoryNode> choiceDialog = new ChoiceDialog<>(node,
        node.getRoot().streamCollapse().collect(Collectors.toList()));
    Utility.setCellFactory(choiceDialog, HierarchicalCategoryEditor::categoryConverter);
    Optional<CategoryNode> optCat = choiceDialog.showAndWait();
    if (optCat.isPresent() && !optCat.get().equals(node)) {
      optCat.get().addCategory(node);
      parent.removeCategory(node);
    }
  }

  private static ContextMenu getCMForRule(ListCell<Rule> cell) {
    ContextMenu cm = new ContextMenu();
    MenuItem iEdit = new MenuItem("Edit");
    iEdit.setOnAction(actionEvent -> addRule(cell));
    MenuItem iRemove = new MenuItem("Remove");
    iRemove.setOnAction(actionEvent -> removeRule(cell));
    cm.getItems().addAll(iEdit, iRemove);
    return cm;
  }

  private static void removeRule(ListCell<Rule> cell) {
    Rule rule = cell.getItem();
    CategoryNode parent = rule.getParent().orElseThrow();
    parent.removeRule(rule);
  }

  private static void addRule(ListCell<Rule> cell) {
    new RuleDialog()
        .editRuleShowAndWait(cell.getItem())
        .ifPresent(rule -> cell.getListView().getItems().set(cell.getIndex(), rule));
  }

  private static void addRule(CategoryNode node) {
    new RuleDialog()
        .showAndWait()
        .ifPresent(node::addRule);
  }

  private TreeItem<CategoryNode> mapToTreeItems(CategoryNode root) {
    TreeItem<CategoryNode> rootItem = new TreeItem<>(root);
    root.childNodesRO().addListener(getNodeListChangeListener(rootItem));
    root.childNodesRO().forEach(category -> addCategoryRecursive(category, rootItem));
    return rootItem;
  }

  private void addCategoryRecursive(CategoryNode categoryNode, TreeItem<CategoryNode> parent) {
    TreeItem<CategoryNode> treeItem = new TreeItem<>(categoryNode);
    parent.getChildren().add(treeItem);
    categoryNode.childNodesRO().addListener(getNodeListChangeListener(treeItem));
    categoryNode.childNodesRO().forEach(category -> addCategoryRecursive(category, treeItem));
  }

  private ListChangeListener<CategoryNode> getNodeListChangeListener(
      TreeItem<CategoryNode> rootItem) {
    // Reflect changes from categoryNode within TreeView.
    return change -> {
      while (change.next()) {
        if (change.wasAdded()) {
          for (var node : change.getAddedSubList()) {
            addCategoryRecursive(node, rootItem);
          }
        }
        if (change.wasRemoved()) {
          rootItem.getChildren().removeIf(item -> change.getRemoved().contains(item.getValue()));
        }
      }
    };

  }

  private static void showAlertForEmptyInput() {
    AlertBox.showAlert("Incorrect input!", "The name can not be empty", "",
        AlertType.INFORMATION);
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

  private static class RuleCell extends ListCell<Rule> {

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
        setContextMenu(getCMForRule(this));
      }
    }
  }
}
