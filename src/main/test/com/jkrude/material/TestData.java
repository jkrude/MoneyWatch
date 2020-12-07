package com.jkrude.material;

import com.jkrude.category.CategoryNode;
import com.jkrude.category.Rule;
import com.jkrude.transaction.Transaction;
import com.jkrude.transaction.TransactionContainer;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;

public class TestData {

  public static TransactionContainer getCamtWithTestData() {

    ObservableList<Transaction> entries = FXCollections.observableArrayList();
    entries.add(new Transaction(
        LocalDate.parse("26.09.19", Utility.DATE_TIME_FORMATTER),
        "DE92500105176644294936",
        LocalDate.parse("01.10.19", Utility.DATE_TIME_FORMATTER),
        "STANDING ORDER",
        "Monthly salary",
        "", "", "", "", "", "",
        "Workplace2",
        "DE51500105174671915231",
        "BELADEBEXXX",
        new Money(800.00),
        "Revenue booked"));
    entries.add(new Transaction(
        LocalDate.parse("25.09.19", Utility.DATE_TIME_FORMATTER),
        "DE92500105176644294936",
        LocalDate.parse("30.09.19", Utility.DATE_TIME_FORMATTER),
        "Card payment",
        "2019-09-27T10:36 Debitk.1 2022-12 ",
        "", "", "", "", "", "",
        "Backery1",
        "DE55500105179467748128",
        "DRESDEFF120",
        new Money(-3.25),
        "Revenue booked"));
    entries.add(
        new Transaction(
            LocalDate.parse("25.09.19", Utility.DATE_TIME_FORMATTER),
            "DE92500105176644294936",
            LocalDate.parse("30.09.19", Utility.DATE_TIME_FORMATTER),
            "DIRECT DEBIT",
            "PP.7515.PP . Public transport ticket",
            "LU96ZZZ0000000000000000058",
            "5LFJ224TP5YA4",
            "", "", "", "",
            "PayPal (Europe) S.a.r.l. et Cie., S.C.A.",
            "DE71500105178342244327",
            "DEUTDEFFXXX",
            new Money(-2.80),
            "Revenue booked"));
    entries.add(new Transaction(
        LocalDate.parse("24.09.19", Utility.DATE_TIME_FORMATTER),
        "DE92500105176644294936",
        LocalDate.parse("30.09.19", Utility.DATE_TIME_FORMATTER),
        "DIRECT DEBIT",
        "PP.7515.PP . FLIXBUS, Your purchase by FLIXBUS ",
        "LU11ZZZ0000000000000000011",
        "5XXJ224XX5XX4",
        "", "", "", "",
        "PayPal (Europe) S.a.r.l. et Cie., S.C.A.",
        "DE67500105173394578785",
        "DEUTDEFFXXX",
        new Money(-8.14),
        "Revenue booked"));
    entries.add(new Transaction(
        LocalDate.parse("23.09.19", Utility.DATE_TIME_FORMATTER),
        "DE92500105176644294936",
        LocalDate.parse("26.09.19", Utility.DATE_TIME_FORMATTER),
        "SALARY",
        "Salary",
        "", "", "", "", "", "",
        "Workplace1",
        "DE25500105177743864571",
        "DRESDEFF850",
        new Money(1000.00),
        "Revenue booked"));
    entries.add(new Transaction(
        LocalDate.parse("30.09.19", Utility.DATE_TIME_FORMATTER),
        "DE92500105176644294936",
        LocalDate.parse("30.09.19", Utility.DATE_TIME_FORMATTER),
        "Standing Order",
        "Rent",
        "", "", "", "", "", "",
        "Max Mustermann",
        "DE38500105174588747568",
        "COBADEHD055",
        new Money(-280.33),
        "Revenue booked"
    ));
    entries.add(new Transaction(
        LocalDate.parse("25.09.19", Utility.DATE_TIME_FORMATTER),
        "DE92500105176644294936",
        LocalDate.parse("25.09.19", Utility.DATE_TIME_FORMATTER),
        "Card payment",
        "2019-09-24T18:25 Debitk.1 2022-12",
        "", "",
        "6.50321626180002E+025", "", "", "",
        "DISCOUNTER1",
        "DE43500105171223732312",
        "HYVEDEMM447",
        new Money(-5.27),
        "Revenue booked"
    ));
    entries.add(new Transaction(
        LocalDate.parse("04.10.19", Utility.DATE_TIME_FORMATTER),
        "DE92500105176644294936",
        LocalDate.parse("04.10.19", Utility.DATE_TIME_FORMATTER),
        "Card payment",
        "2019-09- 25T20:48 Debitk.1 202",
        "", "",
        "1.11111111111111E+011", "", "", "",
        "Cinema",
        "DEDE93450000010100199999",
        "WELAXX1XXXX",
        new Money(-17.00),
        "Revenue booked"
    ));

    return new TransactionContainer(entries);
  }


  public static Profile getProfile() {
    Profile profile = new Profile();
    CategoryNode root = new CategoryNode("debits");
    CategoryNode joy = new CategoryNode("Joy");
    CategoryNode necessaries = new CategoryNode("Necessaries");
    CategoryNode cinema = new CategoryNode("Cinema");
    CategoryNode food = new CategoryNode("Food");
    CategoryNode travel = new CategoryNode("Travel");
    CategoryNode rent = new CategoryNode("Rent");
    root.addCategory(joy);
    root.addCategory(necessaries);
    joy.addCategory(cinema);
    joy.addCategory(travel);
    necessaries.addCategory(food);
    necessaries.addCategory(rent);
    try {
      cinema.addRule(Rule.RuleBuilder
          .fromPair(new Pair<>(Transaction.TransactionField.USAGE, "Cinema"))
          .build());
      food.addRule(Rule.RuleBuilder
          .fromPair(new Pair<>(Transaction.TransactionField.IBAN, "DE43500105171223732312"))
          .addNote("Discounter")
          .build());
      food.addRule(Rule.RuleBuilder
          .fromPair(new Pair<>(Transaction.TransactionField.IBAN, "DE55500105179467748128"))
          .addNote("Backery")
          .build());
      travel.addRule(Rule.RuleBuilder
          .fromSet(Set.of(new Pair<>(Transaction.TransactionField.USAGE, "FLIXBUS"),
              new Pair<>(Transaction.TransactionField.OTHER_PARTY, "PayPal")))
          .build());
      travel.addRule(Rule.RuleBuilder
          .fromPair(new Pair<>(Transaction.TransactionField.USAGE, "Public transport"))
          .build());
      rent.addRule(Rule.RuleBuilder
          .fromPair(new Pair<>(Transaction.TransactionField.OTHER_PARTY, "Max Mustermann"))
          .addNote("Rent")
          .build());
    } catch (ParseException e) {
      e.printStackTrace();
    }
    profile.setRootCategory(root);
    return profile;
  }
}
