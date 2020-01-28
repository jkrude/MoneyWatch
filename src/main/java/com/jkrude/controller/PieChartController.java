package com.jkrude.controller;

import com.jkrude.test.TestData;
import java.util.ArrayList;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.PieChart.Data;

public class PieChartController extends AbstractController {

  public PieChart pieChart;

  @FXML
  public void initialize() {
    ArrayList<Data> data = model.getCamtList().get(0)
        .getPieChartData(TestData.getProfile().getPieCategories());
    pieChart.getData().addAll(data);
    pieChart.setLegendSide(Side.BOTTOM);
    /*
  Get Profile
  Get Categories
  for(i = 0; i < camt.size)
    for(Category c : Categories)
      if(camt.iban[i] i
   */
  }
}
