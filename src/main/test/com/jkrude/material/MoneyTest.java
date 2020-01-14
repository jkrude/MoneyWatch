package com.jkrude.material;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import org.junit.Test;

public class MoneyTest {

  @Test
  public void testBaseFunction(){
    Money amount1 = new Money(12.21);
    Money amount2 = new Money(3);
    Money amountSum = Money.add(amount1, amount2);
    assertEquals(amountSum, new Money(15.21));
    Money strDec = new Money("12.21");
    strDec.add(amount2);
    assertEquals(strDec, new Money(15.21));
  }

}