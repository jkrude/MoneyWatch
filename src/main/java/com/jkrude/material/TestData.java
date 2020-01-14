package com.jkrude.material;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
      dateList.add(simpleDateFormat.parse("26.09"));
      dateList.add(simpleDateFormat.parse("25.09"));
      dateList.add(simpleDateFormat.parse("25.09"));
      dateList.add(simpleDateFormat.parse("25.09"));
      dateList.add(simpleDateFormat.parse("23.09"));
    } catch (ParseException e) {
      e.printStackTrace();
    }

    camt.getTransferDate().addAll(dateList);

    lineChartData = camt.getLineChartData();
  }
}
