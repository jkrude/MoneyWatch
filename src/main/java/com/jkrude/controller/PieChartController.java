package com.jkrude.controller;

import com.jkrude.material.AlertBox;
import com.jkrude.material.Camt;
import com.jkrude.material.Camt.CamtEntry;
import com.jkrude.material.Money;
import com.jkrude.material.PieCategory;
import com.jkrude.material.Rule;
import com.jkrude.material.UI.CamtEntryTablePopUpBuilder;
import com.jkrude.material.UI.SourceChooseDialog;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.PieChart.Data;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

public class PieChartController extends ParentController {

  private boolean populatedChart = false;
  private boolean isInvalidated = false;
  // Marks if chart is up to date with model
  // Saves witch CamtEntries where found for a category
  private Map<String, ObservableList<CamtEntry>> negEntryLookup;
  private Map<String, ObservableList<CamtEntry>> posEntryLookup;
  // The default name for the slice for transactions without matching rule
  public static final String catNameUnmatchedTrans = "Undefiniert";

  private ObservableList<PieChart.Data> posChartData;
  private ObservableList<PieChart.Data> negChartData;


  @FXML
  private PieChart pieChart;
  @FXML
  private Button backBtn;
  @FXML
  private ToggleButton negPosTglBtn;

  Camt pureCAMT;

  @Override
  protected void checkIntegrity() {
    if (isInvalidated || !populatedChart) {
      setupChart();
    }
  }

  @FXML
  public void initialize() {
    negEntryLookup = new HashMap<>();
    posEntryLookup = new HashMap<>();
    negChartData = FXCollections.observableArrayList();
    posChartData = FXCollections.observableArrayList();
    pureCAMT = null;

    backBtn.setOnAction(ParentController::goBack);
    negPosTglBtn.selectedProperty().addListener(
        (observableValue, oldV, newV) -> {
          changeChartData(newV);
        });
    model.getProfile().addListener(change -> isInvalidated = true);
  }

  private void setupChart() {
    pieChart.getData().clear();
    if (this.pureCAMT == null) {
      if (model.getCamtList() == null || model.getCamtList().isEmpty()) {
        AlertBox.showAlert("Daten benötigt", "Keine CSV Dateien geladen",
            "Wähle im Hauptmenü: Open File",
            AlertType.ERROR);
        // TODO
        return;
      } else if (model.getCamtList().size() > 1) {
        SourceChooseDialog.show(
            camt -> {
              if (camt != null) {
                pureCAMT = camt;
              } else {
                AlertBox.showAlert("Fehlender Datensatz", "Bitte wähle im Dialog einen Datensatz",
                    "Möglich ist dies über Klicken + 'OK' oder Doppelklick", AlertType.ERROR);
                //TODO
              }
            }
            , model.getCamtList());
      } else {
        pureCAMT = model.getCamtList().get(0);
      }
    }
    ObservableList<CamtEntry> source = this.pureCAMT.getSource();
    ObservableList<PieCategory> categories = model.getProfile().getPieCategories();

    // Populate the chart with data
    fillListWithGenChartData(
        source,
        categories);
    pieChart.getData().addAll(negChartData);
    setupToolTip(pieChart.getData());
    setupPopUp(pieChart.getData());
    populatedChart = true;
    isInvalidated = false;
  }

  @FXML
  private void changeDataSource() {
    if (model.getCamtList().isEmpty()) {
      AlertBox.showAlert("Kein Auswahl möglich", "Keine CSV-Datein geladen", "", AlertType.ERROR);
    } else {
      SourceChooseDialog.show(
          camt -> {
            if (camt != null) {
              if (camt.equals(pureCAMT)) {
                return;
              }
              pureCAMT = camt;
              setupChart();
            } else {
              AlertBox.showAlert("Fehlender Datensatz", "Bitte wähle im Dialog einen Datensatz",
                  "Möglich ist dies über Klicken + 'OK' oder Doppelklick", AlertType.ERROR);
            }
          },
          model.getCamtList()
      );
    }

  }

  private void fillListWithGenChartData(
      ObservableList<CamtEntry> source,
      ObservableList<PieCategory> categories
  ) {
    // Check for every rule for every category for every CamtEntry(transactions) if the predicate tests positive
    // When a CamtEntry tests positive but the amount is positive it gets marked in the ignoredPositiveEntries

    // For every category add the amount for matching CamtEntries
    HashMap<StringProperty, Money> negEntriesForCategories = new HashMap<>();
    HashMap<StringProperty, Money> posEntriesForCategories = new HashMap<>();

    // Collect all entries with no matching rule
    List<CamtEntry> negTransactionsWithoutRule = new ArrayList<>();
    List<CamtEntry> posTransactionsWithoutRule = new ArrayList<>();

    negEntryLookup.clear();
    posEntryLookup.clear();
    negChartData.clear();
    posChartData.clear();

    // Setup maps with lists for simpler traversing
    categories.forEach(pieCategory -> {
      negEntriesForCategories.put(pieCategory.getName(), new Money(0));
      negEntryLookup.put(pieCategory.getName().get(), FXCollections.observableArrayList());
      posEntriesForCategories.put(pieCategory.getName(), new Money(0));
      posEntryLookup.put(pieCategory.getName().get(), FXCollections.observableArrayList());
    });

    for (final CamtEntry camtEntry : source) {
      // An entry can only belong to one category (one rule)
      boolean foundMatchingRule = false;
      for (final PieCategory category : categories) {
        if (foundMatchingRule) {
          break;
        }
        for (final Rule rule : category.getRulesRO()) {
          if (foundMatchingRule) {
            break;
          }
          if (rule.getPredicate().test(camtEntry)) {
            if (camtEntry.getDataPoint().getAmount().getAmount().compareTo(BigDecimal.ZERO) < 0) {
              negEntriesForCategories.get(category.getName())
                  .add(camtEntry.getDataPoint().getAmount());
              negEntryLookup.get(category.getName().get()).add(camtEntry);
            } else {
              posEntriesForCategories.get(category.getName())
                  .add(camtEntry.getDataPoint().getAmount());
              posEntryLookup.get(category.getName().get()).add(camtEntry);
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
    negEntriesForCategories.forEach(
        (key, value) -> negChartData
            .add(new Data(key.get(), Math.abs(value.getAmount().doubleValue()))));
    // Handle all the transactions where no rule matched
    negEntryLookup
        .put(catNameUnmatchedTrans, FXCollections.observableArrayList(negTransactionsWithoutRule));
    negChartData.add(
        new Data(catNameUnmatchedTrans,
            Money.sum(negTransactionsWithoutRule).getAmount().doubleValue()));
    // Do the same for the chart with positive values
    posEntriesForCategories.forEach(
        (key, value) -> posChartData
            .add(new Data(key.get(), value.getAmount().doubleValue())));
    posEntryLookup
        .put(catNameUnmatchedTrans, FXCollections.observableArrayList(posTransactionsWithoutRule));
    posChartData.add(
        new Data(catNameUnmatchedTrans,
            Money.sum(posTransactionsWithoutRule).getAmount().doubleValue()));

  }

  private void changeChartData(boolean newValue) {
    pieChart.getData().clear();
    if (newValue) {
      pieChart.getData().addAll(posChartData);
    } else {
      pieChart.getData().addAll(negChartData);
    }
    setupToolTip(pieChart.getData());
    setupPopUp(pieChart.getData());

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
          mouseEvent -> {
            if (negPosTglBtn.isSelected()) {
              CamtEntryTablePopUpBuilder.build(posEntryLookup.get(data.getName()))
                  .showAndWait();
            } else {
              CamtEntryTablePopUpBuilder.build(negEntryLookup.get(data.getName()))
                  .showAndWait();
            }
          }
      );
    }
  }

  public void goToCategories(ActionEvent event) {
    ParentController.goTo(ParentController.categoryEditor, event);
  }

  /*
  Getter
   */
  public PieChart getPieChart() {
    return pieChart;
  }
}
