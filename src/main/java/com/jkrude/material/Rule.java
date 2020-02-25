package com.jkrude.material;

import com.jkrude.material.Camt.CamtEntry;
import com.jkrude.material.Camt.ListType;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import javafx.beans.property.MapProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.util.Pair;

public class Rule {

  private Predicate<CamtEntry> predicate;

  private MapProperty<ListType, String> identifierMap;
  private StringProperty note;

  private Rule(Predicate<CamtEntry> predicate, MapProperty<ListType, String> identifierMap,
      StringProperty note) {
    this.predicate = predicate;
    this.identifierMap = identifierMap;
    this.note = note;
  }

  public Predicate<CamtEntry> getPredicate() {
    return predicate;
  }

  public String  getNote() {
    return note.get();
  }

  public Map<ListType, String> getIdentifierMap() {
    return identifierMap.get();
  }

  public MapProperty<ListType, String> identifierMapProperty() {
    return identifierMap;
  }

  public StringProperty noteProperty() {
    return note;
  }


  public static class RuleFactory{

    public static Rule generate(Pair<ListType, String> pair, String note) throws ParseException, NumberFormatException{
      MapProperty<ListType, String> map = new SimpleMapProperty<>(FXCollections.observableMap(new HashMap<>()));
      map.put(pair.getKey(),pair.getValue());
      return generate(map,new SimpleStringProperty(note));
    }

    public static Rule generate(Pair<ListType, String> pair, StringProperty note) throws ParseException, NumberFormatException{
      MapProperty<ListType, String> map = new SimpleMapProperty<>(FXCollections.observableMap(new HashMap<>()));
      map.put(pair.getKey(),pair.getValue());
      return generate(map,note);
    }
    public static Rule generate(Map<ListType, String> map, String note) throws ParseException, NumberFormatException {
      MapProperty<ListType, String> mapProperty = new SimpleMapProperty<>(FXCollections.observableMap(map));
      StringProperty stringProperty = new SimpleStringProperty(note);
      return generate(mapProperty,stringProperty);
    }

    public static Rule generate(MapProperty<ListType, String> map, StringProperty note) throws ParseException, NumberFormatException{
      if(map.isEmpty()){
        throw new IllegalArgumentException("No Qualifier");
      }
      Predicate<CamtEntry> concatPred = camtEntry -> true;
      for (Entry<ListType, String> mapEntry : map.entrySet()) {

        Predicate<CamtEntry> innerPredicate;
        switch (mapEntry.getKey()) {
          case TRANSFER_DATE:
            Date date = Utility.dateFormatter.parse(mapEntry.getValue());
            innerPredicate = camtEntry -> camtEntry.getDate().equals(date);
            break;
          case VALIDATION_DATE:
            innerPredicate = camtEntry -> camtEntry.getDataPoint().getValidationDate()
                .equals(mapEntry.getValue());
            break;
          case TRANSFER_SPECIFICATION:
            innerPredicate = camtEntry -> camtEntry.getDataPoint().getTransferSpecification()
                .equals(mapEntry.getValue());
            break;
          case USAGE:
            innerPredicate = camtEntry -> camtEntry.getDataPoint().getUsage().equals(mapEntry.getValue());
            break;
          case CREDITOR_ID:
            innerPredicate = camtEntry -> camtEntry.getDataPoint().getCreditorId()
                .equals(mapEntry.getValue());
            break;
          case MANDATE_REFERENCE:
            innerPredicate = camtEntry -> camtEntry.getDataPoint().getMandateReference()
                .equals(mapEntry.getValue());
            break;
          case CUSTOMER_REFERENCE_END_TO_END:
            innerPredicate = camtEntry -> camtEntry.getDataPoint().getCustomerReference()
                .equals(mapEntry.getValue());
            break;
          case COLLECTION_REFERENCE:
            innerPredicate = camtEntry -> camtEntry.getDataPoint().getCollectionReference()
                .equals(mapEntry.getValue());
            break;
          case DEBIT_ORIGINAL_AMOUNT:
            innerPredicate = camtEntry -> camtEntry.getDataPoint().getDebitOriginalAmount()
                .equals(mapEntry.getValue());
            break;
          case BACK_DEBIT:
            innerPredicate = camtEntry -> camtEntry.getDataPoint().getBackDebit()
                .equals(mapEntry.getValue());
            break;
          case OTHER_PARTY:
            innerPredicate = camtEntry -> camtEntry.getDataPoint().getOtherParty()
                .equals(mapEntry.getValue());
            break;
          case IBAN:
            innerPredicate = camtEntry -> camtEntry.getDataPoint().getIban().equals(mapEntry.getValue());
            break;
          case BIC:
            innerPredicate = camtEntry -> camtEntry.getDataPoint().getBic().equals(mapEntry.getValue());
            break;
          case AMOUNT:
            Money amount = new Money(mapEntry.getValue());
            innerPredicate = camtEntry -> camtEntry.getDataPoint().getAmount().equals(amount);
            break;
          case INFO:
            innerPredicate = camtEntry -> camtEntry.getDataPoint().getInfo().equals(mapEntry.getValue());
            break;
          default:
            throw new IllegalArgumentException();
        }
        concatPred = concatPred.and(innerPredicate);
      }
      return new Rule(concatPred,map,note);
    }
  }
}
