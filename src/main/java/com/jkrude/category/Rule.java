package com.jkrude.category;

import com.jkrude.material.Money;
import com.jkrude.material.Utility;
import com.jkrude.transaction.Transaction;
import com.jkrude.transaction.Transaction.TransactionField;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import javafx.beans.property.MapProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableObjectValue;
import javafx.collections.FXCollections;

public class Rule {

  private final Predicate<Transaction> predicate;
  private final MapProperty<TransactionField, String> identifierPairs;
  private StringProperty note;
  private CategoryNode parent;

  // Rules should only be constructed with the RuleFactory

  private Rule(Predicate<Transaction> predicate,
      MapProperty<TransactionField, String> identifierPairs,
      StringProperty note, CategoryNode parent) {
    this.predicate = predicate;
    this.identifierPairs = identifierPairs;
    this.note = note;
    this.parent = parent;
  }

  public Rule(Predicate<Transaction> predicate,
      MapProperty<TransactionField, String> idPairs) {
    this(predicate, idPairs, null, null);
  }

  private Rule(Predicate<Transaction> predicate,
      MapProperty<TransactionField, String> identifierPairs,
      StringProperty note) {
    this(predicate, identifierPairs, note, null);
  }

  public Predicate<Transaction> getPredicate() {
    return predicate;
  }

  public Optional<String> getNote() {
    Optional<StringProperty> noteProperty = noteProperty();
    return noteProperty.map(ObservableObjectValue::get);
  }

  public Map<TransactionField, String> getIdentifierPairs() {
    return identifierPairs.get();
  }

  public Optional<StringProperty> noteProperty() {
    return Optional.ofNullable(note);
  }

  public void setParent(CategoryNode parent) {
    this.parent = parent;
  }

  public Optional<CategoryNode> getParent() {
    return Optional.ofNullable(parent);
  }

  @Override
  public boolean equals(Object o) {
    // Is true if the identifying pairs are equal
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Rule rule = (Rule) o;
    return this.getIdentifierPairs().equals(rule.getIdentifierPairs());
  }

  @Override
  public int hashCode() {
    return Objects.hash(identifierPairs);
  }


  public static class RuleBuilder {

    public static RuleBuilderWithRule fromPair(TransactionField field, String value)
        throws ParseException {
      MapProperty<TransactionField, String> map = new SimpleMapProperty<>(
          FXCollections.observableMap(new HashMap<>()));
      map.get().put(field, value);
      return new RuleBuilderWithRule(map);
    }

    public static RuleBuilderWithRule fromMap(Map<TransactionField, String> map)
        throws ParseException {
      return new RuleBuilderWithRule(new SimpleMapProperty<>(FXCollections.observableMap(map)));
    }


    public static class RuleBuilderWithRule {

      private final Rule rule;

      private RuleBuilderWithRule(MapProperty<TransactionField, String> container)
          throws ParseException {

        if (container.isEmpty()) {
          throw new IllegalArgumentException("No Qualifier");
        }
        Predicate<Transaction> concatPred = transaction -> true;
        for (Map.Entry<TransactionField, String> entry : container.get().entrySet()) {

          Predicate<Transaction> innerPredicate;
          switch (entry.getKey()) {
            case ACCOUNT_IBAN:
              innerPredicate = transaction -> transaction.getAccountIban()
                  .equals(entry.getValue());
              break;
            case TRANSFER_DATE:
              LocalDate date = LocalDate.parse(entry.getValue(), Utility.DATE_TIME_FORMATTER);
              innerPredicate = transaction -> transaction.getDate().equals(date);
              break;
            case VALIDATION_DATE:
              LocalDate valDate = LocalDate.parse(entry.getValue(), Utility.DATE_TIME_FORMATTER);
              innerPredicate = transaction -> transaction.getValidationDate().equals(valDate);
              break;
            case TRANSFER_SPECIFICATION:
              innerPredicate = transaction -> transaction
                  .getTransferSpecification()
                  .equals(entry.getValue());
              break;
            case USAGE:
              innerPredicate = transaction -> transaction.getUsage()
                  .toLowerCase()
                  .contains(entry.getValue().toLowerCase());
              break;
            case CREDITOR_ID:
              innerPredicate = transaction -> transaction.getCreditorId()
                  .equals(entry.getValue());
              break;
            case MANDATE_REFERENCE:
              innerPredicate = transaction -> transaction.getMandateReference()
                  .equals(entry.getValue());
              break;
            case CUSTOMER_REFERENCE_END_TO_END:
              innerPredicate = transaction -> transaction
                  .getCustomerReference()
                  .equals(entry.getValue());
              break;
            case COLLECTION_REFERENCE:
              innerPredicate = transaction -> transaction
                  .getCollectionReference()
                  .equals(entry.getValue());
              break;
            case DEBIT_ORIGINAL_AMOUNT:
              innerPredicate = transaction -> transaction
                  .getDebitOriginalAmount()
                  .equals(entry.getValue());
              break;
            case BACK_DEBIT:
              innerPredicate = transaction -> transaction.getBackDebit()
                  .equals(entry.getValue());
              break;
            case OTHER_PARTY:
              innerPredicate = transaction -> transaction.getOtherParty()
                  .toLowerCase()
                  .contains(entry.getValue().toLowerCase());
              break;
            case IBAN:
              innerPredicate = transaction -> transaction.getIban()
                  .equals(entry.getValue());
              break;
            case BIC:
              innerPredicate = transaction -> transaction.getBic()
                  .equals(entry.getValue());
              break;
            case AMOUNT:
              Money amount = new Money(entry.getValue());
              innerPredicate = transaction -> transaction.getMoneyAmount()
                  .equals(amount);
              break;
            case INFO:
              innerPredicate = transaction -> transaction.getInfo()
                  .equals(entry.getValue());
              break;
            default:
              throw new IllegalArgumentException(
                  "Unknown qualifier for rule creation: " + entry.getKey());
          }
          concatPred = concatPred.and(innerPredicate);
        }
        rule = new Rule(concatPred, container);
      }

      public RuleBuilderWithRule addNote(String note) {
        if (note != null) {
          rule.note = new SimpleStringProperty(note);
        }
        return this;
      }

      public RuleBuilderWithRule setParent(CategoryNode parent) {
        rule.setParent(parent);
        return this;
      }

      public Rule build() {
        return rule;
      }
    }
  }
}
