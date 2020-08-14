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
import com.jkrude.material.UI.TransactionTablePopUp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
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

public class PieChartController extends Controller {

  private boolean populatedChart = false;
  private boolean isInvalidated = false;
  // Marks if chart is up to date with Model.getInstance()
  // Saves witch CamtEntries where found for a category
  private Map<String, ObservableList<Transaction>> negEntryLookup;
  private Map<String, ObservableList<Transaction>> posEntryLookup;
  // The default name for the slice for transactions without matching rule
  public static final String UNMATCHED_TRANSACTIONS = "Undefiniert";

  private ObservableList<PieChart.Data> posChartData;
  private ObservableList<PieChart.Data> negChartData;


  @FXML
  private PieChart pieChart;
  @FXML
  private Button backBtn;
  @FXML
  private ToggleButton negPosTglBtn;

  Camt camtData;

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
    camtData = null;

    backBtn.setOnAction(e -> Main.goBack());
    negPosTglBtn.selectedProperty().addListener(
        (observableValue, oldV, newV) -> changeChartData(newV)); // Use pos or neg dataSet
    Model.getInstance().getProfile()
        .addListener(change -> isInvalidated = true); // Needs to be live updated
  }

  private void setupChart() {
    pieChart.getData().clear();
    if (camtData == null) {
      if (Model.getInstance().getCamtList() == null
          || Model.getInstance().getCamtList().isEmpty()) {

        throw new IllegalStateException("PieChart was called but no data is available");
      } else if (Model.getInstance().getCamtList().size() > 1) {
        showError();
      } else {
        camtData = Model.getInstance().getCamtList().get(0);
      }
    }

    ObservableList<Transaction> source = camtData.getSource();
    ObservableList<PieCategory> categories = Model.getInstance().getProfile().getPieCategories();

    // Populate the chart with data
    Map<Transaction, Set<Rule>> multipleMatches = new HashMap<>();
    matchDataToRules(
        source,
        categories,
        multipleMatches);
    if (!multipleMatches.isEmpty()) {
      //TODO Show transaction and matched Rules
      AlertBox.showAlert("Hinweis", "Mehrere passende Regeln",
          "Für einige Überweisungen haben mehrere Regeln gepasst. Nur die erste wurde angewendet.",
          AlertType.WARNING);
    }
    // Default: Display all negative transactions.
    pieChart.getData().addAll(negChartData);
    addToolTipForData(pieChart.getData());
    addTableViewPopUpForData(pieChart.getData());
    populatedChart = true;
    isInvalidated = false;
  }

  private void showError() {
    SourceChooseDialog.show(
        camt -> {
          if (camt != null) {
            camtData = camt;
          } else {
            AlertBox.showAlert("Fehlender Datensatz", "Bitte wähle im Dialog einen Datensatz",
                "Möglich ist dies über Klicken + 'OK' oder Doppelklick", AlertType.ERROR);
            //TODO
          }
        }
        , Model.getInstance().getCamtList());
  }

  @FXML
  private void changeDataSource() {
    if (Model.getInstance().getCamtList().isEmpty()) {
      AlertBox.showAlert("Kein Auswahl möglich", "Keine CSV-Datein geladen", "", AlertType.ERROR);
    } else {
      SourceChooseDialog.show(
          camt -> {
            if (camt != null) {
              if (camt.equals(camtData)) {
                return;
              }
              camtData = camt;
              setupChart();
            }
            // else: user clicked cancel or x
          },
          Model.getInstance().getCamtList()
      );
    }

  }

  private void matchDataToRules(
      ObservableList<Transaction> source,
      ObservableList<PieCategory> categories,
      Map<Transaction, Set<Rule>> multipleMatches
  ) {
    // Check for every rule for every category for every Transaction(transactions) if the predicate tests positive
    // When a transaction tests positive but the amount is positive it gets marked in the ignoredPositiveEntries

    negEntryLookup.clear();
    posEntryLookup.clear();
    negChartData.clear();
    posChartData.clear();

    Map<String, Set<Transaction>> matchedTransactions = new HashMap<>();
    // Fill map with all categories and empty sets.
    categories
        .forEach(category -> matchedTransactions.put(category.getName().get(), new HashSet<>()));
    matchedTransactions.put(UNMATCHED_TRANSACTIONS, new HashSet<>());
    for (final Transaction transaction : source) {
      Set<Rule> matchedRules = new HashSet<>();

      for (final PieCategory category : categories) {

        for (final Rule rule : category.getRulesRO()) {
          if (rule.getPredicate().test(transaction)) {
            if (matchedRules.isEmpty()) {
              matchedTransactions.get(category.getName().get()).add(transaction);
            }
            matchedRules.add(rule);
          }
        }

      }

      multipleMatches.put(transaction, matchedRules);
      if (matchedRules.isEmpty()) {
        matchedTransactions.get(UNMATCHED_TRANSACTIONS).add(transaction);
      }
    }

    populateWithPredicate(matchedTransactions, posEntryLookup, posChartData,
        Transaction::isPositive);
    populateWithPredicate(matchedTransactions, negEntryLookup, negChartData,
        Predicate.not(Transaction::isPositive));
    // Colors are only displayed for positive values
    negChartData.forEach(data -> data.setPieValue(Math.abs(data.getPieValue())));
    /*for (final Transaction camtTransaction : source) {
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
              if (camtTransaction.getMoneyAmount().getRawAmount().compareTo(BigDecimal.ZERO)
                  < 0) {
                negEntriesForCategories.get(category.getName())
                    .add(camtTransaction.getMoneyAmount());
                negEntryLookup.get(category.getName().get()).add(camtTransaction);
              } else {
                posEntriesForCategories.get(category.getName())
                    .add(camtTransaction.getMoneyAmount());
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
        if (camtTransaction.getMoneyAmount().getRawAmount().compareTo(BigDecimal.ZERO) < 0) {
          negTransactionsWithoutRule.add(camtTransaction);
        } else {
          posTransactionsWithoutRule.add(camtTransaction);
        }
      }
    }

    // Colors are only displayed for positive values
    negEntriesForCategories.forEach(
        (key, value) -> negChartData
            .add(new Data(key.get(), Math.abs(value.getRawAmount().doubleValue()))));
    // Handle all the transactions where no rule matched
    negEntryLookup
        .put(UnmatchedTransactions,
            FXCollections.observableArrayList(negTransactionsWithoutRule));
    negChartData.add(
        new Data(UnmatchedTransactions,
            Money.sum(negTransactionsWithoutRule).getRawAmount().doubleValue()));
    // Do the same for the chart with positive values
    posEntriesForCategories.forEach(
        (key, value) -> posChartData
            .add(new Data(key.get(), value.getRawAmount().doubleValue())));
    posEntryLookup
        .put(UnmatchedTransactions,
            FXCollections.observableArrayList(posTransactionsWithoutRule));
    posChartData.add(
        new Data(UnmatchedTransactions,
            Money.sum(posTransactionsWithoutRule).getRawAmount().doubleValue()));
    return multipleMatchingRules;*/
  }

  private void populateWithPredicate(
      Map<String, Set<Transaction>> matchedTransactions,
      Map<String, ObservableList<Transaction>> lookupTable,
      ObservableList<PieChart.Data> chartData,
      Predicate<Transaction> predicate) {

    matchedTransactions.forEach(((category, transactions) ->
        lookupTable.put(category,
            transactions.stream()
                .filter(predicate)
                .collect(Collectors.toCollection(FXCollections::observableArrayList))
        )));

    lookupTable.forEach(
        (category, transactions) -> chartData
            .add(new Data(
                category,
                Money.sum(transactions).getRawAmount().doubleValue())));
  }

  private void changeChartData(boolean showPositiveData) {
    pieChart.getData().clear();
    if (showPositiveData) {
      pieChart.getData().addAll(posChartData);
    } else {
      pieChart.getData().addAll(negChartData);
    }
    addToolTipForData(pieChart.getData());
    addTableViewPopUpForData(pieChart.getData());

  }

  private void addToolTipForData(final ObservableList<PieChart.Data> chartData) {
    // Display the amount for the data-point
    for (final PieChart.Data data : chartData) {
      String displayingText = String.valueOf(data.getPieValue()) + '€';
      Tooltip tlp = new Tooltip(displayingText);
      tlp.setShowDelay(new Duration(100));
      Tooltip.install(data.getNode(), tlp);
    }
  }

  private void addTableViewPopUpForData(final ObservableList<PieChart.Data> chartData) {
    // PopUp for every data-point to display transactions for this day
    // Uses the prebuild table fxml
    for (final PieChart.Data data : chartData) {
      data.getNode().setOnMouseClicked(mouseEvent -> openTablePopUp(data));
    }
  }

  private void openTablePopUp(PieChart.Data data) {
    ObservableList<Transaction> tableData =
        negPosTglBtn.isSelected() ?
            posEntryLookup.get(data.getName()) :
            negEntryLookup.get(data.getName());

    TransactionTablePopUp.Builder.init(tableData)
        .setContextMenu(Model.getInstance().getProfile().getPieCategories())
        .showAndWait();
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
