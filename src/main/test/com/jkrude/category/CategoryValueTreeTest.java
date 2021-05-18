package com.jkrude.category;

import static com.jkrude.material.TestTools.currentTotalMatchedTransactions;
import static com.jkrude.material.TestTools.sum;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.jkrude.material.Money;
import com.jkrude.material.Profile;
import com.jkrude.material.TestData;
import com.jkrude.material.Utility;
import com.jkrude.transaction.ExtendedTransaction;
import com.jkrude.transaction.Transaction;
import com.jkrude.transaction.Transaction.TransactionField;
import com.jkrude.transaction.TransactionContainer;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import org.junit.Before;
import org.junit.Test;

public class CategoryValueTreeTest {

  private CategoryNode rootCategory;
  private TransactionContainer transactions;
  private CategoryValueTree tree;
  private Profile workingProfile;
  private ExtendedTransaction extraTransaction;
  private TransactionContainer testTransactions;

  @Before
  public void setUp() throws Exception {
    workingProfile = TestData.getProfile();
    extraTransaction = TestData.getExtraTransaction();
    testTransactions = TestData.getNewCamtWithTestData();
    testTransactions.addExtendedTransaction(extraTransaction);
    rootCategory = workingProfile.getRootCategory();
    transactions = testTransactions;
    tree = CategoryValueTree.buildTree(rootCategory, transactions.getSourceRO());
  }

  @Test
  public void buildTree() {
    assertNotNull(tree);
    assertNotNull(tree.getRoot());
    assertEquals(sum(currentTotalMatchedTransactions(rootCategory, transactions)),
        tree.getRoot().getValue(), 0.001);
  }

  @Test
  public void changeSource() {
    var newSource = FXCollections.observableArrayList(
        new ExtendedTransaction(
            new Transaction(
                LocalDate.parse("25.09.19", Utility.DATE_TIME_FORMATTER),
                "DE92500105176644294936",
                LocalDate.parse("25.09.19", Utility.DATE_TIME_FORMATTER),
                "Card payment",
                "2019-09-24T18:25 Debitk.1 2022-12",
                "", "",
                "6.50321626180002E+025", "", "", "",
                "DISCOUNTER1",
                "DE43500105171223732312",
                "HYVEDEMM447",
                new Money(-5.27),
                "Revenue booked"
            )));
    tree.changeSource(newSource);
    assertEquals(-5.27, tree.getRoot().getValue(), 0.001);

  }

  @Test
  public void getUnmatchedTransactions() {
    // Tested more in addInvalidationListener
    for (var t : currentTotalMatchedTransactions(rootCategory, transactions)) {
      assertFalse(tree.getUnmatchedTransactions().contains(t));
    }
  }


  @Test
  public void calcDoubleMatchedTransactions() throws ParseException {
    assertEquals(0, tree.calcDoubleMatchedTransactions().size());
    rootCategory.addRule(
        Rule.RuleBuilder.fromPair(TransactionField.OTHER_PARTY, "Max Mustermann")
            .build());
    assertEquals(1, tree.calcDoubleMatchedTransactions().size());
    //TODO
  }

  @Test
  public void addInvalidationListener() throws ParseException {

    final int[] called = {0};
    InvalidationListener listener = observable -> called[0]++;
    tree.addListener(listener);

    // Add a new category:
    Rule newRule = Rule.RuleBuilder.fromPair(
        TransactionField.OTHER_PARTY, "Extra").build();
    CategoryNode extraCategory = new CategoryNode("Extra", List.of(newRule));

    rootCategory.addCategory(extraCategory);
    // The listener should have been called.
    assertTrue(called[0] > 0);
    // The listener should only be called once.
    assertEquals(1, called[0]);
    assertFalse(tree.getUnmatchedTransactions().contains(this.extraTransaction));
    called[0] = 0; // reset

    // Ignore a transaction:
    double preValue = tree.getRoot().getValue();
    this.extraTransaction.setIsActive(false);
    assertEquals(1, called[0]);
    assertNotEquals(preValue, tree.getRoot().getValue(), 0.001);
    called[0] = 0; // reset

    // Remove category:
    rootCategory.removeCategory(extraCategory);
    assertEquals(1, called[0]);
    assertTrue(tree.getUnmatchedTransactions().contains(this.extraTransaction));
    called[0] = 0; // reset

    // Change source:
    tree.changeSource(TestData.getNewCamtWithTestData().getSourceRO());
    assertEquals(0, called[0]);
  }

  @Test
  public void removeListener() {
    final int[] called = {0};
    InvalidationListener listener = observable -> called[0]++;
    tree.addListener(listener);
    tree.removeListener(listener);
    rootCategory.removeCategory(rootCategory.childNodesRO().get().get(0));
    assertEquals(0, called[0]);
  }

}