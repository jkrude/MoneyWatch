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
import java.util.stream.Collectors;
import javafx.util.Pair;
import org.junit.Before;
import org.junit.Test;

public class TreeNodeAdapterTest {

  private CategoryValueTree tree;
  private Map<String, CategoryValueNode> nameMap;
  TreeNode<ChartItem> adaptedRoot;

  @Before
  public void setUp() throws Exception {
    tree = CategoryValueTree.buildTree(
        TestData.getProfile().getRootCategory(),
        TestData.getNewCamtWithTestData().getSourceRO());
    nameMap = new HashMap<>();
    adaptedRoot = TreeNodeAdapter.asTreeNode(tree, nameMap);
  }

  @Test
  public void asTreeNodeTest() {
    tree.getRoot().stream().forEach(
        node -> assertTrue(nameMap.containsKey(node.getCategory().getName()))
    );
    adaptedRoot.stream().forEach(
        adaptedNode -> {
          var node = nameMap.get(adaptedNode.getItem().getName());
          var childNamesSet = node.getChildren().stream().map(c -> c.getCategory().getName())
              .collect(
                  Collectors.toSet());
          for (var adaptedChild : adaptedNode.getChildren()) {
            assertTrue(
                childNamesSet.contains(adaptedChild.getItem().getName()));
          }
        }
    );
    assertEquals(tree.getRoot().getValueBinding().get(), adaptedRoot.getItem().getValue(), 0.001);
  }

  @Test
  public void removeCategory() {
    CategoryValueNode child0 = tree.getRoot().getChildren().get(0);
    tree.getRoot().getCategory().removeCategory(child0.getCategory());
    assertFalse(tree.getRoot().getChildren().contains(child0));
    assertTrue(adaptedRoot.getChildren().stream()
        .map(TreeNode::getItem)
        .map(ChartItem::getName)
        .noneMatch(name -> name.equals(child0.getCategory().getName())));
    for (var c : child0.getChildren()) {
      assertFalse(nameMap.containsKey(c.getCategory().getName()));
    }
    assertEquals(tree.getRoot().getValueBinding().get(), adaptedRoot.getItem().getValue(), 0.001);
  }

  @Test
  public void addCategory() throws ParseException {
    Rule newRule = Rule.RuleBuilder.fromPair(
        new Pair<>(TransactionField.OTHER_PARTY, "Extra")).build();
    CategoryNode extraCategory = new CategoryNode("Extra", List.of(newRule));
    tree.getRoot().getCategory().addCategory(extraCategory);
    assertEquals(1, adaptedRoot.getChildren().stream()
        .map(TreeNode::getItem)
        .map(ChartItem::getName)
        .filter(name -> name.equals(extraCategory.getName()))
        .count());
    assertEquals(tree.getRoot().getValueBinding().get(), adaptedRoot.getItem().getValue(), 0.001);
  }

  @Test
  public void changeAccVal() {
    var node = tree.getRoot().getChildren().get(0).getChildren().get(1);
    var extendedTransaction = node.getMatchedTransactions().get(0);
    extendedTransaction.setIsActive(false);
    assertEquals(tree.getRoot().getValueBinding().get(), adaptedRoot.getItem().getValue(), 0.001);
  }

  @Test
  public void changeCategoryName() {
    tree.getRoot().getCategory().nameProperty().set("");
    assertEquals(tree.getRoot().getCategory().getName(), adaptedRoot.getItem().getName());
  }

}
