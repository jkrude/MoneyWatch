package com.jkrude.category;

import com.jkrude.material.Money;
import com.jkrude.material.TransactionContainer.Transaction;
import com.jkrude.material.TransactionContainer.TransactionField;
import com.jkrude.material.Utility;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableObjectValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.util.Pair;

public class Rule {

  private final Predicate<Transaction> predicate;
  private final SetProperty<Pair<TransactionField, String>> identifierPairs;
  private StringProperty note;
  private CategoryNode parent;

  public Rule(Predicate<Transaction> predicate,
      SetProperty<Pair<TransactionField, String>> idPairs) {
    this.predicate = predicate;
    this.identifierPairs = idPairs;
    this.note = null;
    this.parent = null;
  }

  // Rules should only be constructed with the RuleFactory
  private Rule(Predicate<Transaction> predicate,
      SetProperty<Pair<TransactionField, String>> identifierPairs,
      StringProperty note, CategoryNode parent) {
    this.predicate = predicate;
    this.identifierPairs = identifierPairs;
    this.note = note;
    this.parent = parent;
  }

  private Rule(Predicate<Transaction> predicate,
      SetProperty<Pair<TransactionField, String>> identifierPairs,
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

  public Set<Pair<TransactionField, String>> getIdentifierPairs() {
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

    public static RuleBuilderWithRule fromPair(Pair<TransactionField, String> pair)
        throws ParseException {
      SimpleSetProperty<Pair<TransactionField, String>> set = new SimpleSetProperty<>(
          FXCollections.observableSet(new HashSet<>()));
      set.get().add(pair);
      return new RuleBuilderWithRule(set);
    }

    public static RuleBuilderWithRule fromSet(Set<Pair<TransactionField, String>> set)
        throws ParseException {
      return new RuleBuilderWithRule(new SimpleSetProperty<>(FXCollections.observableSet(set)));
    }

    public static RuleBuilderWithRule fromObservableSet(
        ObservableSet<Pair<TransactionField, String>> set) throws ParseException {
      return new RuleBuilderWithRule(new SimpleSetProperty<>(FXCollections.observableSet(set)));
    }

    public static class RuleBuilderWithRule {

      Rule rule;

      private RuleBuilderWithRule(SetProperty<Pair<TransactionField, String>> container)
          throws ParseException {
        if (container.isEmpty()) {
          throw new IllegalArgumentException("No Qualifier");
        }
        Predicate<Transaction> concatPred = transaction -> true;
        for (Pair<TransactionField, String> pair : container) {

          Predicate<Transaction> innerPredicate;
          switch (pair.getKey()) {
            case ACCOUNT_IBAN:
              innerPredicate = transaction -> transaction.getAccountIban()
                  .equals(pair.getValue());
              break;
            case TRANSFER_DATE:
              Date date = Utility.dateFormatter.parse(pair.getValue());
              innerPredicate = transaction -> transaction.getDate().equals(date);
              break;
            case VALIDATION_DATE:
              Date valDate = Utility.dateFormatter.parse(pair.getValue());
              innerPredicate = transaction -> transaction.getValidationDate().equals(valDate);
              break;
            case TRANSFER_SPECIFICATION:
              innerPredicate = transaction -> transaction
                  .getTransferSpecification()
                  .equals(pair.getValue());
              break;
            case USAGE:
              innerPredicate = transaction -> transaction.getUsage()
                  .toLowerCase()
                  .contains(pair.getValue().toLowerCase());
              break;
            case CREDITOR_ID:
              innerPredicate = transaction -> transaction.getCreditorId()
                  .equals(pair.getValue());
              break;
            case MANDATE_REFERENCE:
              innerPredicate = transaction -> transaction.getMandateReference()
                  .equals(pair.getValue());
              break;
            case CUSTOMER_REFERENCE_END_TO_END:
              innerPredicate = transaction -> transaction
                  .getCustomerReference()
                  .equals(pair.getValue());
              break;
            case COLLECTION_REFERENCE:
              innerPredicate = transaction -> transaction
                  .getCollectionReference()
                  .equals(pair.getValue());
              break;
            case DEBIT_ORIGINAL_AMOUNT:
              innerPredicate = transaction -> transaction
                  .getDebitOriginalAmount()
                  .equals(pair.getValue());
              break;
            case BACK_DEBIT:
              innerPredicate = transaction -> transaction.getBackDebit()
                  .equals(pair.getValue());
              break;
            case OTHER_PARTY:
              innerPredicate = transaction -> transaction.getOtherParty()
                  .toLowerCase()
                  .contains(pair.getValue().toLowerCase());
              break;
            case IBAN:
              innerPredicate = transaction -> transaction.getIban()
                  .equals(pair.getValue());
              break;
            case BIC:
              innerPredicate = transaction -> transaction.getBic()
                  .equals(pair.getValue());
              break;
            case AMOUNT:
              Money amount = new Money(pair.getValue());
              innerPredicate = transaction -> transaction.getMoneyAmount()
                  .equals(amount);
              break;
            case INFO:
              innerPredicate = transaction -> transaction.getInfo()
                  .equals(pair.getValue());
              break;
            default:
              throw new IllegalArgumentException(
                  "Unknown qualifier for rule creation: " + pair.getKey());
          }
          concatPred = concatPred.and(innerPredicate);
        }
        rule = new Rule(concatPred, container);
      }

      public RuleBuilderWithRule addNote(String note) {
        rule.note = new SimpleStringProperty(note);
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
