package com.jkrude.controller;

import com.jkrude.test.TestData;
import java.util.ArrayList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.PieChart.Data;

public class PieChartController extends AbstractController {

  public PieChart pieChart;

  @FXML
  public void initialize() {
    ArrayList<Data> data = model.getCamtList().get(0)
        .getPieChartData(TestData.getProfile().getPieCategories());
    // Colors are only displayed for positive values
    data.forEach( d -> d.setPieValue(Math.abs(d.getPieValue())));

    pieChart.getData().addAll(data);
  }
}
