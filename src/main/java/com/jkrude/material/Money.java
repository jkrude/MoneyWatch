package com.jkrude.material;

import com.jkrude.transaction.ExtendedTransaction;
import com.jkrude.transaction.Transaction;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.util.Pair;

public class Money implements Comparable<Money>{

  public static final Currency EURO = Currency.getInstance("EUR");
  private Currency currency;
  private BigDecimal amount;


  /*
   * Constructors.
   */
  public Money() {
    this.currency = EURO;
    this.amount = scaleToTwo(new BigDecimal(0));
  }

  public Money(String curr, int val) {
    this.currency = Currency.getInstance(curr);
    this.amount = scaleToTwo(new BigDecimal(val));
  }

  public Money(double val) {
    this.currency = EURO;
    this.amount = scaleToTwo(new BigDecimal(val));
  }

  public Money(String curr, BigDecimal bigDec) {
    this.currency = Currency.getInstance(curr);
    this.amount = scaleToTwo(bigDec);
  }

  public Money(int val) {
    this.currency = EURO;
    this.amount = scaleToTwo(new BigDecimal(val));
  }

  public Money(Currency curr, BigDecimal bigDec) {
    this(bigDec);
    this.amount = scaleToTwo(bigDec);
  }

  public Money(BigDecimal bigDecimal) {
    this.currency = EURO;
    this.amount = scaleToTwo(bigDecimal);
  }

  public Money(Currency currency, String val) throws NumberFormatException {
    this(val);
    this.currency = currency;
  }

  public Money(String val) throws NumberFormatException {
    Set<Currency> currencies = Currency.getAvailableCurrencies();
    List<String> codes = currencies.stream().map(Currency::getCurrencyCode)
        .collect(Collectors.toList());
    List<Pair<Currency, String>> symbols = currencies.stream()
        .map(curr -> new Pair<>(curr, curr.getSymbol()))
        .collect(Collectors.toList());
    for (String code : codes) {
      if (val.contains(code)) {
        this.currency = Currency.getInstance(code);
        String num = val.replaceAll("[^-?0-9.]", "");
        this.amount = scaleToTwo(new BigDecimal(num));
        return;

      }
    }
    for (Pair<Currency, String> symPair : symbols) {
      if (val.contains(symPair.getValue())) {
        this.currency = symPair.getKey();
        String num = val.replaceAll("[^-?0-9.]", "");
        this.amount = scaleToTwo(new BigDecimal(num));
        return;

      }
    }
    this.currency = EURO;
    this.amount = scaleToTwo(new BigDecimal(val));
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
        Objects.equals(amount, money.amount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(EURO, currency, amount);
  }

  @Override
  public String toString() {
    String currencyStr = this.currency == EURO ? " €" : ", " + currency.toString();
    return amount.toString() + currencyStr;
  }

  @Override
  public int compareTo(Money o) {
    if(this.currency.equals(o.currency)){
      return this.getRawAmount().compareTo(o.getRawAmount());
    }else{
      throw new IllegalArgumentException("Cant compare values of different currency");
    }
  }

  /*
   * Static functions.
   */
  private static BigDecimal scaleToTwo(BigDecimal bigDecimal) {
    return bigDecimal.setScale(2, RoundingMode.HALF_UP);
  }


  /*
   * Arithmetic operations.
   */
  public static Money add(Money a, Money b) {
    if (!a.currency.equals(b.currency)) {
      throw new IllegalArgumentException("Currencies didnt match!");
    }
    BigDecimal c = a.amount.add(b.amount);
    return new Money(a.currency, c);
  }

  public static Money sub(Money a, Money b) {
    if (!a.currency.equals(b.currency)) {
      throw new IllegalArgumentException("Currencies didnt match!");
    }
    BigDecimal c = a.amount.subtract(b.amount);
    return new Money(a.currency, c);
  }

  public static Money sum(Iterable<Transaction> transactions) {
    Money total = new Money();
    transactions.forEach(
        transaction -> total.add(transaction.getMoneyAmount()));
    return total;
  }

  public static Money mapSum(Iterable<? extends ExtendedTransaction> transactions) {
    Money total = new Money();
    transactions.forEach(
        eTransaction -> total.add(eTransaction.getBaseTransaction().getMoneyAmount()));
    return total;
  }

  public void add(Money b) {
    if (!this.currency.equals(b.currency)) {
      throw new IllegalArgumentException("Currencies didnt match!");
    }
    this.amount = this.amount.add(b.amount);
  }

  public void sub(Money b) {
    if (!this.currency.equals(b.currency)) {
      throw new IllegalArgumentException("Currencies didnt match!");
    }
    this.amount = this.amount.subtract(b.amount);
    ;
  }

  public boolean isPositive() {
    return getRawAmount().compareTo(BigDecimal.ZERO) >= 0;
  }

  /*
   * Getter and setter.
   */
  public BigDecimal getRawAmount() {
    return amount;
  }

  public void setAmount(double val) {
    this.amount = scaleToTwo(new BigDecimal(val));
  }

  public final Currency getCurrency() {
    return currency;
  }

}
