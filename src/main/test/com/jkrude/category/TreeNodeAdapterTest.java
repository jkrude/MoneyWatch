package com.jkrude.category;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.jkrude.material.TestData;
import com.jkrude.transaction.Transaction.TransactionField;
import eu.hansolo.fx.charts.data.ChartItem;
import eu.hansolo.fx.charts.data.TreeNode;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.util.Pair;
import org.junit.Before;
import org.junit.Test;

public class TreeNodeAdapterTest {

  private TreeChartData root;
  private Map<String, TreeChartData> nameMap;
  TreeNode<ChartItem> adaptedRoot;

  @Before
  public void setUp() throws Exception {
    root = TreeChartData.createTree(
        TestData.getProfile().getRootCategory(),
        TestData.getCamtWithTestData().getSource());
    nameMap = new HashMap<>();
    adaptedRoot = TreeNodeAdapter.asTreeNode(root, nameMap);
  }

  @Test
  public void asTreeNodeTest() {
    root.stream().forEach(
        node -> assertTrue(nameMap.containsKey(node.getCategory().getName()))
    );
    adaptedRoot.stream().forEach(
        adaptedNode -> {
          if (adaptedNode.getParent() != null) {
            var parentName = adaptedNode.getParent().getItem().getName();
            var parentNodeName = nameMap.get(parentName).getCategory().getName();
            var parentNodeName1 = nameMap.get(adaptedNode.getItem().getName()).getParent()
                .orElseThrow().getCategory().getName();
            assertEquals(parentNodeName, parentNodeName1);
          }
        }
    );
    assertEquals(root.getValueBinding().get(), adaptedRoot.getItem().getValue(), 0.001);
  }

  @Test
  public void removeCategory() {
    TreeChartData child0 = root.getChildren().get(0);
    root.getCategory().removeCategory(child0.getCategory());
    assertFalse(root.getChildren().contains(child0));
    assertTrue(adaptedRoot.getChildren().stream()
        .map(TreeNode::getItem)
        .map(ChartItem::getName)
        .noneMatch(name -> name.equals(child0.getCategory().getName())));
    for (var c : child0.getChildren()) {
      assertFalse(nameMap.containsKey(c.getCategory().getName()));
    }
    assertEquals(root.getValueBinding().get(), adaptedRoot.getItem().getValue(), 0.001);
  }

  @Test
  public void addCategory() throws ParseException {
    Rule newRule = Rule.RuleBuilder.fromPair(
        new Pair<>(TransactionField.OTHER_PARTY, "Extra")).build();
    CategoryNode extraCategory = new CategoryNode("Extra", List.of(newRule));
    root.getCategory().addCategory(extraCategory);
    assertEquals(1, adaptedRoot.getChildren().stream()
        .map(TreeNode::getItem)
        .map(ChartItem::getName)
        .filter(name -> name.equals(extraCategory.getName()))
        .count());
    assertEquals(root.getValueBinding().get(), adaptedRoot.getItem().getValue(), 0.001);
  }

  @Test
  public void changeAccVal() {
    var node = root.getChildren().get(0).getChildren().get(1);
    var extendedTransaction = node.getMatchedTransactions().get(0);
    extendedTransaction.setIsActive(false);
    assertEquals(root.getValueBinding().get(), adaptedRoot.getItem().getValue(), 0.001);
  }

  @Test
  public void changeCategoryName() {
    root.getCategory().nameProperty().set("");
    assertEquals(root.getCategory().getName(), adaptedRoot.getItem().getName());
  }

}
