package com.jkrude.test;

import com.jkrude.material.Camt;
import com.jkrude.material.Money;
import com.jkrude.material.PieCategory;
import com.jkrude.material.PieCategory.Entry;
import com.jkrude.material.Profile;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

public class TestData {

  public static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yy");


  public static Camt getCamtWithTestData() {
    List<Money> moneyList = new ArrayList<>();
    moneyList.add(new Money(12.21));
    moneyList.add(new Money(1));
    moneyList.add(new Money(11));
    moneyList.add(new Money(-21));
    moneyList.add(new Money(-3.1));
    //camt.getAmount().addAll(moneyList);
    List<Date> dateList = new ArrayList<>();
    try {
      dateList.add(simpleDateFormat.parse("26.09.19"));
      dateList.add(simpleDateFormat.parse("25.09.19"));
      dateList.add(simpleDateFormat.parse("25.09.19"));
      dateList.add(simpleDateFormat.parse("24.09.19"));
      dateList.add(simpleDateFormat.parse("23.09.19"));
    } catch (ParseException e) {
      e.printStackTrace();
    }

    List<String> contractAccount = new ArrayList<>();
    contractAccount.add("DE04100500006013719070");
    contractAccount.add("DE04100500006013719070");
    contractAccount.add("DE04100500006013719070");
    contractAccount.add("DE04100500006013719070");
    contractAccount.add("DE04100500006013719070");
    List<String> transferValidation = new ArrayList<>();
    transferValidation.add("01.10.19");
    transferValidation.add("30.09.19");
    transferValidation.add("30.09.19");
    transferValidation.add("30.09.19");
    transferValidation.add("26.09.19");
    List<String> transferSpecification = new ArrayList<>();
    transferSpecification.add("GUTSCHR. UEBERW. DAUERAUFTR");
    transferSpecification.add("KARTENZAHLUNG");
    transferSpecification.add("FOLGELASTSCHRIFT");
    transferSpecification.add("FOLGELASTSCHRIFT");
    transferSpecification.add("LOHN  GEHALT");
    List<String> usage = new ArrayList<>();
    usage.add("Monatlicher Unterhalt ab November 2018 ");
    usage.add("2019-09-27T10:36 Debitk.1 2022-12 ");
    usage.add("PP.7515.PP . BVG, Ihr Einkauf bei BVG ");
    usage.add("PP.7515.PP . FLIXBUS, Ihr Einkauf bei FLIXBUS ");
    usage.add("Verdienstabrechnung 09.19/1 ");
    List<String> creditorId = new ArrayList<>();
    creditorId.add("");
    creditorId.add("");
    creditorId.add("LU96ZZZ0000000000000000058");
    creditorId.add("LU96ZZZ0000000000000000058");
    creditorId.add("");
    List<String> mandateReference = new ArrayList<>();
    mandateReference.add("");
    mandateReference.add("");
    mandateReference.add("5LFJ224TP5YA4");
    mandateReference.add("5LFJ224TP5YA4");
    mandateReference.add("");
    List<String> customerReference = new ArrayList<>();
    customerReference.add("");
    customerReference.add("");
    customerReference.add("");
    customerReference.add("");
    customerReference.add("");
    List<String> collectorReference = new ArrayList<>();
    collectorReference.add("");
    collectorReference.add("");
    collectorReference.add("");
    collectorReference.add("");
    collectorReference.add("");
    List<String> debitOriginalAmount = new ArrayList<>();
    debitOriginalAmount.add("");
    debitOriginalAmount.add("");
    debitOriginalAmount.add("");
    debitOriginalAmount.add("");
    debitOriginalAmount.add("");
    List<String> backDebit = new ArrayList<>();
    backDebit.add("");
    backDebit.add("");
    backDebit.add("");
    backDebit.add("");
    backDebit.add("");
    List<String> receiverOrPayer = new ArrayList<>();
    receiverOrPayer.add("Dr. Heiko Krude");
    receiverOrPayer.add("Wiener Feinbaecker//Berlin/DE");
    receiverOrPayer.add("PayPal (Europe) S.a.r.l. et Cie., S.C.A.");
    receiverOrPayer.add("PayPal (Europe) S.a.r.l. et Cie., S.C.A.");
    receiverOrPayer.add("HELMHOLTZ-ZENTRUM DRESDEN-ROSSENDORF E.V.");
    List<String> iban = new ArrayList<>();
    iban.add("DE21100500001062583945");
    iban.add("DE85120800000101752405");
    iban.add("DE88500700100175526303");
    iban.add("DE88500700100175526303");
    iban.add("DE42850800000402657300");
    List<String> bic = new ArrayList<>();
    bic.add("BELADEBEXXX");
    bic.add("DRESDEFF120");
    bic.add("DEUTDEFFXXX");
    bic.add("DEUTDEFFXXX");
    bic.add("DRESDEFF850");

    List<String> info = new ArrayList<>();
    info.add("Umsatz gebucht");
    info.add("Umsatz gebucht");
    info.add("Umsatz gebucht");
    info.add("Umsatz gebucht");
    info.add("Umsatz gebucht");

    /*
    23.09 - -3.10
    25.09 - -12.10
    26.09 - 0.11
     */
    //camt.getTransferDate().addAll(dateList);

    return new Camt(contractAccount, dateList, transferValidation, transferSpecification, usage,
        creditorId, mandateReference, customerReference, collectorReference,
        debitOriginalAmount, backDebit, receiverOrPayer, iban, bic, moneyList, info);
  }

  public static Camt reverseData(Camt camt) {
    //reverse
    Collections.reverse(camt.getAmount());
    Collections.reverse(camt.getTransferDate());
    return camt;
  }

  public static Profile getProfile() {
    Profile profile = new Profile();
    PieCategory categoryEating = new PieCategory("Essen");
    ListProperty<Entry> pieEntries = new SimpleListProperty<>(FXCollections.observableList(new ArrayList<>()));
    pieEntries.add(new PieCategory.Entry("NETTO MARKEN-DISCOU//DRESDEN-FRIEDRICHS/DE",
        Camt.ListType.OTHER_PARTY));
    pieEntries.add(new PieCategory.Entry("DANKE, IHR LIDL//Dresden/DE", Camt.ListType.OTHER_PARTY));

    categoryEating.getIdentifierList().addAll(pieEntries);
    profile.addCategory(categoryEating);

    PieCategory categoryLiving = new PieCategory("Living");
    ListProperty<PieCategory.Entry> setLiving = new SimpleListProperty<>(FXCollections.observableList(new ArrayList<>()));
    setLiving.add(new PieCategory.Entry("DE56800400000850447400", Camt.ListType.IBAN));
    setLiving.add(new PieCategory.Entry("DE15200411550651304800", Camt.ListType.IBAN));

    categoryLiving.getIdentifierList().addAll(setLiving);
    profile.addCategory(categoryLiving);

    PieCategory categoryTravel = new PieCategory("Travel");
    ListProperty<PieCategory.Entry> setTravel = new SimpleListProperty<>(FXCollections.observableList(new ArrayList<>()));
    setTravel.add(new PieCategory.Entry("PP.7515.PP . FLIXBUS, Ihr Einkauf bei FLIXBUS ",
        Camt.ListType.USAGE));

    categoryTravel.getIdentifierList().addAll(setTravel);
    profile.addCategory(categoryTravel);

    return profile;
  }
}
