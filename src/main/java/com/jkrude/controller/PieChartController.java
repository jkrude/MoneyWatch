package com.jkrude.controller;

import com.jkrude.material.AlertBox;
import com.jkrude.material.Camt.CamtEntry;
import com.jkrude.material.Money;
import com.jkrude.material.PieCategory;
import com.jkrude.material.Rule;
import com.jkrude.material.UI.TableControllerManager;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.PieChart.Data;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

public class PieChartController extends ParentController {

  private boolean populatedChart = false;
  //Marks if chart is up to date with model
  private boolean dirtyFlag = false;
  // Saves witch CamtEntries where found for a category
  private Map<String, ObservableList<CamtEntry>> chartDataMap;

  public static final String catNameUnmatchedTrans = "Undefiniert";

  @FXML
  private PieChart pieChart;
  @FXML
  private Button backButton;

  @Override
  protected void checkIntegrity() {
    if (dirtyFlag || !populatedChart) {
      setupChart();
    }
  }

  @FXML
  public void initialize() {
    chartDataMap = new HashMap<String, ObservableList<CamtEntry>>();
    backButton.setOnAction(ParentController::goBack);

    // Setup change-listener for data-invalidation
    model.getProfile().getPieCategories().addListener(
        (ListChangeListener<PieCategory>) change -> {
          dirtyFlag = true;
          if (change.next() && !change.getAddedSubList().isEmpty()) {
            change.getAddedSubList().forEach(
                item -> item.getIdentifierList().addListener(
                    (ListChangeListener<? super Rule>) change1 -> dirtyFlag = true
                )
            );
          }
        }
    );
    model.getProfile().getPieCategories().forEach(
        pieCategory -> {
          pieCategory.getIdentifierList().addListener(
              (ListChangeListener<? super Rule>) change -> dirtyFlag = true
          );
          pieCategory.getName().addListener(
              (observableValue, s, t1) -> dirtyFlag = true);
        }
    );
  }

  private void setupChart() {
    pieChart.getData().clear();
    ObservableList<CamtEntry> source = model.getCamtList().get(0).getSource();
    ObservableList<PieCategory> categories = model.getProfile().getPieCategories();
    Map<String, List<CamtEntry>> ignoredPosEntries = new HashMap<>();

    // Populate the chart with data
    fillListWithGenChartData(
        source,
        categories,
        ignoredPosEntries,
        pieChart.getData());
    // Give feedback for possible unintended behaviour
    if (!ignoredPosEntries.isEmpty()) {
      AlertBox.showAlert("Ignorierte gefundene Überweisungen",
          "Manche Überweisungen werden nicht in der Grafik genutzt",
          "Im Diagramm werden nur die Ausgaben betrachtet. Einige Regeln konnten allerdings auch auf Eingaben angewendet werden",
          AlertType.WARNING);
    }

    setupToolTip(pieChart.getData());
    setupPopUp(pieChart.getData());
    populatedChart = true;
    dirtyFlag = false;
  }

  private void fillListWithGenChartData(
      ObservableList<CamtEntry> source,
      ObservableList<PieCategory> categories,
      Map<String, List<CamtEntry>> ignoredPositiveEntries,
      ObservableList<PieChart.Data> pieChartData
  ) {
    // Check for every rule for every category for every CamtEntry(transactions) if the predicate tests positive
    // When a CamtEntry tests positive but the amount is positive it gets marked in the ignoredPositiveEntries

    // For every category add the amount for matching CamtEntries
    HashMap<StringProperty, Money> categoryHashMap = new HashMap<>();
    // Collect all entries with no matching rule
    List<CamtEntry> negTransactionsWithoutRule = new ArrayList<>();
    List<CamtEntry> posTransactionsWithoutRule = new ArrayList<>();

    chartDataMap.clear();
    // Setup maps with lists for simpler traversing
    categories.forEach(pieCategory -> {
      categoryHashMap.put(pieCategory.getName(), new Money(0));
      chartDataMap.put(pieCategory.getName().get(), FXCollections.observableArrayList());
    });

    for (final CamtEntry camtEntry : source) {
      // An entry can only belong to one category (one rule)
      boolean foundMatchingRule = false;
      for (final PieCategory category : categories) {
        if (foundMatchingRule) {
          break;
        }
        for (final Rule rule : category.getIdentifierList()) {
          if (foundMatchingRule) {
            break;
          }
          if (rule.getPredicate().test(camtEntry)) {
            if (camtEntry.getDataPoint().getAmount().getAmount().compareTo(BigDecimal.ZERO) < 0) {
              categoryHashMap.get(category.getName()).add(camtEntry.getDataPoint().getAmount());
              chartDataMap.get(category.getName().get()).add(camtEntry);
            } else {
              if (ignoredPositiveEntries.containsKey(category.getName().get())) {
                ignoredPositiveEntries.get(category.getName().get()).add(camtEntry);
              } else {
                List<CamtEntry> list = new ArrayList<>();
                list.add(camtEntry);
                ignoredPositiveEntries.put(category.getName().get(), list);
              }
            }
            foundMatchingRule = true;
          }
        }
      }
      if (!foundMatchingRule) {
        if (camtEntry.getDataPoint().getAmount().getAmount().compareTo(BigDecimal.ZERO) < 0) {
          negTransactionsWithoutRule.add(camtEntry);
        } else {
          posTransactionsWithoutRule.add(camtEntry);
        }
      }
    }

    // Colors are only displayed for positive values
    categoryHashMap.forEach(
        (key, value) -> pieChartData
            .add(new Data(key.get(), Math.abs(value.getAmount().doubleValue()))));
    // Handle all the transactions where no rule matched
    chartDataMap
        .put(catNameUnmatchedTrans, FXCollections.observableArrayList(negTransactionsWithoutRule));
    pieChartData.add(
        new Data(catNameUnmatchedTrans,
            Money.sum(negTransactionsWithoutRule).getAmount().doubleValue()));
  }

  private void setupToolTip(final ObservableList<PieChart.Data> chartData) {
    // Display the amount for the data-point
    for (final PieChart.Data data : chartData) {
      String displayingText = String.valueOf(data.getPieValue()) + '€';
      Tooltip tlp = new Tooltip(displayingText);
      tlp.setShowDelay(new Duration(100));
      Tooltip.install(data.getNode(), tlp);
    }
  }

  private void setupPopUp(final ObservableList<PieChart.Data> chartData) {
    // PopUp for every data-point to display transactions for this day
    // Uses the prebuild table fxml
    for (final PieChart.Data data : chartData) {
      data.getNode().setOnMouseClicked(
          mouseEvent -> TableControllerManager.showAsTablePopUp(chartDataMap.get(data.getName()))
      );
    }
  }

  public void goToCategories(ActionEvent event) {
    ParentController.goTo("categoryEditor", event);
  }

  /*
  Getter
   */
  public PieChart getPieChart() {
    return pieChart;
  }
}
