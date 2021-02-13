package com.jkrude.category;

import static org.junit.Assert.assertEquals;

import com.jkrude.material.Money;
import com.jkrude.material.TestData;
import com.jkrude.material.Utility;
import com.jkrude.transaction.ExtendedTransaction;
import com.jkrude.transaction.Transaction;
import com.jkrude.transaction.Transaction.TransactionField;
import com.jkrude.transaction.TransactionContainer;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.util.Pair;
import org.junit.Before;
import org.junit.Test;

public class TreeChartDataTest {

  private CategoryNode rootCategory;
  private TransactionContainer transactions;
  private TreeChartData tree;

  @Before
  public void setUp() {
    rootCategory = TestData.getProfile().getRootCategory();
    transactions = TestData.getCamtWithTestData();
    tree = TreeChartData.createTree(rootCategory, transactions.getSource());

  }

  @Test
  public void createTree() {
    var matchedTransactions = currentTotalMatchedTransactions();
    assertEquals(sum(matchedTransactions), tree.getValue(), 0.001);
  }

  @Test
  public void addCategory() throws ParseException {
    Rule newRule = Rule.RuleBuilder.fromPair(
        new Pair<>(TransactionField.OTHER_PARTY, "Extra")).build();
    CategoryNode extraCategory = new CategoryNode("Extra", List.of(newRule));
    rootCategory.addCategory(extraCategory);
    assertEquals(sum(currentTotalMatchedTransactions()), tree.getValue(), 0.001);
  }

  @Test
  public void removeCategory() {
    var category = rootCategory.childNodesRO().get(0);
    rootCategory.removeCategory(category);
    assertEquals(sum(currentTotalMatchedTransactions()), tree.getValueBinding().get(), 0.001);
  }

  @Test
  public void changeIsActive() {
    currentTotalMatchedTransactions().forEach(et -> et.setIsActive(false));
    assertEquals(0d, tree.getValue(), 0.001);
    var transaction = currentTotalMatchedTransactions().stream().findFirst().orElseThrow();
    transaction.setIsActive(true);
    assertEquals(transaction.getBaseTransaction().getMoneyAmount().getRawAmount().doubleValue(),
        tree.getValue(), 0.001);
  }

  @Test
  public void changeDataSource() {
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
    TreeChartData.changeSource(newSource, tree);
    assertEquals(-5.27, tree.getValue(), 0.001);

  }

  private Set<ExtendedTransaction> currentTotalMatchedTransactions() {
    Set<ExtendedTransaction> matchedTransactions = new HashSet<>();
    rootCategory.streamCollapse().forEach(
        node -> node.rulesRO()
            .forEach(
                rule -> {
                  for (var t : transactions.getSource()) {
                    if (rule.getPredicate().test(t.getBaseTransaction())) {
                      matchedTransactions.add(t);
                    }
                  }
                })
    );
    return matchedTransactions;
  }

  private double sum(Set<ExtendedTransaction> s) {
    double total = 0d;
    for (var t : s) {
      total += t.getBaseTransaction().getMoneyAmount().getRawAmount().doubleValue();
    }
    return total;
  }

}