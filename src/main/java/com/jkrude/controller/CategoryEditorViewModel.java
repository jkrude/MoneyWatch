package com.jkrude.controller;

import com.jkrude.category.CategoryNode;
import com.jkrude.material.Model;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class CategoryEditorViewModel implements ViewModel {

  private final Model globalModel;

  public CategoryEditorViewModel() {
    globalModel = Model.getInstance();
  }

  public void setTreeViewItems(TreeView<CategoryNode> categoryTreeView,
      InvalidationListener listener) {
    categoryTreeView.setRoot(mapToTreeItems(globalModel.getProfile().getRootCategory()));
    categoryTreeView.getRoot().getValue().streamCollapse().forEach(
        categoryNode -> categoryNode.addListener(listener)
    );
  }

  private TreeItem<CategoryNode> mapToTreeItems(CategoryNode root) {
    TreeItem<CategoryNode> rootItem = new TreeItem<>(root);
    rootItem.setExpanded(true);
    root.childNodesRO().addListener(getNodeListChangeListener(rootItem));
    root.childNodesRO().forEach(category -> addCategoryRecursive(category, rootItem));
    return rootItem;
  }

  private void addCategoryRecursive(CategoryNode categoryNode, TreeItem<CategoryNode> parent) {
    TreeItem<CategoryNode> treeItem = new TreeItem<>(categoryNode);
    treeItem.setExpanded(true);
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


}
