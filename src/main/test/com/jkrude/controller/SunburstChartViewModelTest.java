package com.jkrude.controller;

import static com.jkrude.material.TestTools.currentTotalMatchedTransactions;
import static com.jkrude.material.TestTools.sum;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.jkrude.category.CategoryNode;
import com.jkrude.category.Rule;
import com.jkrude.material.Model;
import com.jkrude.material.TestData;
import com.jkrude.transaction.ExtendedTransaction;
import com.jkrude.transaction.Transaction.TransactionField;
import com.jkrude.transaction.TransactionContainer;
import eu.hansolo.fx.charts.data.ChartItem;
import eu.hansolo.fx.charts.data.TreeNode;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import org.junit.Before;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;


// If test fails with "module was not exported" add
// --add-exports javafx.graphics/com.sun.javafx.application=ALL-UNNAMED
// to your Run-Configuration
public class SunburstChartViewModelTest extends ApplicationTest {

  private SunburstChartViewModel viewModel;
  private CategoryNode rootCategory;
  private TransactionContainer transactions;
  private ExtendedTransaction extraTransaction;

  @Before
  public void setUp() throws Exception {
    var profile = TestData.getProfile();
    rootCategory = profile.getRootCategory();
    Model.getInstance().setProfile(profile);
    transactions = TestData.getNewCamtWithTestData();

    extraTransaction = TestData.getExtraTransaction();
    transactions.addExtendedTransaction(extraTransaction);
    Model.getInstance().getTransactionContainerList().add(transactions);
    Model.getInstance().setActiveData(transactions);
    viewModel = new SunburstChartViewModel();
  }

  @Test
  public void getAdaptedRootTest() {
    var adaptedRoot = viewModel.getAdaptedRoot();
    assertEquals(sum(currentTotalMatchedTransactions(rootCategory, transactions)),
        adaptedRoot.getItem().getValue(), 0.001);
    var nameSet = rootCategory.streamCollapse().map(CategoryNode::getName)
        .collect(Collectors.toSet());
    long count = adaptedRoot.stream().map(TreeNode::getItem).map(ChartItem::getName).filter(
        nameSet::contains
    ).count();
    assertEquals(nameSet.size(), count);
  }


  @Test
  public void getAdaptedRootTestIgnored() {
    var someTransaction = transactions.getSourceRO().stream()
        .filter(transaction -> !transaction.getBaseTransaction().isPositive()).findFirst()
        .orElseThrow();
    someTransaction.setIsActive(false);
    var adaptedRoot = viewModel.getAdaptedRoot();
    assertEquals(sum(currentTotalMatchedTransactions(rootCategory, transactions))
            - someTransaction.getBaseTransaction().getMoneyAmount().getRawAmount().doubleValue(),
        adaptedRoot.getItem().getValue(), 0.001);
  }

  @Test
  public void getAdaptedRootTestIgnoredUndefined() {
    extraTransaction.setIsActive(false);
    var adaptedRoot = viewModel.getAdaptedRoot();
    // Find undefined-node in adapted-Tree
    adaptedRoot.getChildren();
    double undefinedNodeAmount = Double.NaN;
    for (var node : adaptedRoot.getChildren()) {
      if (node.getItem().getName().equals(SunburstChartViewModel.UNDEFINED_SEGMENT)) {
        undefinedNodeAmount = node.getItem().getValue();
      }
    }
    assertEquals(0d, undefinedNodeAmount, 0.001);
  }

  @Test
  public void getAdaptedRootDefineTest() throws ParseException {
    var adaptedRoot = viewModel.getAdaptedRoot();
    var undefSegOpt = adaptedRoot.getChildren().stream()
        .filter(n -> n.getItem().getName().equals(SunburstChartViewModel.UNDEFINED_SEGMENT))
        .findFirst();
    assertTrue(undefSegOpt.isPresent());
    Rule extra = Rule.RuleBuilder.fromPair(TransactionField.OTHER_PARTY, "Extra")
        .build();
    Model.getInstance().getProfile().getRootCategory().addRule(extra);
    assertEquals(0d, undefSegOpt.get().getItem().getValue(), 0.001);
  }

  @Test
  public void possibleActiveDataChange() {
    // Tests isInvalidated and addChangeListener too
    TransactionContainer container = new TransactionContainer();
    assertNotEquals(0d, viewModel.getAdaptedRoot().getItem().getValue(), 0.001);

    viewModel.possibleActiveDataChange(container);
    assertEquals(Model.getInstance().getActiveData(), container);
    assertTrue(viewModel.isInvalidated());
    assertEquals(0d, viewModel.getAdaptedRoot().getItem().getValue(), 0.001);
  }

  @Test
  public void hasActiveDataPropertyTest() {
    assertTrue(viewModel.hasActiveDataProperty());
  }

  private ObservableList<ExtendedTransaction> getTransactionsForSegment(String name) {
    return viewModel.getTransactionsForSegment(name).listProperty.get();
  }

  @Test
  public void getTransactionsForSegmentTest() {
    // TODO getAdaptedRoot() has to be called to fill nameToDataMap.
    viewModel.getAdaptedRoot();
    String undefined = SunburstChartViewModel.UNDEFINED_SEGMENT;
    var matchedTransactions = currentTotalMatchedTransactions(rootCategory, transactions);
    var undefinedTransactions = FXCollections
        .observableArrayList(transactions.getSourceRO().stream().filter(
            et -> !et.getBaseTransaction().isPositive() && !matchedTransactions.contains(et)
        ).collect(Collectors.toList()));
    assertEquals(undefinedTransactions, getTransactionsForSegment(SunburstChartViewModel.UNDEFINED_SEGMENT));

    var categoryNode = rootCategory.streamCollapse()
        .filter(category -> category.getName().equals("Rent")).findFirst().orElseThrow();
    var matchedTransactionsForCategory = transactions.getSourceRO().stream()
        .filter(
            t -> !t.getBaseTransaction().isPositive() && categoryNode.rulesRO().stream().anyMatch(
                rule -> rule.getPredicate().test(t.getBaseTransaction())
            )).collect(Collectors.toList());

    assertEquals(FXCollections.observableArrayList(matchedTransactionsForCategory),
        getTransactionsForSegment("Rent"));

    // Test ignored transaction still matching.
    matchedTransactionsForCategory.get(0).setIsActive(false);
    assertEquals(FXCollections.observableArrayList(matchedTransactionsForCategory),
        getTransactionsForSegment("Rent"));
  }

  private int[] preInvalidationTest() {
    final int[] counter = {0};
    ChangeListener<Boolean> changeListener = (observableValue, oldProp, newProp) ->
    {
      if (newProp != oldProp) {
        counter[0]++;
      }
    };
    viewModel.addChangeListener(changeListener);
    assertEquals(0, counter[0]);
    assertFalse(viewModel.isInvalidated());
    return counter;
  }

  private void postInvalidationTest(int[] counter) {
    assertTrue(viewModel.isInvalidated());
    assertEquals(1, counter[0]);
  }


  @Test
  public void invalidationTestRemoveChild() {
    // Model should invalidate if a category-node gets removed.
    var counter = preInvalidationTest();
    var childNode = rootCategory.childNodesRO().get(0);
    rootCategory.removeCategory(childNode);
    postInvalidationTest(counter);
  }

  @Test
  public void invalidationTestAddChild() throws ParseException {
    // Model should invalidate if a category-node was added.
    var counter = preInvalidationTest();
    var extraCategory = new CategoryNode("Extra", List.of(
        Rule.RuleBuilder.fromPair(TransactionField.OTHER_PARTY, "Extra").build()));
    rootCategory.addCategory(extraCategory);
    postInvalidationTest(counter);

  }

  @Test
  public void invalidationTestIgnoreTransaction() {
    //FIXME has to be called to update nameTo... map
    getAdaptedRootTest();
    // Model should invalidate if any matched transaction ist set inactive.
    var counter = preInvalidationTest();
    var matchedTransactions = getTransactionsForSegment("Rent");
    assertFalse(matchedTransactions.isEmpty());
    matchedTransactions.stream()
        .filter(ExtendedTransaction::isActive)
        .findFirst().orElseThrow()
        .setIsActive(false);
    assertTrue(viewModel.isInvalidated());
    postInvalidationTest(counter);
  }

  @Test
  public void invalidationTestIgnoreUndefined() {
    // FIXME has to be called to update nameTo... map
    getAdaptedRootTest();
    // Model should invalidate even if an unmatched transaction ist set inactive.
    var counter = preInvalidationTest();
    var undefinedTransactions = getTransactionsForSegment(SunburstChartViewModel.UNDEFINED_SEGMENT);
    assertFalse(undefinedTransactions.isEmpty()); // Extra-Transaction should be in undefined.
    undefinedTransactions.stream()
        .filter(ExtendedTransaction::isActive)
        .findFirst().orElseThrow()
        .setIsActive(false);
    postInvalidationTest(counter);
  }

  @Test
  public void invalidationTestChangeColor() {
    var counter = preInvalidationTest();
    // Change category color
    rootCategory.childNodesRO().get().get(0).setColor(Color.ANTIQUEWHITE);
    postInvalidationTest(counter);
  }

}