package com.jkrude.material;

import static org.junit.Assert.*;

import com.jkrude.transaction.ExtendedTransaction;
import java.math.BigDecimal;
import javafx.beans.property.DoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.Before;
import org.junit.Test;

public class UtilityTest {

  @Test
  public void bindToSumOfListTest() {

    ObservableList<ExtendedTransaction> list = TestData.getCamtWithTestData().getSource();
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
}