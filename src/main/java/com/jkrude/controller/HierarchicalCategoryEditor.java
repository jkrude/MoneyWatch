package com.jkrude.controller;

import com.jkrude.category.CategoryNode;
import com.jkrude.category.Rule;
import com.jkrude.main.Main;
import com.jkrude.material.AlertBox;
import com.jkrude.material.Model;
import com.jkrude.material.UI.RuleDialog;
import java.util.Optional;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class HierarchicalCategoryEditor extends Controller {

  @FXML
  private TreeView<TreeWrapper> categoryTreeView;
  @FXML
  private Button backButton;
  private SimpleBooleanProperty invalidatedProperty;

  private static ContextMenu getContextMenu(TreeCell<TreeWrapper> cell) {
    assert cell != null && cell.getItem() != null;
    ContextMenu contextMenu = new ContextMenu();
    if (cell.getItem().holdsNode()) {
      MenuItem itemRename = new MenuItem("Rename");
      itemRename.setOnAction(event -> newNameDialog(cell));
      contextMenu.getItems().add(itemRename);

      MenuItem itemAddChild = new MenuItem("Add SubCategory");
      itemAddChild.setOnAction(event -> addSubCategory(cell));
      contextMenu.getItems().add(itemAddChild);

      if (cell.getItem().node.isLeaf()) {
        MenuItem itemAddRule = new MenuItem("Add Rule");
        itemAddRule.setOnAction(event -> addRule(cell));
        contextMenu.getItems().add(itemAddRule);
      }
    } else {
      MenuItem itemEditRule = new MenuItem("Edit");
      itemEditRule.setOnAction(event -> editRule(cell));
      contextMenu.getItems().add(itemEditRule);
    }
    MenuItem itemDelete = new MenuItem("Delete");
    itemDelete.setOnAction(event -> removeFromTree(cell));
    contextMenu.getItems().add(itemDelete);
    return contextMenu;
  }

  private static void addSubCategory(TreeCell<TreeWrapper> cell) {
    assert cell.getItem().holdsNode();
    var node = cell.getItem().node;
    TextInputDialog textInputDialog = new TextInputDialog("New Category");
    textInputDialog.setTitle("Add the name here");
    Optional<String> result = textInputDialog.showAndWait();
    if (result.isEmpty()) {
      return;
    }
    CategoryNode newCategory = new CategoryNode(result.get());
    node.addCategoryIfPossible(newCategory);
    cell.getTreeItem().getChildren().add(TreeWrapper.wrapCategory(newCategory));
  }

  private static void addRule(TreeCell<TreeWrapper> cell) {
    Optional<Rule> optRule = new RuleDialog().showAndWait();
    if (optRule.isPresent()) {
      TreeItem<TreeWrapper> newRuleItem = TreeWrapper.wrapRule(optRule.get());
      cell.getTreeItem().getChildren().add(newRuleItem);
      cell.getItem().node.addRule(optRule.get());
    }
  }

  private static TreeCell<TreeWrapper> cellFactory(TreeView<TreeWrapper> tree) {

    TreeCell<TreeWrapper> cell = new TreeCell<>() {
      @Override
      public void updateItem(TreeWrapper item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
          setContextMenu(null);
        } else {
          setContextMenu(HierarchicalCategoryEditor.getContextMenu(this));
        }
      }
    };
    cell.itemProperty().isNotNull().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue,
          Boolean newValue) {
        if (newValue) {
          cell.textProperty().bind(cell.itemProperty().get().stringProperty);
        } else {
          cell.textProperty().unbind();
          cell.textProperty().set("");
        }
      }
    });

    return cell;
  }

  private static void removeFromTree(TreeCell<TreeWrapper> cell) {
    TreeItem<TreeWrapper> item = cell.getTreeItem();
    TreeWrapper parent = item.getParent().getValue();
    parent.removeChild(item.getValue());
    item.getParent().getChildren().remove(item);
  }

  private static void newNameDialog(TreeCell<TreeWrapper> cell) {
    TextInputDialog textInputDialog = new TextInputDialog("New name");
    textInputDialog.setHeaderText("");
    textInputDialog.setTitle("Change the name here");
    textInputDialog.getEditor().setText(cell.getItem().toString());
    Optional<String> result = textInputDialog.showAndWait();
    if (result.isPresent()) {
      if (result.get().isBlank()) {
        showAlertForEmptyInput();
        newNameDialog(cell);
      } else if (!cell.getItem().toString().equals(result.get())) { // only rename if it is new name
        cell.getItem().renameNode(result.get());
      }
    }
  }

  private static void showAlertForEmptyInput() {
    AlertBox.showAlert("Incorrect input!", "The name can not be empty", "",
        AlertType.INFORMATION);
  }

  private static void editRule(TreeCell<TreeWrapper> cell) {
    Optional<Rule> optRule = editRuleDialog(cell.getItem().rule,
        cell.getTreeView().getRoot().getValue().node);
    optRule.ifPresent(rule -> cell.getItem().changeRule(rule));
  }

  private static Optional<Rule> editRuleDialog(Rule rule, CategoryNode root) {
    Optional<Rule> optRule = new RuleDialog().editRuleShowAndWait(rule);
    if (optRule.isPresent() && !optRule.get().equals(rule)) {
      Rule editedRule = optRule.get();
      if (root.stream()
          .flatMap(node -> node.leafsRO().stream())
          .anyMatch(rule1 -> rule1.equals(editedRule))) {
        showAlertForExistingRule();
      }
      return Optional.of(editedRule);
    }
    return Optional.empty();
  }

  private static void showAlertForExistingRule() {
    AlertBox.showAlert("Error!", "Rule already exists", "", AlertType.ERROR);
  }

  @Override
  public void prepare() {
    if (invalidatedProperty.get()) {
      categoryTreeView.setRoot(
          convertToTreeItems(Model.getInstance().getProfile().getRootCategory())
      );
      invalidatedProperty.set(false);
    }
  }

  @FXML
  public void initialize() {
    invalidatedProperty = new SimpleBooleanProperty();
    backButton.setOnAction(event -> Main.goBack());
    CategoryNode rootCategory = Model.getInstance().getProfile().getRootCategory();
    rootCategory.stream()
        .forEach(node -> node.addListener(observable -> invalidatedProperty.set(true)));
    TreeItem<TreeWrapper> root = convertToTreeItems(rootCategory);
    categoryTreeView.setRoot(root);
    categoryTreeView.setCellFactory(HierarchicalCategoryEditor::cellFactory);
  }

  private TreeItem<TreeWrapper> convertToTreeItems(CategoryNode rootNode) {
    TreeItem<TreeWrapper> root = TreeWrapper.wrapCategory(rootNode);
    rootNode.leafsRO().forEach(rule -> root.getChildren().add(TreeWrapper.wrapRule(rule)));
    rootNode.childrenRO().forEach(category -> addRecursive(category, root));
    root.setExpanded(!rootNode.childrenRO().isEmpty() || !rootNode.leafsRO().isEmpty());
    return root;
  }

  private void addRecursive(CategoryNode categoryNode, TreeItem<TreeWrapper> parent) {
    TreeItem<TreeWrapper> node = TreeWrapper.wrapCategory(categoryNode);
    parent.getChildren().add(node);
    categoryNode.leafsRO().forEach(rule -> node.getChildren().add(TreeWrapper.wrapRule(rule)));
    categoryNode.childrenRO().forEach(category -> addRecursive(category, node));
  }

  private static class TreeWrapper {

    private final StringProperty stringProperty;
    private CategoryNode node;
    private Rule rule;

    private TreeWrapper(CategoryNode node) {
      this.node = node;
      this.stringProperty = new SimpleStringProperty();
      this.stringProperty.bindBidirectional(node.nameProperty());
      this.rule = null;
    }

    private TreeWrapper(Rule rule) {
      this.rule = rule;
      this.stringProperty = new SimpleStringProperty(ruleAsString());
      this.node = null;
    }

    public static TreeItem<TreeWrapper> wrapCategory(CategoryNode node) {
      return new TreeItem<>(new TreeWrapper(node));
    }

    public static TreeItem<TreeWrapper> wrapRule(Rule rule) {
      return new TreeItem<>(new TreeWrapper(rule));
    }

    private String ruleAsString() {
      assert this.rule != null;
      StringBuilder stringBuilder = new StringBuilder();
      rule.getIdentifierPairs()
          .forEach(
              pair -> stringBuilder.append(pair.getKey()).append(": ").append(pair.getValue())
                  .append(", "));
      stringBuilder.delete(stringBuilder.lastIndexOf(","), stringBuilder.length() - 1);
      return stringBuilder.toString();
    }

    @Override
    public String toString() {
      return stringProperty.get();
    }

    public void renameNode(String s) {
      assert node != null;
      node.nameProperty().set(s);
    }

    public boolean holdsNode() {
      return node != null;
    }

    public void removeChild(TreeWrapper value) {
      assert holdsNode();
      if (value.holdsNode()) {
        this.node.removeCategory(value.node);
      } else {
        this.node.removeRule(value.rule);
      }
    }

    public void changeRule(Rule newRule) {
      assert !holdsNode();
      if (this.rule.getParent().isPresent()) {
        CategoryNode parent = this.rule.getParent().get();
        parent.removeRule(this.rule);
        parent.addRule(newRule);
        newRule.setParent(parent);
        this.rule = newRule;
        this.stringProperty.set(ruleAsString());
      }
    }

  }
}
