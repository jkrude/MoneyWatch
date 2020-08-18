package com.jkrude.material;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import org.junit.Test;

public class MoneyTest {

  @Test
  public void testBaseFunction() {
    Money amount1 = new Money(12.21);
    Money amount2 = new Money(3);
    Money amountSum = Money.add(amount1, amount2);
    assertEquals(amountSum, new Money(15.21));
    Money strDec = new Money("12.21");
    strDec.add(amount2);
    assertEquals(strDec, new Money(15.21));
  }

  @Test
  public void testGenericConstructor() {
    String a = "-10.20 EUR";
    String b = "1 €";
    String c = "20";
    Money A = new Money(a);
    Money B = new Money(b);
    Money C = new Money(c);
    assertEquals(A.getRawAmount().stripTrailingZeros(),
        BigDecimal.valueOf(-10.2).stripTrailingZeros());
    assertEquals(A.getCurrency(), Money.EURO);
    assertEquals(B.getRawAmount().stripTrailingZeros(), BigDecimal.valueOf(1));
    assertEquals(C.getCurrency(), Money.EURO);
    assertEquals(C.getRawAmount().longValue(), BigDecimal.valueOf(20).longValue());
    assertEquals(C.getCurrency(), Money.EURO);
    String d = "12d";
    try {
      new Money(d);
      fail();
    } catch (NumberFormatException e) {
    }

  }

}