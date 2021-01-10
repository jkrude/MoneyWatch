package com.jkrude.category;

import eu.hansolo.fx.charts.data.ChartItem;
import eu.hansolo.fx.charts.data.TreeNode;
import java.util.Map;
import javafx.collections.ListChangeListener;
import javafx.scene.paint.Color;


public class TreeNodeAdapter {

  private static Color[] defaultColors = {
      Color.web("#66545e"),
      Color.web("#aa6f73"),
      Color.web("#eea990"),
      Color.web("#f6e0b5"),
      Color.web("#97ebdb"),
      Color.web("#00c2c7"),
      Color.web("#0086ad"),
      Color.web("#005582"),

  };
  private static int idx = 0;

  private static int getIdx() {
    return idx++;
  }

  private static void resetIdx() {
    idx = 0;
  }

  private static Color getNextColor() {
    return defaultColors[getIdx() % defaultColors.length];
  }

  public static TreeNode<ChartItem> asTreeNode(TreeChartData rootData,
      Map<String, TreeChartData> nameToDataMap) {
    TreeNode<ChartItem> root = new TreeNode<>(createChartItem(rootData));
    addChildrenRec(rootData, root, nameToDataMap, true);
    resetIdx();
    return root;
  }

  private static void addChildrenRec(
      TreeChartData treeChartData,
      TreeNode<ChartItem> parent,
      Map<String, TreeChartData> nameToDataMap,
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

  private static ChartItem createChartItem(TreeChartData treeChartData) {
    ChartItem chartItem = new ChartItem();
    chartItem.valueProperty().bind(treeChartData.getValueBinding());
    chartItem.nameProperty().bind(treeChartData.getCategory().nameProperty());
    chartItem.setFill(treeChartData.getCategory().getColor());
    return chartItem;
  }

  private static void listenForChanges(
      TreeChartData treeChartData,
      TreeNode<ChartItem> parent,
      TreeNode<ChartItem> node,
      Map<String, TreeChartData> nameToDataMap) {
    treeChartData.getChildren().addListener(new ListChangeListener<TreeChartData>() {
      @Override
      public void onChanged(Change<? extends TreeChartData> change) {
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
      TreeChartData treeChartData,
      TreeNode<ChartItem> parent,
      Map<String, TreeChartData> nameToDataMap) {

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
