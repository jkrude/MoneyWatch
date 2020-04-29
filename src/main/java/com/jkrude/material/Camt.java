package com.jkrude.material;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;

public class Camt {

  public enum ListType {
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

    private String translation;

    ListType(String s) {
      this.translation = s;
    }
    public String getTranslation(){
      return translation;
    }

    @Override
    public String toString() {
     return translation;
    }

    private static final Map<String, ListType> lookup = new HashMap<>();

    //Populate the lookup table on loading time
    static
    {
      for(ListType env : ListType.values())
      {
        lookup.put(env.getTranslation(), env);
      }
    }

    //This method can be used for reverse lookup purpose
    public static ListType get(String url)
    {
      return lookup.get(url);
    }

  }

  public SimpleDateFormat getDateFormatter() {
    return dateFormatter;
  }

  private ListProperty<CamtEntry> source;

  private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yy");


  public Camt() {
    source = new SimpleListProperty<>(FXCollections.observableArrayList());
  }

  public Camt(ObservableList<CamtEntry> entries) {
    source = new SimpleListProperty<>(entries);
  }

  public Camt(Scanner sc) throws ParseException, IllegalArgumentException {
    this();
    this.csvFileParser(sc);
  }

  /*
  PieChart associated methods.

  public ObservableList<PieChart.Data> getPieChartData(ObservableList<PieCategory> categories) {

    ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
    HashMap<StringProperty, Money> dataHashMap = new HashMap<>();

    for (int i = 0; i < this.source.size(); ++i) {
      boolean found = false;

      for (PieCategory category : categories) {

        for (PieCategory.Entry entry : category.getIdentifierList()) {
          if (found) {
            break;
          }

          switch (entry.getType()) {
            case IBAN:
              found = searchForCategory(dataHashMap, i, category, entry, iban);
              break;
            case ACCOUNT_IBAN:
              found = searchForCategory(dataHashMap, i, category, entry, accountIban);
              break;
            case TRANSFER_DATE:
              found = searchForCategory(dataHashMap, i, category, entry, transferDate);
              break;
            case VALIDATION_DATE:
              found = searchForCategory(dataHashMap, i, category, entry, validationDate);
              break;
            case TRANSFER_SPECIFICATION:
              found = searchForCategory(dataHashMap, i, category, entry, transferSpecification);
              break;
            case USAGE:
              found = searchForCategory(dataHashMap, i, category, entry, usage);
              break;
            case CREDITOR_ID:
              found = searchForCategory(dataHashMap, i, category, entry, creditorId);
              break;
            case MANDATE_REFERENCE:
              found = searchForCategory(dataHashMap, i, category, entry, mandateReference);
              break;
            case CUSTOMER_REFERENCE_END_TO_END:
              found = searchForCategory(dataHashMap, i, category, entry, customerReference);
              break;
            case COLLECTION_REFERENCE:
              found = searchForCategory(dataHashMap, i, category, entry, collectionReference);
              break;
            case DEBIT_ORIGINAL_AMOUNT:
              found = searchForCategory(dataHashMap, i, category, entry, debitOriginalAmount);
              break;
            case BACK_DEBIT:
              found = searchForCategory(dataHashMap, i, category, entry, backDebit);
              break;
            case OTHER_PARTY:
              found = searchForCategory(dataHashMap, i, category, entry, otherParty);
              break;
            case BIC:
              found = searchForCategory(dataHashMap, i, category, entry, bic);
              break;
            case INFO:
              found = searchForCategory(dataHashMap, i, category, entry, info);
              break;
          }
        }
      }
    }
    dataHashMap.forEach((StringProperty key, Money value) -> pieChartData
        .add(new PieChart.Data(key.get(), value.getAmount().doubleValue())));
    return pieChartData;
  }

  private <T> boolean searchForCategory(HashMap<StringProperty, Money> dataHashMap, int i,
      PieCategory category, PieCategory.Entry entry, List<T> list) {

    boolean found = false;
    if (list.get(i).equals(entry.getPattern())) {
      if (dataHashMap.containsKey(category.getName())) {
        dataHashMap.get(category.getName()).add(amount.get(i));
      } else {
        dataHashMap.put(category.getName(), amount.get(i));
      }
      found = true;
    }
    return found;
  }
*/

  public TreeMap<Date, List<DateDataPoint>> getSourceAsDateMap() {
    TreeMap<Date, List<DateDataPoint>> dateMap = new TreeMap<>();
    for (CamtEntry camtEntry : source) {
      if (dateMap.containsKey(camtEntry.getDate())) {
        dateMap.get(camtEntry.getDate()).add(camtEntry.getDataPoint());
      } else {
        List<DateDataPoint> list = new ArrayList<>();
        list.add(camtEntry.getDataPoint());
        dateMap.put(camtEntry.getDate(), list);
      }
    }
    return dateMap;
  }

  /*
  Construct a Camt-Obj. from a CSV-File
   */
  public void csvFileParser(Scanner sc) throws ParseException, IllegalArgumentException {
    if (sc == null || source == null) {
      throw new NullPointerException();
    }
    sc.useDelimiter("[\\n]");

    if (sc.hasNextLine()) {
      sc.nextLine(); // discard first line
    } else {
      throw new ParseException("File is empty or could not be parsed", 0);
    }



    while (sc.hasNext()) {
      final Money currAmount;
      final Date currDate;
      final DateDataPoint currDDP = new DateDataPoint();
      final String line = sc.next().replaceAll("\"", "");
      final String[] strings = line.split(";");
      if (strings.length != 17) {
        throw new ParseException("Line was not splittable into 17 parts with delimiter ';' ",
            source.size());
      }
      currDDP.setContractAccount(strings[0]);
      try {
        currDate = dateFormatter.parse(strings[1]);
      } catch (ParseException e) {
        e.printStackTrace();
        throw e;
      }
      currDDP.setValidationDate(strings[2]);
      currDDP.setTransferSpecification(strings[3]);
      currDDP.setUsage(strings[4]);
      currDDP.setCreditorId(strings[5]);
      currDDP.setMandateReference(strings[6]);
      currDDP.setCustomerReference(strings[7]);
      currDDP.setCollectionReference(strings[8]);
      currDDP.setDebitOriginalAmount(strings[9]);
      currDDP.setBackDebit(strings[10]);
      currDDP.setOtherParty(strings[11]);
      currDDP.setIban(strings[12]);
      currDDP.setBic(strings[13]);
      currAmount = new Money(strings[14].replace(",", "."));
      if (strings[15].equals(Money.EURO.toString())) {
        currDDP.setAmount(currAmount);
      } else {
        throw new IllegalArgumentException(
            "Currently not supporting other Currency's than EURO:"
                + "Problem at row: " + source.size() + 1 + ", found: " + line);
      }
      currDDP.setInfo(strings[16]);
      source.add(new CamtEntry(currDate, currDDP));
    }
  }

  public ObservableList<CamtEntry> getSource() {
    return source.get();
  }

  public ListProperty<CamtEntry> sourceProperty() {
    return source;
  }

  /*
  Class to hold the i. element from every list (except the date).
  This is used for the date map.
   */
  public static class DateDataPoint {

    private String contractAccount;
    private String validationDate;
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

    public DateDataPoint() {
    }

    public DateDataPoint(String contractAccount, String validationDate,
        String transferSpecification, String usage, String creditorId,
        String mandateReference, String customerReference, String collectionReference,
        String debitOriginalAmount, String backDebit, String otherParty, String iban,
        String bic, Money amount, String info) {
      this.contractAccount = contractAccount;
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
    }

    /*
        Getter.
         */
    public String getContractAccount() {
      return contractAccount;
    }

    public String getValidationDate() {
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

    public Money getAmount() {
      return amount;
    }

    //TODO
    // Hack for LineChartController.setupSeries: amountColumn.setCellValueFactory(new PropertyValueFactory<>("amountAsDouble"));
    public Double getAmountAsDouble() {
      return amount.getAmount().doubleValue();
    }

    public String getInfo() {
      return info;
    }

    /*
    Setter.
     */
    public void setContractAccount(String contractAccount) {
      this.contractAccount = contractAccount;
    }

    public void setValidationDate(String validationDate) {
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
      this.otherParty
          = otherParty;
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
  }

  public static class CamtEntry {

    public Set<Pair<ListType, String>> getSelectedFields(Set<ListType> listTypes) {
      Set<Pair<ListType, String>> resultSet = new HashSet<>();
      for (ListType listType : listTypes) {
        switch (listType) {

          case ACCOUNT_IBAN:
            break;
          case TRANSFER_DATE:
            resultSet.add(new Pair<>(listType, Utility.dateFormatter.format(getDate())));
            break;
          case VALIDATION_DATE:
            resultSet.add(new Pair<>(listType, getDataPoint().getValidationDate()));
            break;
          case TRANSFER_SPECIFICATION:
            resultSet.add(new Pair<>(listType, getDataPoint().getTransferSpecification()));
            break;
          case USAGE:
            resultSet.add(new Pair<>(listType, getDataPoint().getUsage()));
            break;
          case CREDITOR_ID:
            resultSet.add(new Pair<>(listType, getDataPoint().getCreditorId()));
            break;
          case MANDATE_REFERENCE:
            resultSet.add(new Pair<>(listType, getDataPoint().getMandateReference()));
            break;
          case CUSTOMER_REFERENCE_END_TO_END:
            resultSet.add(new Pair<>(listType, getDataPoint().getCustomerReference()));
            break;
          case COLLECTION_REFERENCE:
            resultSet.add(new Pair<>(listType, getDataPoint().getCollectionReference()));
            break;
          case DEBIT_ORIGINAL_AMOUNT:
            resultSet.add(new Pair<>(listType, getDataPoint().getDebitOriginalAmount()));
            break;
          case BACK_DEBIT:
            resultSet.add(new Pair<>(listType, getDataPoint().getBackDebit()));
            break;
          case OTHER_PARTY:
            resultSet.add(new Pair<>(listType, getDataPoint().getOtherParty()));
            break;
          case IBAN:
            resultSet.add(new Pair<>(listType, getDataPoint().getIban()));
            break;
          case BIC:
            resultSet.add(new Pair<>(listType, getDataPoint().getBic()));
            break;
          case AMOUNT:
            resultSet.add(new Pair<>(listType, getDataPoint().getAmount().toString()));
            break;
          case INFO:
            resultSet.add(new Pair<>(listType, getDataPoint().getInfo()));
            break;
        }
      }
      return resultSet;
    }

    private ObjectProperty<Date> date;
    private ObjectProperty<DateDataPoint> dataPoint;

    public CamtEntry(Date date, DateDataPoint dataPoint) {
      this.date = new SimpleObjectProperty<>(date);
      this.dataPoint = new SimpleObjectProperty<>(dataPoint);
    }

    public Date getDate() {
      return date.get();
    }

    public ObjectProperty<Date> dateProperty() {
      return date;
    }

    public void setDate(Date date) {
      this.date.set(date);
    }

    public DateDataPoint getDataPoint() {
      return dataPoint.get();
    }

    public ObjectProperty<DateDataPoint> dataPointProperty() {
      return dataPoint;
    }

    public void setDataPoint(DateDataPoint dataPoint) {
      this.dataPoint.set(dataPoint);
    }
  }
}
