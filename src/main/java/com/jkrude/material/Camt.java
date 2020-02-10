package com.jkrude.material;

import com.jkrude.material.PieCategory.Entry;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.TreeMap;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class Camt {

  public enum ListType {
    ACCOUNT_IBAN,
    TRANSFER_DATE,
    VALIDATION_DATE,
    TRANSFER_SPECIFICATION,
    USAGE,
    CREDITOR_ID,
    MANDATE_REFERENCE,
    CUSTOMER_REFERENCE_END_TO_END,
    COLLECTION_REFERENCE,
    DEBIT_ORIGINAL_AMOUNT,
    BACK_DEBIT,
    OTHER_PARTY,
    IBAN,
    BIC,
    //AMOUNT,
    INFO;

    @Override
    public String toString() {
      switch (this) {
        case IBAN:
          return "Iban";
        case USAGE:
          return "Verwendungszweck";
        case OTHER_PARTY:
          return "Beguenstigter/Zahlungspflichtiger";
        default:
          return super.toString();
      }
    }
  }

  private List<CamtEntry> source;

  private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yy");


  public Camt() {
    source = new ArrayList<>();
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
      if(dateMap.containsKey(camtEntry.getDate())){
        dateMap.get(camtEntry.getDate()).add(camtEntry.getDataPoint());
      }else{
        List<DateDataPoint> list = new ArrayList<>();
        list.add(camtEntry.getDataPoint());
        dateMap.put(camtEntry.getDate(),list);
      }
    }
    return dateMap;
  }

  /*
  Construct a Camt-Obj. from a CSV-File
   */
  public void csvFileParser(Scanner sc) throws ParseException, IllegalArgumentException {
    if (sc == null) {
      throw new NullPointerException();
    }
    sc.useDelimiter("[\\n]");

    if (sc.hasNextLine()) {
      sc.nextLine(); // discard first line
    }else{
      throw new ParseException("File is empty or could not be parsed", 0);
    }

    Money currAmount = null;
    Date currDate = null;
    DateDataPoint currDDP = new DateDataPoint();

    while (sc.hasNext()) {
      final String line = sc.next().replaceAll("\"", "");
      final String[] strings = line.split(";");
      if(strings.length != 17){
        throw new ParseException("Line was not splittable into 17 parts with delimiter ';' ",source.size());
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

  public List<CamtEntry> getSource(){
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
