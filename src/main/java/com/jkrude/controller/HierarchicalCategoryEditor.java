package com.jkrude.controller;

import com.jkrude.category.CategoryNode;
import com.jkrude.category.Rule;
import com.jkrude.main.Main;
import com.jkrude.material.AlertBox;
import com.jkrude.material.Model;
import java.util.Optional;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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


  @Override
  public void prepare() {
  }

  @FXML
  public void initialize() {
    backButton.setOnAction(event -> Main.goBack());
    CategoryNode rootNode = Model.getInstance().getProfile().getRootCategory();
    TreeItem<TreeWrapper> root = convertToTreeItems(rootNode);
    categoryTreeView.setRoot(root);
    categoryTreeView.setCellFactory(HierarchicalCategoryEditor::cellFactory);
  }

  private static TreeCell<TreeWrapper> cellFactory(TreeView<TreeWrapper> tree) {
    return new TreeCell<>() {
      @Override
      public void updateItem(TreeWrapper item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
          setText(null);
          setContextMenu(null);
        } else {
          setText(item.toString());
          setContextMenu(HierarchicalCategoryEditor.getContextMenu(this));
        }
      }
    };
  }

  private static ContextMenu getContextMenu(TreeCell<TreeWrapper> cell) {
    assert cell != null && cell.getItem() != null;
    ContextMenu contextMenu = new ContextMenu();
    if (cell.getItem().holdsNode()) {
      MenuItem itemRename = new MenuItem("Rename");
      itemRename.setOnAction(event -> newNameDialog(cell));
      contextMenu.getItems().add(itemRename);
    }
    MenuItem itemDelete = new MenuItem("Delete");
    itemDelete.setOnAction(event -> removeFromTree(cell));
    contextMenu.getItems().add(itemDelete);
    return contextMenu;
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
      } else if (!cell.getItem().toString().equals(result.get())) { //only rename if it is new name
        //FIXME: graphic updates only after next click
        cell.getItem().renameNode(result.get());
        cell.setText(cell.getItem().toString());
      }
    }
  }

  private static void showAlertForEmptyInput() {
    AlertBox.showAlert("Incorrect input!", "The name can not be empty", "",
        AlertType.INFORMATION);
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

    private static long idCounter = 0;
    private long id;
    private final CategoryNode node;
    private final Rule rule;
    private final StringProperty stringProperty;

    public static TreeItem<TreeWrapper> wrapCategory(CategoryNode node) {
      return new TreeItem<>(new TreeWrapper(node));
    }

    public static TreeItem<TreeWrapper> wrapRule(Rule rule) {
      return new TreeItem<>(new TreeWrapper(rule));
    }

    private TreeWrapper(CategoryNode node) {
      this.node = node;
      this.stringProperty = new SimpleStringProperty();
      this.stringProperty.bindBidirectional(node.nameProperty());
      this.rule = null;
      this.id = ++idCounter;
    }

    private TreeWrapper(Rule rule) {
      this.rule = rule;
      this.stringProperty = new SimpleStringProperty(ruleAsString());
      this.node = null;
      this.id = ++idCounter;
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
  }
}
