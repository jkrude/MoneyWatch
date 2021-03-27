package com.jkrude.controller;

import static com.jkrude.material.TestTools.currentTotalMatchedTransactions;
import static com.jkrude.material.TestTools.sum;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.jkrude.category.CategoryNode;
import com.jkrude.material.Model;
import com.jkrude.material.TestData;
import com.jkrude.transaction.TransactionContainer;
import eu.hansolo.fx.charts.data.ChartItem;
import eu.hansolo.fx.charts.data.TreeNode;
import java.util.stream.Collectors;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import org.junit.Before;
import org.junit.Test;

public class SunburstChartViewModelTest {

  SunburstChartViewModel viewModel;
  CategoryNode rootCategory;
  TransactionContainer transactions;

  @Before
  public void setUp() throws Exception {
    var profile = TestData.getProfile();
    rootCategory = profile.getRootCategory();
    Model.getInstance().setProfile(profile);
    transactions = TestData.getNewCamtWithTestData();

    transactions.addExtendedTransaction(TestData.getExtraTransaction());
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
  public void possibleActiveDataChange() {
    // Tests isInvalidated and addChangeListener too
    final boolean[] change = new boolean[1];
    final int[] changeCalled = {0};
    viewModel.addChangeListener(new ChangeListener<>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldV,
          Boolean newV) {
        changeCalled[0]++;
        change[0] = newV;
      }
    });
    TransactionContainer container = new TransactionContainer();
    viewModel.possibleActiveDataChange(container);
    assertEquals(Model.getInstance().getActiveData(), container);
    assertTrue(viewModel.isInvalidated());
    assertTrue(change[0]);
    assertEquals(1, changeCalled[0]);
  }

  @Test
  public void hasActiveDataPropertyTest() {
    assertTrue(viewModel.hasActiveDataProperty());
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
    assertEquals(undefinedTransactions, viewModel.getTransactionsForSegment(undefined));

    var categoryNode = rootCategory.streamCollapse()
        .filter(category -> category.getName().equals("Rent")).findFirst().orElseThrow();
    var matchedTransactionsForCategory = transactions.getSourceRO().stream()
        .filter(
            t -> !t.getBaseTransaction().isPositive() && categoryNode.rulesRO().stream().anyMatch(
                rule -> rule.getPredicate().test(t.getBaseTransaction())
            )).collect(Collectors.toList());

    assertEquals(FXCollections.observableArrayList(matchedTransactionsForCategory),
        viewModel.getTransactionsForSegment("Rent"));

    // Test ignored transaction still matching.
    matchedTransactionsForCategory.get(0).setIsActive(false);
    assertEquals(FXCollections.observableArrayList(matchedTransactionsForCategory),
        viewModel.getTransactionsForSegment("Rent"));
  }


}