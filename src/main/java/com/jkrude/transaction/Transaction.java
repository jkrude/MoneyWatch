package com.jkrude.transaction;

import com.jkrude.material.Money;
import com.jkrude.material.Utility;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.util.Pair;

public class Transaction {

  private final ObjectProperty<LocalDate> date;
  private String accountIban;
  private LocalDate validationDate;
  private String transferSpecification;
  private String usage;
  private String creditorId;
  private String mandateReference;
  private String customerReference;
  private String collectionReference;
  private String debitOriginalAmount;
  private String backDebit;
  private String otherParty;
  private String iban;
  private String bic;
  private Money amount;
  private String info;

  public Transaction() {
    this.date = new SimpleObjectProperty<>();
  }

  public Transaction(
      LocalDate date,
      String accountIban, LocalDate validationDate,
      String transferSpecification, String usage, String creditorId,
      String mandateReference, String customerReference, String collectionReference,
      String debitOriginalAmount, String backDebit, String otherParty, String iban,
      String bic, Money amount, String info) {
    this.accountIban = accountIban;
    this.validationDate = validationDate;
    this.transferSpecification = transferSpecification;
    this.usage = usage;
    this.creditorId = creditorId;
    this.mandateReference = mandateReference;
    this.customerReference = customerReference;
    this.collectionReference = collectionReference;
    this.debitOriginalAmount = debitOriginalAmount;
    this.backDebit = backDebit;
    this.otherParty = otherParty;
    this.iban = iban;
    this.bic = bic;
    this.amount = amount;
    this.info = info;
    this.date = new SimpleObjectProperty<>(date);
  }

  /*
   * Getter.
   */

  public boolean isPositive() {
    return getMoneyAmount().isPositive();
  }

  public LocalDate getDate() {
    return date.get();
  }

  public ObjectProperty<LocalDate> dateProperty() {
    return date;
  }

  public String getAccountIban() {
    return accountIban;
  }

  public LocalDate getValidationDate() {
    return validationDate;
  }

  public String getTransferSpecification() {
    return transferSpecification;
  }

  public String getUsage() {
    return usage;
  }

  public String getCreditorId() {
    return creditorId;
  }

  public String getMandateReference() {
    return mandateReference;
  }

  public String getCustomerReference() {
    return customerReference;
  }

  public String getCollectionReference() {
    return collectionReference;
  }

  public String getDebitOriginalAmount() {
    return debitOriginalAmount;
  }

  public String getBackDebit() {
    return backDebit;
  }

  public String getOtherParty() {
    return otherParty;
  }

  public String getIban() {
    return iban;
  }

  public String getBic() {
    return bic;
  }

  public Money getMoneyAmount() {
    return amount;
  }

  public String getInfo() {
    return info;
  }

  /*
   * Setter.
   */

  public void setDate(LocalDate date) {
    this.date.set(date);
  }

  public void setAccountIban(String accountIban) {
    this.accountIban = accountIban;
  }

  public void setValidationDate(LocalDate validationDate) {
    this.validationDate = validationDate;
  }

  public void setTransferSpecification(String transferSpecification) {
    this.transferSpecification = transferSpecification;
  }

  public void setUsage(String usage) {
    this.usage = usage;
  }

  public void setCreditorId(String creditorId) {
    this.creditorId = creditorId;
  }

  public void setMandateReference(String mandateReference) {
    this.mandateReference = mandateReference;
  }

  public void setCustomerReference(String customerReference) {
    this.customerReference = customerReference;
  }

  public void setCollectionReference(String collectionReference) {
    this.collectionReference = collectionReference;
  }

  public void setDebitOriginalAmount(String debitOriginalAmount) {
    this.debitOriginalAmount = debitOriginalAmount;
  }

  public void setBackDebit(String backDebit) {
    this.backDebit = backDebit;
  }

  public void setOtherParty(String otherParty) {
    this.otherParty = otherParty;
  }

  public void setIban(String iban) {
    this.iban = iban;
  }

  public void setBic(String bic) {
    this.bic = bic;
  }

  public void setAmount(Money amount) {
    this.amount = amount;
  }

  public void setInfo(String info) {
    this.info = info;
  }

  public Set<Pair<TransactionField, String>> getAsPairSet() {
    return getSelectedFields(Set.of(TransactionField.values()));
  }

  public Set<Pair<TransactionField, String>> getSelectedFields(
      Set<TransactionField> selectedFields) {
    Set<Pair<TransactionField, String>> resultSet = new HashSet<>();
    for (TransactionField transactionField : selectedFields) {
      switch (transactionField) {

        case ACCOUNT_IBAN:
          resultSet.add(new Pair<>(transactionField, getAccountIban()));
          break;
        case TRANSFER_DATE:
          resultSet
              .add(new Pair<>(transactionField, Utility.DATE_TIME_FORMATTER.format(getDate())));
          break;
        case VALIDATION_DATE:
          resultSet.add(
              new Pair<>(transactionField,
                  Utility.DATE_TIME_FORMATTER.format(getValidationDate())));
          break;
        case TRANSFER_SPECIFICATION:
          resultSet.add(new Pair<>(transactionField, getTransferSpecification()));
          break;
        case USAGE:
          resultSet.add(new Pair<>(transactionField, getUsage()));
          break;
        case CREDITOR_ID:
          resultSet.add(new Pair<>(transactionField, getCreditorId()));
          break;
        case MANDATE_REFERENCE:
          resultSet.add(new Pair<>(transactionField, getMandateReference()));
          break;
        case CUSTOMER_REFERENCE_END_TO_END:
          resultSet.add(new Pair<>(transactionField, getCustomerReference()));
          break;
        case COLLECTION_REFERENCE:
          resultSet.add(new Pair<>(transactionField, getCollectionReference()));
          break;
        case DEBIT_ORIGINAL_AMOUNT:
          resultSet.add(new Pair<>(transactionField, getDebitOriginalAmount()));
          break;
        case BACK_DEBIT:
          resultSet.add(new Pair<>(transactionField, getBackDebit()));
          break;
        case OTHER_PARTY:
          resultSet.add(new Pair<>(transactionField, getOtherParty()));
          break;
        case IBAN:
          resultSet.add(new Pair<>(transactionField, getIban()));
          break;
        case BIC:
          resultSet.add(new Pair<>(transactionField, getBic()));
          break;
        case AMOUNT:
          resultSet.add(new Pair<>(transactionField, getMoneyAmount().toString()));
          break;
        case INFO:
          resultSet.add(new Pair<>(transactionField, getInfo()));
          break;
      }
    }
    return resultSet;
  }

  public enum TransactionField {
    ACCOUNT_IBAN("Auftragskonto"),
    TRANSFER_DATE("Buchungstag"),
    VALIDATION_DATE("Valutadatum"),
    TRANSFER_SPECIFICATION("Buchungstext"),
    USAGE("Verwendungszweck"),
    CREDITOR_ID("Glaeubiger ID"),
    MANDATE_REFERENCE("Mandatsreferenz"),
    CUSTOMER_REFERENCE_END_TO_END("Kundenreferenz (End-to-End)"),
    COLLECTION_REFERENCE("Sammlerreferenz"),
    DEBIT_ORIGINAL_AMOUNT("Lastschrift Ursprungsbetrag"),
    BACK_DEBIT("Auslagenersatz Ruecklastschrift"),
    OTHER_PARTY("Beguenstigter/Zahlungspflichtiger"),
    IBAN("Kontonummer/IBAN"),
    BIC("BIC (SWIFT-Code)"),
    AMOUNT("Betrag"),
    INFO("Info");

    private final String translation;

    TransactionField(String s) {
      this.translation = s;
    }

    public String getTranslation() {
      return translation;
    }

    @Override
    public String toString() {
      return translation;
    }

    private static final Map<String, TransactionField> lookup = new HashMap<>();

    //Populate the lookup table on loading time
    static {
      for (TransactionField env : TransactionField.values()) {
        lookup.put(env.getTranslation(), env);
      }
    }

    //This method can be used for reverse lookup purpose
    public static TransactionField get(String url) {
      return lookup.get(url);
    }

  }
}