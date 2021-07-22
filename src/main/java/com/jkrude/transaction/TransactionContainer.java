package com.jkrude.transaction;

import com.jkrude.material.Money;
import com.jkrude.material.Utility;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.TreeMap;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TransactionContainer {

  private final ListProperty<ExtendedTransaction> source;


  /*
   * Constructor
   */
  public TransactionContainer() {
    source = new SimpleListProperty<>(FXCollections.observableArrayList());
  }

  public TransactionContainer(ObservableList<Transaction> transactions) {
    source = new SimpleListProperty<>(
        FXCollections.observableList(new ArrayList<>(transactions.size())));
    transactions.stream().map(ExtendedTransaction::new).forEach(source::add);
  }

  public TransactionContainer(Scanner sc) throws ParseException, IllegalArgumentException {
    this();
    this.csvFileParser(sc);
  }

  /*
   * Collect all transactions according to their date.
   */
  public TreeMap<LocalDate, List<ExtendedTransaction>> getSourceAsDateMap() {

    TreeMap<LocalDate, List<ExtendedTransaction>> dateMap = new TreeMap<>();
    for (ExtendedTransaction extendedTransaction : source) {
      LocalDate date = extendedTransaction.getBaseTransaction().getDate();
      if (dateMap.containsKey(date)) {
        dateMap.get(date).add(extendedTransaction);
      } else {
        dateMap.put(date, new ArrayList<>(List.of(extendedTransaction))); // modifiable
      }

    }
    return dateMap;
  }

  /*
   * Construct a Camt-Obj. from a CSV-File
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
      final LocalDate currDate;
      final Transaction transaction = new Transaction();
      final String line = sc.next().replaceAll("\"", "");
      final String[] strings = line.split(";");
      if (strings.length != 17) {
        throw new ParseException("Line was not splittable into 17 parts with delimiter ';' ",
            source.size());
      }
      transaction.setAccountIban(strings[0]);
      currDate = Utility.parse(strings[1]);
      transaction.setDate(currDate);
      if (strings[2].isBlank()) {
        transaction.setValidationDate(null);
      } else {
        LocalDate validationDate = Utility.parse(strings[1]);
        transaction.setValidationDate(validationDate);
      }
      transaction.setTransferSpecification(strings[3]);
      transaction.setUsage(strings[4]);
      transaction.setCreditorId(strings[5]);
      transaction.setMandateReference(strings[6]);
      transaction.setCustomerReference(strings[7]);
      transaction.setCollectionReference(strings[8]);
      transaction.setDebitOriginalAmount(strings[9]);
      transaction.setBackDebit(strings[10]);
      transaction.setOtherParty(strings[11]);
      transaction.setIban(strings[12]);
      transaction.setBic(strings[13]);
      currAmount = new Money(strings[14].replace(",", "."));
      if (strings[15].equals(Money.EURO.toString())) {
        transaction.setAmount(currAmount);
      } else {
        throw new IllegalArgumentException(
            "Currently not supporting other Currency's than EURO:"
                + "Problem at row: " + source.size() + 1 + ", found: " + line);
      }
      transaction.setInfo(strings[16]);
      source.add(new ExtendedTransaction(transaction));
    }
  }

  public ObservableList<ExtendedTransaction> getSourceRO() {
    return FXCollections.unmodifiableObservableList(source);
  }

  // Should only be used for TransactionTable.
  public ObservableList<ExtendedTransaction> getSource() {
    return source;
  }

  public void addExtendedTransaction(ExtendedTransaction transaction) {
    source.add(transaction);
  }

  public boolean removeExtendedTransaction(ExtendedTransaction transaction) {
    return source.remove(transaction);
  }

  public String getName() {
    TreeMap<LocalDate, List<ExtendedTransaction>> asDateMap = this.getSourceAsDateMap();
    String firstDate = Utility.DATE_TIME_FORMATTER.format(asDateMap.firstEntry().getKey());
    String lastDate = Utility.DATE_TIME_FORMATTER.format(asDateMap.lastEntry().getKey());
    return firstDate + " - " + lastDate;
  }

}
