package com.jkrude.material;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javafx.scene.chart.XYChart.Data;

public class TestData {

  private static List<Data<Number, Number>> lineChartData;

  public static List<Data<Number, Number>> getLineChartData(){
    if (lineChartData==null){
      generateData();
    }
    return lineChartData;
  }

  public static void generateData(){
    Camt camt = new Camt();
    List<Money> moneyList = new ArrayList<>();
    moneyList.add(new Money(12.21));
    moneyList.add(new Money(1));
    moneyList.add(new Money(11));
    moneyList.add(new Money(-21));
    moneyList.add(new Money(-3.1));
    camt.getAmount().addAll(moneyList);
    List<Date> dateList = new ArrayList<>();
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yy");

    try {
      dateList.add(simpleDateFormat.parse("26.09.19"));
      dateList.add(simpleDateFormat.parse("25.09.19"));
      dateList.add(simpleDateFormat.parse("25.09.19"));
      dateList.add(simpleDateFormat.parse("25.09.19"));
      dateList.add(simpleDateFormat.parse("23.09.19"));
    } catch (ParseException e) {
      e.printStackTrace();
    }
    /*
    23.09 - -3.10
    25.09 - -12.10
    26.09 - 0.11
     */
    camt.getTransferDate().addAll(dateList);
    camt.firstEntryIsFirstDate();

    //reverse
    Collections.reverse(camt.getAmount());
    Collections.reverse(camt.getTransferDate());

    camt.firstEntryIsFirstDate();

    lineChartData = camt.getLineChartData();
  }
}
