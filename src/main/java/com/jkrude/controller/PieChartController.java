package com.jkrude.controller;

import com.jkrude.material.AlertBox;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.PieChart.Data;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;

public class PieChartController extends AbstractController {

  public Button categoryButton;
  private boolean populatedChart = false;

  @FXML
  public PieChart pieChart;
  @FXML
  public Button backButton;

  @FXML
  public void initialize() {
    backButton.setOnAction(AbstractController::goBack);

    try {
      ObservableList<Data> data = model.getCamtList().get(0)
          .getPieChartData(model.getProfile().getPieCategories());
      // Colors are only displayed for positive values
      data.forEach(d -> d.setPieValue(Math.abs(d.getPieValue())));
      pieChart.getData().addAll(data);
      populatedChart = true;
    } catch (NullPointerException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
      AlertBox.showAlert("Fatal Error", "Internal Error", "", AlertType.ERROR);
    }
  }

  protected void checkIntegrity() {
    if (!populatedChart) {
      initialize();
      if (!populatedChart) {
        throw new IllegalStateException("Chart could not get populated");
      }
    }
  }

  public void goToCategories(ActionEvent event) {
    AbstractController.goTo("categoryEditor", event);
  }
}
