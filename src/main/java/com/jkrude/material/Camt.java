package com.jkrude.material;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;

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
    INFO
  }


  private List<String> accountIban;
  private List<Date> transferDate;
  private List<String> validationDate;
  private List<String> transferSpecification;
  private List<String> usage;
  private List<String> creditorId;
  private List<String> mandateReference;
  private List<String> customerReference;
  private List<String> collectionReference;
  private List<String> debitOriginalAmount;
  private List<String> backDebit;
  private List<String> otherParty;
  private List<String> iban;
  private List<String> bic;
  private List<Money> amount;
  private List<String> info;

  // Maps every date to a number of transactions that happened that day.
  private TreeMap<Date, List<DateDataPoint>> dateMap;

  private List<XYChart.Data<Number, Number>> lineChartData;
  private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yy");


  public Camt() {
    this.accountIban = new ArrayList<>();
    this.transferDate = new ArrayList<>();
    this.validationDate = new ArrayList<>();
    this.transferSpecification = new ArrayList<>();
    this.usage = new ArrayList<>();
    this.creditorId = new ArrayList<>();
    this.mandateReference = new ArrayList<>();
    this.customerReference = new ArrayList<>();
    this.collectionReference = new ArrayList<>();
    this.debitOriginalAmount = new ArrayList<>();
    this.backDebit = new ArrayList<>();
    this.otherParty = new ArrayList<>();
    this.iban = new ArrayList<>();
    this.bic = new ArrayList<>();
    this.amount = new ArrayList<>();
    this.info = new ArrayList<>();

    this.dateMap = new TreeMap<>();
  }

  public Camt(
      List<String> accountIban,
      List<Date> transferDate,
      List<String> validationDate,
      List<String> transferSpecification,
      List<String> usage,
      List<String> creditorId,
      List<String> mandateReference,
      List<String> customerReference,
      List<String> collectionReference,
      List<String> debitOriginalAmount,
      List<String> backDebit,
      List<String> otherParty,
      List<String> iban,
      List<String> bic,
      List<Money> amount,
      List<String> info) {
    this.accountIban = accountIban;
    this.transferDate = transferDate;
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

    this.dateMap = new TreeMap<>();
    generateDateMap();

  }

  public Camt(Scanner sc) throws ParseException, IllegalArgumentException {
    this();
    this.csvFileParser(sc);
    this.generateDateMap();
  }

  /*
  LineChart associated methods.
   */
  public List<XYChart.Data<Number, Number>> getLineChartData() {
    if (lineChartData == null || lineChartData.isEmpty()) {
      generateLineChart();
    }
    return lineChartData;
  }

  private void generateLineChart() {
    lineChartData = new ArrayList<>();
    Set<Date> set = dateMap.keySet();
    Money currAmount = new Money(0);

    for (Date d : set) {
      for (DateDataPoint dateDataPoint : dateMap.get(d)) {
        currAmount.add(dateDataPoint.amount);
      }
      lineChartData.add(new Data<>(d.toInstant().toEpochMilli(), currAmount.getValue()));
    }
  }

  // TODO
  public List<DateDataPoint> getDPsForChartData(XYChart.Data<Number, Number> xyData) {
    for (Date date : dateMap.keySet()) {
      if (date.toInstant().toEpochMilli() == xyData.getXValue().longValue()) {
        return dateMap.get(date);
      }
    }
    return null;
  }


  /*
  PieChart associated methods.
   */
  public ArrayList<PieChart.Data> getPieChartData(HashSet<PieCategory> categories) {

    ArrayList<PieChart.Data> pieChartData = new ArrayList<>();
    HashMap<String, Money> dataHashMap = new HashMap<>();

    for (int i = 0; i < this.transferDate.size(); ++i) {
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
            case USAGE:
              found = searchForCategory(dataHashMap, i, category, entry, usage);
              break;
            case OTHER_PARTY:
              found = searchForCategory(dataHashMap, i, category, entry, otherParty);
              break;
          }
        }
      }
    }
    dataHashMap.forEach((String key, Money value) -> pieChartData
        .add(new PieChart.Data(key, value.getValue().doubleValue())));
    return pieChartData;
  }

  private boolean searchForCategory(HashMap<String, Money> dataHashMap, int i,
      PieCategory category, PieCategory.Entry entry, List<String> list) {

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


  public TreeMap<Date, List<DateDataPoint>> getDateMap() {
    if (dateMap == null) {
      generateDateMap();
    }
    return dateMap;
  }

  private void generateDateMap() {

    Date currDate = transferDate.get(0);
    List<DateDataPoint> list = new ArrayList<>();
    /*
    for loop adds list from i-1 if new date occurs
    after for loop checks where to put last list
     */
    for (int i = 0; i < transferDate.size(); i++) {
      DateDataPoint dateDataPoint = new DateDataPoint();
      dateDataPoint.setContractAccount(accountIban.get(i));
      dateDataPoint.setTransferValidation(validationDate.get(i));
      dateDataPoint.setTransferSpecification(transferSpecification.get(i));
      dateDataPoint.setUsage(usage.get(i));
      dateDataPoint.setCreditorId(creditorId.get(i));
      dateDataPoint.setMandateReference(mandateReference.get(i));
      dateDataPoint.setCustomerReference(customerReference.get(i));
      dateDataPoint.setCollectorReference(collectionReference.get(i));
      dateDataPoint.setDebitOriginalAmount(debitOriginalAmount.get(i));
      dateDataPoint.setBackDebit(backDebit.get(i));
      dateDataPoint.setReceiverOrPayer(otherParty.get(i));
      dateDataPoint.setIban(iban.get(i));
      dateDataPoint.setBic(bic.get(i));
      dateDataPoint.setAmount(amount.get(i));
      dateDataPoint.setInfo(info.get(i));
      // date != dates[i-1] -> save list (with entries from i-1)
      if (!currDate.equals(transferDate.get(i))) {
        dateMap.put(currDate, list);
        list = new ArrayList<>();
        currDate = transferDate.get(i);
      }
      list.add(dateDataPoint);
    }
    // Check where to put last generated list (DataPoints)
    if (currDate.equals(transferDate.get(transferDate.size() - 1))) {
      dateMap.put(currDate, list);
    } else {
      dateMap.get(currDate).addAll(list);
    }
  }

  /*
  Construct a Camt-Obj. from a CSV-File
   */
  public void csvFileParser(Scanner sc) throws ParseException, IllegalArgumentException {
    if (sc == null) {
      throw new NullPointerException();
    }
    sc.useDelimiter("[;\\n]");
    String line;
    int column = 0; // ranges form 0 to 16
    if (sc.hasNextLine()) {
      sc.nextLine(); // discard first line
    }

    Money currAmount = null;
    Date currDate = null;
    DateDataPoint currDateDataPoint = new DateDataPoint();
    while (sc.hasNext()) {
      line = sc.next().replaceAll("\"", "");
      switch (column) {
        case 0:
          accountIban.add(line);
          currDateDataPoint.setContractAccount(line);
          break;
        case 1:
          currDate = dateFormatter.parse(line);
          transferDate.add(currDate);
          break;
        case 2:
          validationDate.add(line);
          break;
        case 3:
          transferSpecification.add(line);
          break;
        case 4:
          usage.add(line);
          break;
        case 5:
          creditorId.add(line);
          break;
        case 6:
          mandateReference.add(line);
          break;
        case 7:
          customerReference.add(line);
          break;
        case 8:
          collectionReference.add(line);
          break;
        case 9:
          debitOriginalAmount.add(line);
          break;
        case 10:
          backDebit.add(line);
          break;
        case 11:
          otherParty.add(line);
          break;
        case 12:
          iban.add(line);
          break;
        case 13:
          bic.add(line);
          break;
        case 14:
          currAmount = new Money(line.replace(",", "."));
          break;
        case 15:
          if (line.equals(Money.EURO.toString())) {
            amount.add(currAmount);
          } else {
            throw new IllegalArgumentException(
                "Currently not supporting other Currency's than EURO:"
                    + "Problem at row: " + amount.size() + 1 + ", found: " + line);
          }
          break;
        case 16:
          info.add(line);
          break;
      }
      column++;
      column %= 17;
    }
  }

  /*
  Getter.
   */
  public List<Date> getTransferDate() {
    return transferDate;
  }

  public List<Money> getAmount() {
    return amount;
  }

  public List<String> getAccountIban() {
    return accountIban;
  }

  public List<String> getValidationDate() {
    return validationDate;
  }

  public List<String> getTransferSpecification() {
    return transferSpecification;
  }

  public List<String> getUsage() {
    return usage;
  }

  public List<String> getCreditorId() {
    return creditorId;
  }

  public List<String> getMandateReference() {
    return mandateReference;
  }

  public List<String> getCustomerReference() {
    return customerReference;
  }

  public List<String> getCollectionReference() {
    return collectionReference;
  }

  public List<String> getDebitOriginalAmount() {
    return debitOriginalAmount;
  }

  public List<String> getBackDebit() {
    return backDebit;
  }

  public List<String> getOtherParty() {
    return otherParty;
  }

  public List<String> getIban() {
    return iban;
  }

  public List<String> getBic() {
    return bic;
  }

  public List<String> getInfo() {
    return info;
  }

  /*
  Class to hold the i. element from every list (except the date).
  This is used for the date map.
   */
  public static class DateDataPoint {

    private String contractAccount;
    private String transferValidation;
    private String transferSpecification;
    private String usage;
    private String creditorId;
    private String mandateReference;
    private String customerReference;
    private String collectorReference;
    private String debitOriginalAmount;
    private String backDebit;
    private String receiverOrPayer;
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

    public String getTransferValidation() {
      return transferValidation;
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

    public String getCollectorReference() {
      return collectorReference;
    }

    public String getDebitOriginalAmount() {
      return debitOriginalAmount;
    }

    public String getBackDebit() {
      return backDebit;
    }

    public String getReceiverOrPayer() {
      return receiverOrPayer;
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

    public String getInfo() {
      return info;
    }

    /*
    Setter.
     */
    public void setContractAccount(String contractAccount) {
      this.contractAccount = contractAccount;
    }

    public void setTransferValidation(String transferValidation) {
      this.transferValidation = transferValidation;
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

    public void setCollectorReference(String collectorReference) {
      this.collectorReference = collectorReference;
    }

    public void setDebitOriginalAmount(String debitOriginalAmount) {
      this.debitOriginalAmount = debitOriginalAmount;
    }

    public void setBackDebit(String backDebit) {
      this.backDebit = backDebit;
    }

    public void setReceiverOrPayer(String receiverOrPayer) {
      this.receiverOrPayer = receiverOrPayer;
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

}
