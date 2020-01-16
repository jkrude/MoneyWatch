package com.jkrude.material;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import javafx.scene.chart.XYChart;

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

  }

  public Camt(Scanner sc) {
    this();
    this.csvFileParser(sc);
  }


  public boolean firstEntryIsFirstDate(){
    return transferDate.get(0).compareTo(transferDate.get(transferDate.size() - 1)) < 0;
  }

  private void generateLineChart() {
    lineChartData = new ArrayList<>();
    assert (amount.size() == transferDate.size());
    int start;
    int x;
    int finished;
    // Is the first entry after the last date?
    if(firstEntryIsFirstDate()){
      // reverse traversing
      start = transferDate.size()-1;
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
      if(!dateInLoop.equals(currDate)){
        //int currDateInteger = Utility.mapDateToInteger(currDate.toString());
        long instant = currDate.toInstant().toEpochMilli();
        lineChartData.add(new XYChart.Data<>(instant,currAmount.getValue()));
        currDate = dateInLoop;
      }
      currAmount.add(amount.get(i));
    }
    long instant = currDate.toInstant().toEpochMilli();
    lineChartData.add(new XYChart.Data<>(instant, currAmount.getValue()));
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
    while (sc.hasNext()) {
      line = sc.next().replaceAll("\"", "");
      switch (column) {
        case 0:
          contractAccount.add(line);
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
}
