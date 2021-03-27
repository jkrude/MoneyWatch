package com.jkrude.material;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.jkrude.transaction.ExtendedTransaction;
import java.math.BigDecimal;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import org.junit.Test;

public class UtilityTest {

  @Test
  public void bindToSumOfListTest() {

    ObservableList<ExtendedTransaction> list = FXCollections
        .observableArrayList(TestData.getNewCamtWithTestData().getSourceRO());
    DoubleProperty sumOfList = Utility.bindToSumOfList(list);
    double expected = list.stream()
        .map(t -> t.getBaseTransaction().getMoneyAmount().getRawAmount()).mapToDouble(
            BigDecimal::doubleValue).sum();
    assertEquals(expected, sumOfList.get(), 0.01);

    var removed = list.remove(0);
    expected -= removed.getBaseTransaction().getMoneyAmount().getRawAmount().doubleValue();
    assertEquals(expected, sumOfList.get(), 0.01);

    double added = 3.141;
    removed.getBaseTransaction().getMoneyAmount().setAmount(added);
    list.add(removed);
    expected += added;
    assertEquals(expected, sumOfList.get(), 0.01);

    list.clear();
    assertEquals(0d, sumOfList.get(), 0.001);
  }

  @Test
  public void bindList2SetTest() {
    ObservableSet<Integer> observableSet = FXCollections.observableSet(1, 2, 3);
    ReadOnlyListWrapper<Integer> list = Utility.bindList2Set(observableSet);
    for (Integer integer : observableSet) {
      assertTrue(list.contains(integer));
    }
    assertEquals(observableSet.size(), list.size());

    observableSet.add(3);
    assertEquals(observableSet.size(), list.size());

    observableSet.add(4);
    assertTrue(list.contains(4));
    observableSet.remove(4);
    assertFalse(list.contains(4));
  }
}