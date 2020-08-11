package com.jkrude.controller;

import com.jkrude.main.Main;
import com.jkrude.main.Main.UsableScene;
import com.jkrude.material.AlertBox;
import com.jkrude.material.Camt;
import com.jkrude.material.Camt.Transaction;
import com.jkrude.material.Model;
import com.jkrude.material.Money;
import com.jkrude.material.PieCategory;
import com.jkrude.material.Rule;
import com.jkrude.material.UI.SourceChooseDialog;
import com.jkrude.material.UI.TransactionTableDialog;
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
import javafx.util.Pair;

public class PieChartController extends Controller {

  private boolean populatedChart = false;
  private boolean isInvalidated = false;
  // Marks if chart is up to date with Model.getInstance()
  // Saves witch CamtEntries where found for a category
  private Map<String, ObservableList<Transaction>> negEntryLookup;
  private Map<String, ObservableList<Transaction>> posEntryLookup;
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
  public void prepare() {
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

    backBtn.setOnAction(e -> Main.goBack());
    negPosTglBtn.selectedProperty().addListener(
        (observableValue, oldV, newV) -> {
          changeChartData(newV);
        });
    Model.getInstance().getProfile().addListener(change -> isInvalidated = true);
  }

  private void setupChart() {
    pieChart.getData().clear();
    if (this.pureCAMT == null) {
      if (Model.getInstance().getCamtList() == null || Model.getInstance().getCamtList()
          .isEmpty()) {
        throw new IllegalStateException("PieChart was called but no data is available");
      } else if (Model.getInstance().getCamtList().size() > 1) {
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
            , Model.getInstance().getCamtList());
      } else {
        pureCAMT = Model.getInstance().getCamtList().get(0);
      }
    }
    ObservableList<Transaction> source = this.pureCAMT.getSource();
    ObservableList<PieCategory> categories = Model.getInstance().getProfile().getPieCategories();

    // Populate the chart with data
    List<Pair<List<Rule>, Transaction>> multipleMatches = matchDataToRules(
        source,
        categories);
    //TODO Show transaction and matched Rules
    AlertBox.showAlert("Hinweis", "Mehrere passende Regeln",
        "Für einige Überweisungen haben mehrere Regeln gepasst", AlertType.WARNING);
    pieChart.getData().addAll(negChartData);
    setupToolTip(pieChart.getData());
    setupPopUp(pieChart.getData());
    populatedChart = true;
    isInvalidated = false;
  }

  @FXML
  private void changeDataSource() {
    if (Model.getInstance().getCamtList().isEmpty()) {
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
            }
            // else: user clicked cancel or x
          },
          Model.getInstance().getCamtList()
      );
    }

  }

  private List<Pair<List<Rule>, Transaction>> matchDataToRules(
      ObservableList<Transaction> source,
      ObservableList<PieCategory> categories
  ) {
    // Check for every rule for every category for every Transaction(transactions) if the predicate tests positive
    // When a Transaction tests positive but the amount is positive it gets marked in the ignoredPositiveEntries

    // For every category add the amount for matching CamtEntries
    HashMap<StringProperty, Money> negEntriesForCategories = new HashMap<>();
    HashMap<StringProperty, Money> posEntriesForCategories = new HashMap<>();

    // Collect all entries with no matching rule
    List<Transaction> negTransactionsWithoutRule = new ArrayList<>();
    List<Transaction> posTransactionsWithoutRule = new ArrayList<>();

    List<Pair<List<Rule>, Transaction>> multipleMatchingRules = new ArrayList<>();

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

    for (final Transaction camtTransaction : source) {
      // An entry can only belong to one category (one rule)
      // If multiple rules apply the transaction will all the rules will be flagged
      Rule firstMatchedRule = null;
      int matchingRulesCounter = 0;

      for (final PieCategory category : categories) {
        for (final Rule rule : category.getRulesRO()) { //RO = ReadOnly
          if (rule.getPredicate().test(camtTransaction)) {
            // If rule applies to data: check if another rule did so too
            if (matchingRulesCounter > 0) {
              if (matchingRulesCounter == 1) {
                multipleMatchingRules
                    .add(new Pair<>(List.of(rule, firstMatchedRule), camtTransaction));
              } else {
                multipleMatchingRules.get(multipleMatchingRules.size() - 1).getKey().add(rule);
              }
              // else add it to category (in positive or negative chart)
            } else {
              if (camtTransaction.getAmount().getAmount().compareTo(BigDecimal.ZERO)
                  < 0) {
                negEntriesForCategories.get(category.getName())
                    .add(camtTransaction.getAmount());
                negEntryLookup.get(category.getName().get()).add(camtTransaction);
              } else {
                posEntriesForCategories.get(category.getName())
                    .add(camtTransaction.getAmount());
                posEntryLookup.get(category.getName().get()).add(camtTransaction);
              }
            }
            firstMatchedRule = rule;
            matchingRulesCounter++;
          }
        }
      }
      // All categories have been checked -> If no rule applied add it to unspecified slice
      if (matchingRulesCounter == 0) {
        if (camtTransaction.getAmount().getAmount().compareTo(BigDecimal.ZERO) < 0) {
          negTransactionsWithoutRule.add(camtTransaction);
        } else {
          posTransactionsWithoutRule.add(camtTransaction);
        }
      }
    }

    // Colors are only displayed for positive values
    negEntriesForCategories.forEach(
        (key, value) -> negChartData
            .add(new Data(key.get(), Math.abs(value.getAmount().doubleValue()))));
    // Handle all the transactions where no rule matched
    negEntryLookup
        .put(catNameUnmatchedTrans,
            FXCollections.observableArrayList(negTransactionsWithoutRule));
    negChartData.add(
        new Data(catNameUnmatchedTrans,
            Money.sum(negTransactionsWithoutRule).getAmount().doubleValue()));
    // Do the same for the chart with positive values
    posEntriesForCategories.forEach(
        (key, value) -> posChartData
            .add(new Data(key.get(), value.getAmount().doubleValue())));
    posEntryLookup
        .put(catNameUnmatchedTrans,
            FXCollections.observableArrayList(posTransactionsWithoutRule));
    posChartData.add(
        new Data(catNameUnmatchedTrans,
            Money.sum(posTransactionsWithoutRule).getAmount().doubleValue()));
    return multipleMatchingRules;
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
            ObservableList<Transaction> tableData;
            if (negPosTglBtn.isSelected()) {
              tableData = posEntryLookup.get(data.getName());
            } else {
              tableData = negEntryLookup.get(data.getName());
            }
            TransactionTableDialog.Builder.init(tableData)
                .setContextMenu(Model.getInstance().getProfile().getPieCategories())
                .showAndWait();
          }
      );
    }
  }

  public void goToCategories(ActionEvent event) {
    Main.goTo(UsableScene.CATEGORY_EDITOR);
  }

  /*
  Getter
   */
  public PieChart getPieChart() {
    return pieChart;
  }
}
