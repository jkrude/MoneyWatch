package com.jkrude.material;

import com.jkrude.category.CategoryNode;
import com.jkrude.category.Rule;
import com.jkrude.material.TransactionContainer.Transaction;
import com.jkrude.material.TransactionContainer.TransactionField;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;

public class TestData {

  public static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yy");


  public static TransactionContainer getCamtWithTestData() {

    ObservableList<TransactionContainer.Transaction> entries = FXCollections.observableArrayList();
    try {
      entries.add(new TransactionContainer.Transaction(
          simpleDateFormat.parse("26.09.19"),
          "DE04100500006013719070",
          simpleDateFormat.parse("01.10.19"),
          "GUTSCHR. UEBERW. DAUERAUFTR",
          "Monatlicher Unterhalt ab November 2018 ",
          "", "", "", "", "", "",
          "Dr. Heiko Krude",
          "DE21100500001062583945",
          "BELADEBEXXX",
          new Money(800.00),
          "Umsatz gebucht"));
      entries.add(new TransactionContainer.Transaction(
          simpleDateFormat.parse("25.09.19"),
          "DE04100500006013719070",
          simpleDateFormat.parse("30.09.19"),
          "KARTENZAHLUNG",
          "2019-09-27T10:36 Debitk.1 2022-12 ",
          "", "", "", "", "", "",
          "Wiener Feinbaecker//Berlin/DE",
          "DE85120800000101752405",
          "DRESDEFF120",
          new Money(-3.25),
          "Umsatz gebucht"));
      entries.add(
          new TransactionContainer.Transaction(
              simpleDateFormat.parse("25.09.19"),
              "DE04100500006013719070",
              simpleDateFormat.parse("30.09.19"),
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
      entries.add(new TransactionContainer.Transaction(
          simpleDateFormat.parse("24.09.19"),
          "DE04100500006013719070",
          simpleDateFormat.parse("30.09.19"),
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
      entries.add(new TransactionContainer.Transaction(
          simpleDateFormat.parse("23.09.19"),
          "DE04100500006013719070",
          simpleDateFormat.parse("26.09.19"),
          "LOHN  GEHALT",
          "Verdienstabrechnung 09.19/1 ",
          "", "", "", "", "", "",
          "HELMHOLTZ-ZENTRUM DRESDEN-ROSSENDORF E.V.",
          "DE42850800000402657300",
          "DRESDEFF850",
          new Money(850.00),
          "Umsatz gebucht"));
      entries.add(new Transaction(
          simpleDateFormat.parse("03.02.20"),
          "DE04100500006013719070",
          simpleDateFormat.parse("03.02.20"),
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
          simpleDateFormat.parse("25.09.19"),
          "DE04100500006013719070",
          simpleDateFormat.parse("25.09.19"),
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
    } catch (ParseException e) {
      e.printStackTrace();
    }
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
          .fromPair(new Pair<>(TransactionField.USAGE, "Schauburg"))
          .build());
      food.addRule(Rule.RuleBuilder
          .fromPair(new Pair<>(TransactionField.IBAN, "DE68750200730008472092"))
          .addNote("Netto")
          .build());
      food.addRule(Rule.RuleBuilder
          .fromPair(new Pair<>(TransactionField.IBAN, "DE85120800000101752405"))
          .addNote("Wiener Bäckerei")
          .build());
      travel.addRule(Rule.RuleBuilder
          .fromSet(Set.of(new Pair<>(TransactionField.USAGE, "FLIXBUS"),
              new Pair<>(TransactionField.OTHER_PARTY, "PayPal")))
          .build());
      travel.addRule(Rule.RuleBuilder
          .fromPair(new Pair<>(TransactionField.USAGE, "Ihr Einkauf bei BVG"))
          .build());
      rent.addRule(Rule.RuleBuilder
          .fromPair(new Pair<>(TransactionField.OTHER_PARTY, "Maximilian Walther"))
          .addNote("Miete")
          .build());
    } catch (ParseException e) {
      e.printStackTrace();
    }
    profile.setRootCategory(root);
    return profile;
  }
}
