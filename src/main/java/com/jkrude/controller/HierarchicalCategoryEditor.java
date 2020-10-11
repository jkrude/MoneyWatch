package com.jkrude.controller;

import com.jkrude.category.CategoryNode;
import com.jkrude.category.Rule;
import com.jkrude.main.Main;
import com.jkrude.material.Model;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
//    ObservableList<AbstractCategory> categories = Model.getInstance().getProfile().getCategories();
//    ParentCategory rootCategory = new ParentCategory("Categories and Rules");
//    categories.forEach(rootCategory::addCategory);
//    TreeItem<AbstractCategory> root = new TreeItem<>(rootCategory);
//    root.setExpanded(!categories.isEmpty());
//    categoryTreeView.setRoot(root);
//    for(AbstractCategory category: categories){
//      TreeItem<AbstractCategory> item = new TreeItem<>(category);
//      root.getChildren().add(item);
//    }
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

    public static TreeItem<TreeWrapper> wrapCategory(CategoryNode node) {

      return new TreeItem<>(new TreeWrapper(node));
    }

    public static TreeItem<TreeWrapper> wrapRule(Rule rule) {
      return new TreeItem<>(new TreeWrapper(rule));
    }

    private TreeWrapper(CategoryNode node) {
      this.node = node;
      this.rule = null;
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

    private TreeWrapper(Rule rule) {
      this.rule = rule;
      this.node = null;
      this.id = ++idCounter;

    }

    @Override
    public String toString() {
      return node == null ? ruleAsString() : node.getName();
    }
  }
}
