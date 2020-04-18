package com.jkrude.material;

import com.jkrude.material.Camt.CamtEntry;
import com.jkrude.material.Camt.ListType;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.util.Pair;

public class Rule {

  private final Predicate<CamtEntry> predicate;
  private final SetProperty<Pair<ListType, String>> identifierPairs;
  private StringProperty note;

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

  // Rules should only be constructed with the RuleFactory
  private Rule(Predicate<CamtEntry> predicate, SetProperty<Pair<ListType, String>> identifierPairs,
      StringProperty note) {
    this.predicate = predicate;
    this.identifierPairs = identifierPairs;
    this.note = note;
  }

  public Predicate<CamtEntry> getPredicate() {
    return predicate;
  }

  public String getNote() {
    return note.get();
  }

  public Set<Pair<ListType, String>> getIdentifierPairs() {
    return identifierPairs.get();
  }

  public StringProperty noteProperty() {
    return note;
  }


  public static class RuleFactory {

    // Wrap note and call next wrapper
    public static Rule generate(Pair<ListType, String> pair, String note)
        throws ParseException, NumberFormatException {
      SetProperty<Pair<ListType, String>> container = new SimpleSetProperty<>(
          FXCollections.observableSet(new HashSet<>()));
      container.add(pair);
      return generate(container, new SimpleStringProperty(note));
    }

    // Wrap pair
    public static Rule generate(Pair<ListType, String> pair, StringProperty note)
        throws ParseException, NumberFormatException {
      SetProperty<Pair<ListType, String>> container = new SimpleSetProperty<>(
          FXCollections.observableSet(new HashSet<>()));
      container.add(pair);
      return generate(container, note);
    }

    // Wrap set and note
    public static Rule generate(Set<Pair<ListType, String>> container, String note)
        throws ParseException, NumberFormatException {
      SetProperty<Pair<ListType, String>> pairsProperty = new SimpleSetProperty<>(
          FXCollections.observableSet(container));
      StringProperty stringProperty = new SimpleStringProperty(note);
      return generate(pairsProperty, stringProperty);
    }

    // Real generator
    public static Rule generate(SetProperty<Pair<ListType, String>> container, StringProperty note)
        throws ParseException, NumberFormatException {
      if (container.isEmpty()) {
        throw new IllegalArgumentException("No Qualifier");
      }
      Predicate<CamtEntry> concatPred = camtEntry -> true;
      for (Pair<ListType, String> pair : container) {

        Predicate<CamtEntry> innerPredicate;
        switch (pair.getKey()) {
          case TRANSFER_DATE:
            Date date = Utility.dateFormatter.parse(pair.getValue());
            innerPredicate = camtEntry -> camtEntry.getDate().equals(date);
            break;
          case VALIDATION_DATE:
            innerPredicate = camtEntry -> camtEntry.getDataPoint().getValidationDate()
                .equals(pair.getValue());
            break;
          case TRANSFER_SPECIFICATION:
            innerPredicate = camtEntry -> camtEntry.getDataPoint().getTransferSpecification()
                .equals(pair.getValue());
            break;
          case USAGE:
            innerPredicate = camtEntry -> camtEntry.getDataPoint().getUsage().toLowerCase()
                .contains(pair.getValue().toLowerCase());
            break;
          case CREDITOR_ID:
            innerPredicate = camtEntry -> camtEntry.getDataPoint().getCreditorId()
                .equals(pair.getValue());
            break;
          case MANDATE_REFERENCE:
            innerPredicate = camtEntry -> camtEntry.getDataPoint().getMandateReference()
                .equals(pair.getValue());
            break;
          case CUSTOMER_REFERENCE_END_TO_END:
            innerPredicate = camtEntry -> camtEntry.getDataPoint().getCustomerReference()
                .equals(pair.getValue());
            break;
          case COLLECTION_REFERENCE:
            innerPredicate = camtEntry -> camtEntry.getDataPoint().getCollectionReference()
                .equals(pair.getValue());
            break;
          case DEBIT_ORIGINAL_AMOUNT:
            innerPredicate = camtEntry -> camtEntry.getDataPoint().getDebitOriginalAmount()
                .equals(pair.getValue());
            break;
          case BACK_DEBIT:
            innerPredicate = camtEntry -> camtEntry.getDataPoint().getBackDebit()
                .equals(pair.getValue());
            break;
          case OTHER_PARTY:
            innerPredicate = camtEntry -> camtEntry.getDataPoint().getOtherParty().toLowerCase()
                .contains(pair.getValue().toLowerCase());
            break;
          case IBAN:
            innerPredicate = camtEntry -> camtEntry.getDataPoint().getIban()
                .equals(pair.getValue());
            break;
          case BIC:
            innerPredicate = camtEntry -> camtEntry.getDataPoint().getBic()
                .equals(pair.getValue());
            break;
          case AMOUNT:
            Money amount = new Money(pair.getValue());
            innerPredicate = camtEntry -> camtEntry.getDataPoint().getAmount().equals(amount);
            break;
          case INFO:
            innerPredicate = camtEntry -> camtEntry.getDataPoint().getInfo()
                .equals(pair.getValue());
            break;
          default:
            throw new IllegalArgumentException();
        }
        concatPred = concatPred.and(innerPredicate);
      }
      return new Rule(concatPred, container, note);
    }
  }
}
