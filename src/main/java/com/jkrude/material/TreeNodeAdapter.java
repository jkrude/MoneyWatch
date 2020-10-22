package com.jkrude.material;

import com.jkrude.category.TreeChartData;
import eu.hansolo.fx.charts.data.ChartItem;
import eu.hansolo.fx.charts.data.TreeNode;
import java.util.Map;
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
    TreeNode<ChartItem> rootNode = new TreeNode<>(
        new ChartItem(rootData.getCategory().getName(), rootData.valueAsFloat(), getNextColor()),
        null);
    nameToDataMap.put(rootData.getCategory().getName(), rootData);
    rootData.getChildren().forEach(child -> addChildrenRec(child, rootNode, nameToDataMap));
    resetIdx();
    return rootNode;
  }

  private static void addChildrenRec(
      TreeChartData treeChartData,
      TreeNode<ChartItem> parent,
      Map<String, TreeChartData> nameToDataMap) {

    TreeNode<ChartItem> node = new TreeNode<>(
        new ChartItem(treeChartData.getCategory().getName(), treeChartData.valueAsFloat(),
            getNextColor()),
        parent);
    parent.addNode(node);
    nameToDataMap.put(treeChartData.getCategory().getName(), treeChartData);
    treeChartData.getChildren().forEach(child -> addChildrenRec(child, node, nameToDataMap));
  }

}
