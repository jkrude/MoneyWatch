package com.jkrude.category;

import static com.jkrude.material.TestTools.currentTotalMatchedTransactions;
import static com.jkrude.material.TestTools.sum;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.jkrude.material.PropertyFilteredList;
import com.jkrude.material.TestData;
import com.jkrude.transaction.ExtendedTransaction;
import com.jkrude.transaction.Transaction.TransactionField;
import com.jkrude.transaction.TransactionContainer;
import java.text.ParseException;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import org.junit.Before;
import org.junit.Test;


public class CategoryValueNodeTest {

  private CategoryNode rootCategory;
  private TransactionContainer transactions;
  private CategoryValueNode rootNode;


  @Before
  public void setUp() {
    rootCategory = TestData.getProfile().getRootCategory();
    transactions = TestData.getNewCamtWithTestData();
    rootNode = CategoryValueTree.buildTree(rootCategory, transactions.getSourceRO()).getRoot();
  }

  @Test
  public void addCategory() throws ParseException {
    Rule newRule = Rule.RuleBuilder.fromPair(
        new Pair<>(TransactionField.OTHER_PARTY, "Extra")).build();
    CategoryNode extraCategory = new CategoryNode("Extra", List.of(newRule));
    rootCategory.addCategory(extraCategory);
    assertEquals(sum(currentTotalMatchedTransactions(rootCategory, transactions)),
        rootNode.getValue(), 0.001);
  }

  @Test
  public void removeCategory() {
    var category = rootCategory.childNodesRO().get(0);
    rootCategory.removeCategory(category);
    assertEquals(sum(currentTotalMatchedTransactions(rootCategory, transactions)),
        rootNode.getValueBinding().get(), 0.001);
  }

  @Test
  public void changeIsActive() {
    currentTotalMatchedTransactions(rootCategory, transactions)
        .forEach(et -> et.setIsActive(false));
    assertEquals(0d, rootNode.getValue(), 0.001);
    var transaction = currentTotalMatchedTransactions(rootCategory, transactions).stream()
        .findFirst().orElseThrow();
    transaction.setIsActive(true);
    assertEquals(transaction.getBaseTransaction().getMoneyAmount().getRawAmount().doubleValue(),
        rootNode.getValue(), 0.001);
  }


  @Test
  public void stream() {
    long count = rootNode.stream()
        .filter(
            node -> rootCategory.streamCollapse().anyMatch(cat -> cat.equals(node.getCategory())))
        .count();
    assertEquals(rootCategory.streamCollapse().count(), count);
  }

  @Test
  public void getCategory() {
    assertEquals(rootCategory, rootNode.getCategory());
  }


  @Test
  public void getValueBinding() {
    assertNotNull(rootNode.getValueBinding());
    assertEquals(sum(currentTotalMatchedTransactions(rootCategory, transactions)),
        rootNode.getValueBinding().get(), 0.001);
  }

  @Test
  public void getValue() {
    assertEquals(rootNode.getValueBinding().get(), rootNode.getValue(), 0.001);
    assertEquals(sum(currentTotalMatchedTransactions(rootCategory, transactions)),
        rootNode.getValue(), 0.001);
  }

  @Test
  public void getChildren() {
    assertNotNull(rootNode.getChildren());
    for (CategoryValueNode child : rootNode.getChildren()) {
      assertTrue(rootCategory.childNodesRO().contains(child.getCategory()));
    }
  }

  @Test
  public void getMatchedTransactions() {
    ObservableList<ExtendedTransaction> reduce = rootNode.stream()
        .map(CategoryValueNode::getMatchedTransactions)
        .map(PropertyFilteredList::getBaseList)
        .reduce(FXCollections.observableArrayList(), (list1, list2) -> {
          list1.addAll(list2);
          return list1;
        });
    for (ExtendedTransaction transaction : currentTotalMatchedTransactions(rootCategory,
        transactions)) {
      assertTrue(reduce.contains(transaction));
    }

  }

}