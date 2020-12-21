package com.jkrude.material;

import static org.junit.Assert.assertEquals;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.junit.Test;

public class DoubleBindingSumTest {

  @Test
  public void doubleBindingSum() {
    DoubleProperty dp1 = new SimpleDoubleProperty(1);
    DoubleBinding db1 = Utility.asBinding(dp1);
    DoubleProperty dp2 = new SimpleDoubleProperty(2);
    DoubleBinding db2 = Utility.asBinding(dp2);
    DoubleProperty dp3 = new SimpleDoubleProperty(3);
    DoubleBinding db3 = Utility.asBinding(dp3);
    DoubleProperty dp4 = new SimpleDoubleProperty(4);
    DoubleBinding db4 = Utility.asBinding(dp4);

    DoubleProperty dp5 = new SimpleDoubleProperty(5);
    DoubleBinding db5 = Utility.asBinding(dp5);

    DoubleBindingSum sum = new DoubleBindingSum(db1, db2, db3);
    DoubleBindingSum sumSum = new DoubleBindingSum(db4, sum);

    System.out.println("SUM SUM: " + sumSum.get());
    assertEquals(dp1.get() + dp2.get() + dp3.get(), sum.get(), 0.001);
    assertEquals(dp1.get() + dp2.get() + dp3.get() + dp4.get(), sumSum.get(), 0.001);
    dp1.set(0);
    assertEquals(dp1.get() + dp2.get() + dp3.get(), sum.get(), 0.001);
    assertEquals(dp1.get() + dp2.get() + dp3.get() + dp4.get(), sumSum.get(), 0.001);
    dp4.set(0);
    assertEquals(dp1.get() + dp2.get() + dp3.get(), sum.get(), 0.001);
    assertEquals(dp1.get() + dp2.get() + dp3.get() + dp4.get(), sumSum.get(), 0.001);

    sum.addDependency(db5);
    assertEquals(dp1.get() + dp2.get() + dp3.get() + dp5.get(), sum.get(), 0.001);
    assertEquals(dp1.get() + dp2.get() + dp3.get() + dp4.get() + dp5.get(), sumSum.get(), 0.001);
    sumSum.removeDependency(db4);
    assertEquals(dp1.get() + dp2.get() + dp3.get() + dp5.get(), sum.get(), 0.001);
    assertEquals(dp1.get() + dp2.get() + dp3.get() + dp5.get(), sumSum.get(), 0.001);

  }
}