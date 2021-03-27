package com.jkrude.material;

import com.jkrude.category.CategoryNode;
import com.jkrude.transaction.ExtendedTransaction;
import com.jkrude.transaction.TransactionContainer;
import java.util.HashSet;
import java.util.Set;

public class TestTools {

  public static Set<ExtendedTransaction> currentTotalMatchedTransactions(
      CategoryNode rootCategory,
      TransactionContainer transactions) {

    Set<ExtendedTransaction> matchedTransactions = new HashSet<>();
    rootCategory.streamCollapse().forEach(
        node -> node.rulesRO()
            .forEach(
                rule -> {
                  for (var t : transactions.getSourceRO()) {
                    if (rule.getPredicate().test(t.getBaseTransaction())) {
                      matchedTransactions.add(t);
                    }
                  }
                })
    );
    return matchedTransactions;
  }

  public static double sum(Set<ExtendedTransaction> s) {
    double total = 0d;
    for (var t : s) {
      total += t.getBaseTransaction().getMoneyAmount().getRawAmount().doubleValue();
    }
    return total;
  }

}
