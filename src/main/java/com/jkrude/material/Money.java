package com.jkrude.material;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

public class Money {

  public static final Currency EURO = Currency.getInstance("EUR");
  private Currency currency;
  private BigDecimal value;


  /*
   * Constructors.
   */

  public Money(){
    this.currency = EURO;
    this.value = scaleToTwo(new BigDecimal(0));
  }

  public Money(String curr, int val){
    this.currency = Currency.getInstance(curr);
    this.value = scaleToTwo(new BigDecimal(val));
  }
  public Money(double val){
    this.currency = EURO;
    this.value = scaleToTwo(new BigDecimal(val));
  }

  public Money(String curr, BigDecimal bigDec){
    this.currency = Currency.getInstance(curr);
    this.value = scaleToTwo(bigDec);
  }

  public Money(int val){
    this.currency = EURO;
    this.value = scaleToTwo(new BigDecimal(val));
  }

  public Money(Currency curr, BigDecimal bigDec){
    this(bigDec);
    this.value = scaleToTwo(bigDec);
  }
  public Money(BigDecimal bigDecimal){
    this.currency = EURO;
    this.value = scaleToTwo(bigDecimal);
  }

  public Money(Currency currency, String val){
    this(val);
    this.currency = currency;
  }
  public Money(String val){
    this.currency = EURO;
    this.value = scaleToTwo(new BigDecimal(val));
  }

  /*
   * Overrides.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Money money = (Money) o;
    return Objects.equals(currency, money.currency) &&
        Objects.equals(value, money.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(EURO, currency, value);
  }

  @Override
  public String toString() {
    return "Money{" +
        "currency=" + currency +
        ", value=" + value.toString() +
        '}';
  }


  /*
   * Static functions.
   */
  private static BigDecimal scaleToTwo(BigDecimal bigDecimal){
    return bigDecimal.setScale(2, RoundingMode.HALF_UP);
  }


  /*
   * Arithmetic operations.
   */
  public static Money add(Money a, Money b){
    if(!a.currency.equals(b.currency)){
      throw new IllegalArgumentException("Currencies didnt match!");
    }
    BigDecimal c = a.value.add(b.value);
    return new Money(a.currency,c);
  }

  public static Money sub(Money a, Money b){
    if(!a.currency.equals(b.currency)){
      throw new IllegalArgumentException("Currencies didnt match!");
    }
    BigDecimal c = a.value.subtract(b.value);
    return new Money(a.currency, c);
  }

  public void add(Money b){
    if(!this.currency.equals(b.currency)){
      throw new IllegalArgumentException("Currencies didnt match!");
    }
    this.value = this.value.add(b.value);
  }
  
  public void sub(Money b){
    if(!this.currency.equals(b.currency)){
      throw new IllegalArgumentException("Currencies didnt match!");
    }
    this.value = this.value.subtract(b.value);;
  }

  /*
   * Getter and setter.
   */
  public BigDecimal getValue() {
    return value;
  }

  public void setValue(int val){
    this.value = scaleToTwo(new BigDecimal(val));
  }

}
