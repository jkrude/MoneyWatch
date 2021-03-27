package com.jkrude.category;

import eu.hansolo.fx.charts.data.ChartItem;
import eu.hansolo.fx.charts.data.TreeNode;
import java.util.Map;
import javafx.collections.ListChangeListener;


public class TreeNodeAdapter {

  public static TreeNode<ChartItem> asTreeNode(CategoryValueTree tree,
      Map<String, CategoryValueNode> nameToDataMap) {
    TreeNode<ChartItem> root = new TreeNode<>(createChartItem(tree.getRoot()));
    addChildrenRec(tree.getRoot(), root, nameToDataMap, true);
    return root;
  }

  private static void addChildrenRec(
      CategoryValueNode treeChartData,
      TreeNode<ChartItem> parent,
      Map<String, CategoryValueNode> nameToDataMap,
      boolean isRoot) {

    TreeNode<ChartItem> node;
    if (isRoot) {
      // create root
      node = parent;
    } else {
      // create node
      ChartItem item = createChartItem(treeChartData);
      node = new TreeNode<>(item, parent);
      parent.addNode(node);
    }
    nameToDataMap.put(treeChartData.getCategory().getName(), treeChartData);
    listenForChanges(treeChartData, parent, node, nameToDataMap);
    treeChartData.getChildren().forEach(child -> addChildrenRec(child, node, nameToDataMap, false));
  }

  private static ChartItem createChartItem(CategoryValueNode treeChartData) {
    ChartItem chartItem = new ChartItem();
    chartItem.valueProperty().bind(treeChartData.getValueBinding());
    chartItem.nameProperty().bind(treeChartData.getCategory().nameProperty());
    chartItem.setFill(treeChartData.getCategory().getColor());
    return chartItem;
  }

  private static void listenForChanges(
      CategoryValueNode treeChartData,
      TreeNode<ChartItem> parent,
      TreeNode<ChartItem> node,
      Map<String, CategoryValueNode> nameToDataMap) {
    treeChartData.getChildren().addListener(new ListChangeListener<>() {
      @Override
      public void onChanged(Change<? extends CategoryValueNode> change) {
        while (change.next()) {
          if (change.wasAdded()) {
            for (var added : change.getAddedSubList()) {
              addChildrenRec(added, parent, nameToDataMap, false);
            }
          }
          if (change.wasRemoved()) {
            for (var removed : change.getRemoved()) {
              removeChildRec(removed, node, nameToDataMap);
            }
          }
        }
      }
    });
  }

  private static void removeChildRec(
      CategoryValueNode treeChartData,
      TreeNode<ChartItem> parent,
      Map<String, CategoryValueNode> nameToDataMap) {

    var it = parent.getChildren().listIterator();
    while (it.hasNext()) {
      var treeNode = it.next();
      if (treeNode.getItem().getName().equals(treeChartData.getCategory().getName())) {
        treeNode.stream().map(TreeNode::getItem).forEach(item -> {
          item.nameProperty().unbind();
          item.valueProperty().unbind();
          nameToDataMap.remove(item.getName());
        });
        it.remove();
        nameToDataMap.remove(treeNode.getItem().getName());
      }
    }
  }
}
