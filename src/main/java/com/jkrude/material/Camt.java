package com.jkrude.material;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;

public class Camt {

  private List<String> contractAccount;
  private List<Date> transferDate;
  private List<String> transferValidation;
  private List<String> transferSpecification;
  private List<String> usage;
  private List<String> creditorId;
  private List<String> mandateReference;
  private List<String> customerReference;
  private List<String> collectorReference;
  private List<String> debitOriginalAmount;
  private List<String> backDebit;
  private List<String> receiverOrPayer;
  private List<String> iban;
  private List<String> bic;
  private List<Money> amount;
  private List<String> info;

  private TreeMap<Date, List<DataPoint>> dateMap;

  private List<XYChart.Data<Number, Number>> lineChartData;
  private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yy");


  public Camt() {
    this.contractAccount = new ArrayList<>();
    this.transferDate = new ArrayList<>();
    this.transferValidation = new ArrayList<>();
    this.transferSpecification = new ArrayList<>();
    this.usage = new ArrayList<>();
    this.creditorId = new ArrayList<>();
    this.mandateReference = new ArrayList<>();
    this.customerReference = new ArrayList<>();
    this.collectorReference = new ArrayList<>();
    this.debitOriginalAmount = new ArrayList<>();
    this.backDebit = new ArrayList<>();
    this.receiverOrPayer = new ArrayList<>();
    this.iban = new ArrayList<>();
    this.bic = new ArrayList<>();
    this.amount = new ArrayList<>();
    this.info = new ArrayList<>();

    this.dateMap = new TreeMap<>();
  }

  public Camt(
      List<String> contractAccount,
      List<Date> transferDate,
      List<String> transferValidation,
      List<String> transferSpecification,
      List<String> usage,
      List<String> creditorId,
      List<String> mandateReference,
      List<String> customerReference,
      List<String> collectorReference,
      List<String> debitOriginalAmount,
      List<String> backDebit,
      List<String> receiverOrPayer,
      List<String> iban,
      List<String> bic,
      List<Money> amount,
      List<String> info) {
    this.contractAccount = contractAccount;
    this.transferDate = transferDate;
    this.transferValidation = transferValidation;
    this.transferSpecification = transferSpecification;
    this.usage = usage;
    this.creditorId = creditorId;
    this.mandateReference = mandateReference;
    this.customerReference = customerReference;
    this.collectorReference = collectorReference;
    this.debitOriginalAmount = debitOriginalAmount;
    this.backDebit = backDebit;
    this.receiverOrPayer = receiverOrPayer;
    this.iban = iban;
    this.bic = bic;
    this.amount = amount;
    this.info = info;

    this.dateMap = new TreeMap<>();
    generateDateMap();

  }

  public Camt(Scanner sc) {
    this();
    this.csvFileParser(sc);
    this.generateDateMap();
  }


  public boolean firstEntryIsFirstDate() {
    return transferDate.get(0).compareTo(transferDate.get(transferDate.size() - 1)) < 0;
  }

  private void generateLineChart() {
    /*
    lineChartData = new ArrayList<>();
    assert (amount.size() == transferDate.size());
    int start;
    int x;
    int finished;
    // Is the first entry after the last date?
    if (firstEntryIsFirstDate()) {
      // reverse traversing
      start = transferDate.size() - 1;
      x = -1;
      finished = -1;
    } else {
      // normal traversing
      start = 0;
      x = 1;
      finished = transferDate.size();
    }

    Date currDate = transferDate.get(start);
    Money currAmount = new Money();
    for (int i = start; i != finished; i += x) {
      Date dateInLoop = transferDate.get(i);
      if (!dateInLoop.equals(currDate)) {
        //int currDateInteger = Utility.mapDateToInteger(currDate.toString());
        long instant = currDate.toInstant().toEpochMilli();
        lineChartData.add(new XYChart.Data<>(instant, currAmount.getValue()));
        currDate = dateInLoop;
      }
      currAmount.add(amount.get(i));
    }
    long instant = currDate.toInstant().toEpochMilli();
    lineChartData.add(new XYChart.Data<>(instant, currAmount.getValue()));
     */
    lineChartData = new ArrayList<>();
    Set<Date> set = dateMap.keySet();
    Money currAmount = new Money(0);
    List<DataPoint> arr;

    for (Date d : set) {
      for (DataPoint dataPoint : dateMap.get(d)) {
        currAmount.add(dataPoint.amount);
      }
      lineChartData.add(new Data<>(d.toInstant().toEpochMilli(), currAmount.getValue()));
    }
  }

  private void generateDateMap() {

    Date currDate = transferDate.get(0);
    List<DataPoint> list = new ArrayList<>();
    /*
    for loop adds list from i-1 if new date occurs
    after for loop checks where to put last list
     */
    for (int i = 0; i < transferDate.size(); i++) {
      DataPoint dataPoint = new DataPoint();
      dataPoint.setContractAccount(contractAccount.get(i));
      dataPoint.setTransferValidation(transferValidation.get(i));
      dataPoint.setTransferSpecification(transferSpecification.get(i));
      dataPoint.setUsage(usage.get(i));
      dataPoint.setCreditorId(creditorId.get(i));
      dataPoint.setMandateReference(mandateReference.get(i));
      dataPoint.setCustomerReference(customerReference.get(i));
      dataPoint.setCollectorReference(collectorReference.get(i));
      dataPoint.setDebitOriginalAmount(debitOriginalAmount.get(i));
      dataPoint.setBackDebit(backDebit.get(i));
      dataPoint.setReceiverOrPayer(receiverOrPayer.get(i));
      dataPoint.setIban(iban.get(i));
      dataPoint.setBic(bic.get(i));
      dataPoint.setAmount(amount.get(i));
      dataPoint.setInfo(info.get(i));
      // date != dates[i-1] -> save list (with entries from i-1)
      if (!currDate.equals(transferDate.get(i))) {
        dateMap.put(currDate, list);
        list = new ArrayList<>();
        currDate = transferDate.get(i);
      }
      list.add(dataPoint);
    }
    // Check where to put last generated list (DataPoints)
    if (currDate.equals(transferDate.get(transferDate.size() - 1))) {
      dateMap.put(currDate, list);
    } else {
      dateMap.get(currDate).addAll(list);
    }
  }

  public void csvFileParser(Scanner sc) {
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
    DataPoint currDataPoint = new DataPoint();
    while (sc.hasNext()) {
      line = sc.next().replaceAll("\"", "");
      switch (column) {
        case 0:
          contractAccount.add(line);
          currDataPoint.setContractAccount(line);
          break;
        case 1:
          try {
            currDate = dateFormatter.parse(line);
            transferDate.add(currDate);
          } catch (ParseException e) {
            e.printStackTrace();
            AlertBox.display("Problem with date-format!",
                "The Date needs to be formatted as dd.mm.yy!");
          }
          break;
        case 2:
          transferValidation.add(line);
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
          collectorReference.add(line);
          break;
        case 9:
          debitOriginalAmount.add(line);
          break;
        case 10:
          backDebit.add(line);
          break;
        case 11:
          receiverOrPayer.add(line);
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
            AlertBox.display("Import Error",
                "Currently not supporting other Currency's than EURO");
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

  public List<XYChart.Data<Number, Number>> getLineChartData() {
    if (lineChartData == null || lineChartData.isEmpty()) {
      generateLineChart();
    }
    return lineChartData;
  }

  public TreeMap<Date, List<DataPoint>> getDateMap() {
    return dateMap;
  }

  public List<Date> getTransferDate() {
    return transferDate;
  }

  public List<Money> getAmount() {
    return amount;
  }

  public List<String> getContractAccount() {
    return contractAccount;
  }

  public List<String> getTransferValidation() {
    return transferValidation;
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

  public List<String> getCollectorReference() {
    return collectorReference;
  }

  public List<String> getDebitOriginalAmount() {
    return debitOriginalAmount;
  }

  public List<String> getBackDebit() {
    return backDebit;
  }

  public List<String> getReceiverOrPayer() {
    return receiverOrPayer;
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

  protected static class DataPoint {

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
