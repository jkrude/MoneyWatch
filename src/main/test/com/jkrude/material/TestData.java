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
        "DE04100500006013719070",
        LocalDate.parse("01.10.19", Utility.DATE_TIME_FORMATTER),
        "GUTSCHR. UEBERW. DAUERAUFTR",
        "Monatlicher Unterhalt ab November 2018 ",
        "", "", "", "", "", "",
        "Dr. Heiko Krude",
        "DE21100500001062583945",
        "BELADEBEXXX",
        new Money(800.00),
        "Umsatz gebucht"));
    entries.add(new Transaction(
        LocalDate.parse("25.09.19", Utility.DATE_TIME_FORMATTER),
        "DE04100500006013719070",
        LocalDate.parse("30.09.19", Utility.DATE_TIME_FORMATTER),
        "KARTENZAHLUNG",
        "2019-09-27T10:36 Debitk.1 2022-12 ",
        "", "", "", "", "", "",
        "Wiener Feinbaecker//Berlin/DE",
        "DE85120800000101752405",
        "DRESDEFF120",
        new Money(-3.25),
        "Umsatz gebucht"));
    entries.add(
        new Transaction(
            LocalDate.parse("25.09.19", Utility.DATE_TIME_FORMATTER),
            "DE04100500006013719070",
            LocalDate.parse("30.09.19", Utility.DATE_TIME_FORMATTER),
            "FOLGELASTSCHRIFT",
            "PP.7515.PP . BVG, Ihr Einkauf bei BVG ",
            "LU96ZZZ0000000000000000058",
            "5LFJ224TP5YA4",
            "", "", "", "",
            "PayPal (Europe) S.a.r.l. et Cie., S.C.A.",
            "DE88500700100175526303",
            "DEUTDEFFXXX",
            new Money(-2.80),
            "Umsatz gebucht"));
    entries.add(new Transaction(
        LocalDate.parse("24.09.19", Utility.DATE_TIME_FORMATTER),
        "DE04100500006013719070",
        LocalDate.parse("30.09.19", Utility.DATE_TIME_FORMATTER),
        "FOLGELASTSCHRIFT",
        "PP.7515.PP . FLIXBUS, Ihr Einkauf bei FLIXBUS ",
        "LU96ZZZ0000000000000000058",
        "5LFJ224TP5YA4",
        "", "", "", "",
        "PayPal (Europe) S.a.r.l. et Cie., S.C.A.",
        "DE88500700100175526303",
        "DEUTDEFFXXX",
        new Money(-8.14),
        "Umsatz gebucht"));
    entries.add(new Transaction(
        LocalDate.parse("23.09.19", Utility.DATE_TIME_FORMATTER),
        "DE04100500006013719070",
        LocalDate.parse("26.09.19", Utility.DATE_TIME_FORMATTER),
        "LOHN  GEHALT",
        "Verdienstabrechnung 09.19/1 ",
        "", "", "", "", "", "",
        "HELMHOLTZ-ZENTRUM DRESDEN-ROSSENDORF E.V.",
        "DE42850800000402657300",
        "DRESDEFF850",
        new Money(850.00),
        "Umsatz gebucht"));
    entries.add(new Transaction(
        LocalDate.parse("03.02.20", Utility.DATE_TIME_FORMATTER),
        "DE04100500006013719070",
        LocalDate.parse("03.02.20", Utility.DATE_TIME_FORMATTER),
        "DAUERAUFTRAG",
        "Miete + Internet Wachsbleichstrasse 47, Dresden",
        "", "", "", "", "", "",
        "Maximilian Walther",
        "DE15200411550651304800",
        "COBADEHD055",
        new Money(-303.33),
        "Umsatz gebucht"
    ));
    entries.add(new Transaction(
        LocalDate.parse("25.09.19", Utility.DATE_TIME_FORMATTER),
        "DE04100500006013719070",
        LocalDate.parse("25.09.19", Utility.DATE_TIME_FORMATTER),
        "KARTENZAHLUNG",
        "2019-09-24T18:25 Debitk.1 2022-12",
        "", "",
        "6.50321626188772E+025", "", "", "",
        "NETTO MARKEN-DISCOU//DRESDEN-FRIEDRICHS/DE",
        "DE68750200730008472092",
        "HYVEDEMM447",
        new Money(-5.27),
        "Umsatz gebucht"
    ));
    /*
    23.09 - -3.10
    25.09 - -12.10
    26.09 - 0.11
     */

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
          .fromPair(new Pair<>(Transaction.TransactionField.USAGE, "Schauburg"))
          .build());
      food.addRule(Rule.RuleBuilder
          .fromPair(new Pair<>(Transaction.TransactionField.IBAN, "DE68750200730008472092"))
          .addNote("Netto")
          .build());
      food.addRule(Rule.RuleBuilder
          .fromPair(new Pair<>(Transaction.TransactionField.IBAN, "DE85120800000101752405"))
          .addNote("Wiener Bäckerei")
          .build());
      travel.addRule(Rule.RuleBuilder
          .fromSet(Set.of(new Pair<>(Transaction.TransactionField.USAGE, "FLIXBUS"),
              new Pair<>(Transaction.TransactionField.OTHER_PARTY, "PayPal")))
          .build());
      travel.addRule(Rule.RuleBuilder
          .fromPair(new Pair<>(Transaction.TransactionField.USAGE, "Ihr Einkauf bei BVG"))
          .build());
      rent.addRule(Rule.RuleBuilder
          .fromPair(new Pair<>(Transaction.TransactionField.OTHER_PARTY, "Maximilian Walther"))
          .addNote("Miete")
          .build());
    } catch (ParseException e) {
      e.printStackTrace();
    }
    profile.setRootCategory(root);
    return profile;
  }
}
