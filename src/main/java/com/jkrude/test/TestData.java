package com.jkrude.test;

import com.jkrude.material.Camt;
import com.jkrude.material.Camt.CamtEntry;
import com.jkrude.material.Camt.DateDataPoint;
import com.jkrude.material.Camt.ListType;
import com.jkrude.material.Money;
import com.jkrude.material.PieCategory;
import com.jkrude.material.Profile;
import com.jkrude.material.Rule;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;

public class TestData {

  public static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yy");


  public static Camt getCamtWithTestData() {

    ObservableList<CamtEntry> entries = FXCollections.observableArrayList();
    try {
      entries.add(new CamtEntry(
          simpleDateFormat.parse("26.09.19"),
          new DateDataPoint(
              "DE04100500006013719070",
              "01.10.19",
              "GUTSCHR. UEBERW. DAUERAUFTR",
              "Monatlicher Unterhalt ab November 2018 ",
              "", "", "", "", "", "",
              "Dr. Heiko Krude",
              "DE21100500001062583945",
              "BELADEBEXXX",
              new Money(800.00),
              "Umsatz gebucht")));
      entries.add(new CamtEntry(
          simpleDateFormat.parse("25.09.19"),
          new DateDataPoint(
              "DE04100500006013719070",
              "30.09.19",
              "KARTENZAHLUNG",
              "2019-09-27T10:36 Debitk.1 2022-12 ",
              "", "", "", "", "", "",
              "Wiener Feinbaecker//Berlin/DE",
              "DE85120800000101752405",
              "DRESDEFF120",
              new Money(-3.25),
              "Umsatz gebucht")));
      entries.add(
          new CamtEntry(
              simpleDateFormat.parse("25.09.19"),
              new DateDataPoint(
                  "DE04100500006013719070",
                  "30.09.19",
                  "FOLGELASTSCHRIFT",
                  "PP.7515.PP . BVG, Ihr Einkauf bei BVG ",
                  "LU96ZZZ0000000000000000058",
                  "5LFJ224TP5YA4",
                  "", "", "", "",
                  "PayPal (Europe) S.a.r.l. et Cie., S.C.A.",
                  "DE88500700100175526303",
                  "DEUTDEFFXXX",
                  new Money(-2.80),
                  "Umsatz gebucht")));
      entries.add(new CamtEntry(
          simpleDateFormat.parse("24.09.19"),
          new DateDataPoint(
              "DE04100500006013719070",
              "30.09.19",
              "FOLGELASTSCHRIFT",
              "PP.7515.PP . FLIXBUS, Ihr Einkauf bei FLIXBUS ",
              "LU96ZZZ0000000000000000058",
              "5LFJ224TP5YA4",
              "", "", "", "",
              "PayPal (Europe) S.a.r.l. et Cie., S.C.A.",
              "DE88500700100175526303",
              "DEUTDEFFXXX",
              new Money(-8.14),
              "Umsatz gebucht")));
      entries.add(new CamtEntry(
          simpleDateFormat.parse("23.09.19"),
          new DateDataPoint(
              "DE04100500006013719070",
              "26.09.19",
              "LOHN  GEHALT",
              "Verdienstabrechnung 09.19/1 ",
              "", "", "", "", "", "",
              "HELMHOLTZ-ZENTRUM DRESDEN-ROSSENDORF E.V.",
              "DE42850800000402657300",
              "DRESDEFF850",
              new Money(850.00),
              "Umsatz gebucht")));
    } catch (ParseException e) {
      e.printStackTrace();
    }
    /*
    23.09 - -3.10
    25.09 - -12.10
    26.09 - 0.11
     */

    return new Camt(entries);
  }


  public static Profile getProfile(){
    Profile profile = new Profile();
    try {
      PieCategory categoryEating = new PieCategory("Essen");
      ListProperty<Rule> pieEntries = new SimpleListProperty<>(
          FXCollections.observableList(new ArrayList<>()));

      pieEntries.add(Rule.RuleFactory.generate(
          new Pair<>(ListType.OTHER_PARTY, "NETTO MARKEN-DISCOU//DRESDEN-FRIEDRICHS/DE"), ""));

      pieEntries.add(Rule.RuleFactory.generate(
          new Pair<>(ListType.OTHER_PARTY, "DANKE, IHR LIDL//Dresden/DE"), ""));

      categoryEating.getRulesRO().addAll(pieEntries);
      profile.addCategory(categoryEating);

      PieCategory categoryLiving = new PieCategory("Leben");
      ListProperty<Rule> setLiving = new SimpleListProperty<>(
          FXCollections.observableList(new ArrayList<>()));
      setLiving.add(Rule.RuleFactory.generate(
          new Pair<>(ListType.IBAN, "DE56800400000850447400"), ""));

      setLiving.add(Rule.RuleFactory.generate(
          new Pair<>(ListType.IBAN, "DE15200411550651304800"), ""));

      categoryLiving.getRulesRO().addAll(setLiving);
      profile.addCategory(categoryLiving);

      PieCategory categoryTravel = new PieCategory("Reise");
      ListProperty<Rule> setTravel = new SimpleListProperty<>(
          FXCollections.observableList(new ArrayList<>()));

      setTravel.add(Rule.RuleFactory.generate(
          new Pair<>(ListType.USAGE, "PP.7515.PP . FLIXBUS, Ihr Einkauf bei FLIXBUS "), ""));
      categoryTravel.getRulesRO().addAll(setTravel);
      profile.addCategory(categoryTravel);

      PieCategory categoryEmpty = new PieCategory("Empty");
      profile.addCategory(categoryEmpty);

    }catch (Exception e){
      e.printStackTrace();
    }

    return profile;
  }
}
