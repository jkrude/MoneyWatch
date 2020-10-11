//package com.jkrude.controller;
//
//import com.jkrude.category.Rule;
//import com.jkrude.main.Main;
//import com.jkrude.main.Main.UsableScene;
//import com.jkrude.material.AlertBox;
//import com.jkrude.material.Model;
//import com.jkrude.material.Money;
//import com.jkrude.material.TransactionContainer;
//import com.jkrude.material.TransactionContainer.Transaction;
//import com.jkrude.material.TransactionContainer.TransactionField;
//import com.jkrude.material.UI.RuleDialog;
//import com.jkrude.material.UI.SourceChoiceDialog;
//import com.jkrude.material.UI.TransactionTablePopUp;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Optional;
//import java.util.Set;
//import java.util.function.Predicate;
//import java.util.stream.Collectors;
//import javafx.beans.property.BooleanProperty;
//import javafx.beans.property.SimpleBooleanProperty;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.collections.ObservableMap;
//import javafx.event.ActionEvent;
//import javafx.fxml.FXML;
//import javafx.scene.chart.PieChart;
//import javafx.scene.chart.PieChart.Data;
//import javafx.scene.control.Alert.AlertType;
//import javafx.scene.control.Button;
//import javafx.scene.control.ContextMenu;
//import javafx.scene.control.Menu;
//import javafx.scene.control.MenuItem;
//import javafx.scene.control.TableRow;
//import javafx.scene.control.ToggleButton;
//import javafx.scene.control.Tooltip;
//import javafx.util.Duration;
//import javafx.util.Pair;
//
//public class PieChartController extends DataDependingController {
//
//  private boolean populatedChart = false;
//  private boolean isInvalidated = false;
//  private BooleanProperty isHandlingVisibleScene;
//  // Marks if chart is up to date with Model.getInstance()
//  // Saves witch CamtEntries where found for a category
//  private Map<String, ObservableList<Transaction>> negEntryLookup;
//  private Map<String, ObservableList<Transaction>> posEntryLookup;
//  // The default name for the slice for transactions without matching rule
//  public static final String UNMATCHED_TRANSACTIONS = "Undefined";
//
//  private ObservableList<PieChart.Data> posChartData;
//  private ObservableList<PieChart.Data> negChartData;
//  private ObservableMap<Transaction, Set<Rule>> multipleMatches;
//
//  @FXML
//  private PieChart pieChart;
//  @FXML
//  private Button backBtn;
//  @FXML
//  private ToggleButton negPosTglBtn;
//
//
//  @Override
//  public void prepare() {
//    if (isInvalidated || !populatedChart) {
//      setupChart();
//    }
//    //TODO: Contract: if prepare is called the scene is shown
//    isHandlingVisibleScene.setValue(true);
//  }
//
//  @FXML
//  public void initialize() {
//    negEntryLookup = new HashMap<>();
//    posEntryLookup = new HashMap<>();
//    negChartData = FXCollections.observableArrayList();
//    posChartData = FXCollections.observableArrayList();
//    multipleMatches = FXCollections.observableHashMap();
//    transactions = null;
//    isHandlingVisibleScene = new SimpleBooleanProperty(false);
//
//    backBtn.setOnAction(this::goBack);
//    negPosTglBtn.selectedProperty().addListener(
//        (observableValue, oldV, newV) -> changeChartData(newV)); // Use pos or neg dataSet
//    Model.getInstance().getProfile()
//        .addListener(change -> refreshOrInvalidate()); // Needs to be live updated
//  }
//
//  private void refreshOrInvalidate() {
//    if (isHandlingVisibleScene.get()) {
//      setupChart();
//    } else {
//      isInvalidated = true;
//    }
//  }
//
//  private void setupChart() {
//    pieChart.getData().clear();
//    fetchDataWithDialogs();
//
//    ObservableList<Transaction> source = transactions.getSource();
//    ObservableList<PieCategory> categories = Model.getInstance().getProfile().getRootCategory();
//
//    // Populate the chart with data
//    multipleMatches = FXCollections.observableHashMap();
//    matchDataToRules(
//        source,
//        categories,
//        multipleMatches);
//    // Default: Display all negative transactions.
//    pieChart.getData().addAll(negChartData);
//    addToolTipForData(pieChart.getData());
//    addTableViewPopUpForData(pieChart.getData());
//    populatedChart = true;
//    isInvalidated = false;
//  }
//
//  @FXML
//  private void changeDataSource() {
//    if (Model.getInstance().getTransactionContainerList().isEmpty()) {
//      AlertBox.showAlert("No selection possible!", "No CSV-Files loaded", "", AlertType.ERROR);
//    } else {
//      Optional<TransactionContainer> result = SourceChoiceDialog
//          .showAndWait(Model.getInstance().getTransactionContainerList());
//      if (result.isPresent() && !result.get().equals(transactions)) {
//        transactions = result.get();
//        setupChart();
//      }
//    }
//
//  }
//
//  private void matchDataToRules(
//      ObservableList<Transaction> source,
//      ObservableList<PieCategory> categories,
//      Map<Transaction, Set<Rule>> multipleMatches
//  ) {
//    // Check for every rule for every category for every Transaction(transactions) if the predicate tests positive
//    // When a transaction tests positive but the amount is positive it gets marked in the ignoredPositiveEntries
//
//    negEntryLookup.clear();
//    posEntryLookup.clear();
//    negChartData.clear();
//    posChartData.clear();
//
//    Map<String, Set<Transaction>> matchedTransactions = new HashMap<>();
//    // Fill map with all categories and empty sets.
//    categories
//        .forEach(category -> matchedTransactions.put(category.getName(), new HashSet<>()));
//    matchedTransactions.put(UNMATCHED_TRANSACTIONS, new HashSet<>());
//    for (final Transaction transaction : source) {
//      Set<Rule> matchedRules = new HashSet<>();
//
//      for (final PieCategory category : categories) {
//
//        for (final Rule rule : category.getRulesRO()) {
//          if (rule.getPredicate().test(transaction)) {
//            if (matchedRules.isEmpty()) {
//              matchedTransactions.get(category.getName()).add(transaction);
//            }
//            matchedRules.add(rule);
//          }
//        }
//
//      }
//
//      multipleMatches.put(transaction, matchedRules);
//      if (matchedRules.isEmpty()) {
//        matchedTransactions.get(UNMATCHED_TRANSACTIONS).add(transaction);
//      }
//    }
//
//    populateWithPredicate(matchedTransactions, posEntryLookup, posChartData,
//        Transaction::isPositive);
//    populateWithPredicate(matchedTransactions, negEntryLookup, negChartData,
//        Predicate.not(Transaction::isPositive));
//    // Colors are only displayed for positive values
//    negChartData.forEach(data -> data.setPieValue(Math.abs(data.getPieValue())));
//  }
//
//  private void populateWithPredicate(
//      Map<String, Set<Transaction>> matchedTransactions,
//      Map<String, ObservableList<Transaction>> lookupTable,
//      ObservableList<PieChart.Data> chartData,
//      Predicate<Transaction> predicate) {
//
//    matchedTransactions.forEach(((category, transactions) ->
//        lookupTable.put(category,
//            transactions.stream()
//                .filter(predicate)
//                .collect(Collectors.toCollection(FXCollections::observableArrayList))
//        )));
//
//    lookupTable.forEach(
//        (category, transactions) -> chartData
//            .add(new Data(
//                category,
//                Money.sum(transactions).getRawAmount().doubleValue())));
//  }
//
//  private void changeChartData(boolean showPositiveData) {
//    pieChart.getData().clear();
//    if (showPositiveData) {
//      pieChart.getData().addAll(posChartData);
//    } else {
//      pieChart.getData().addAll(negChartData);
//    }
//    addToolTipForData(pieChart.getData());
//    addTableViewPopUpForData(pieChart.getData());
//
//  }
//
//  private void addToolTipForData(final ObservableList<PieChart.Data> chartData) {
//    // Display the amount for the data-point
//    for (final PieChart.Data data : chartData) {
//      String displayingText = String.valueOf(data.getPieValue()) + '€';
//      Tooltip tlp = new Tooltip(displayingText);
//      tlp.setShowDelay(new Duration(100));
//      Tooltip.install(data.getNode(), tlp);
//    }
//  }
//
//  private void addTableViewPopUpForData(final ObservableList<PieChart.Data> chartData) {
//    // PopUp for every data-point to display transactions for this day
//    // Uses the prebuild table fxml
//    for (final PieChart.Data data : chartData) {
//      data.getNode().setOnMouseClicked(mouseEvent -> openTablePopUp(data));
//    }
//  }
//
//  private void openTablePopUp(PieChart.Data data) {
//    ObservableList<Transaction> tableData =
//        negPosTglBtn.isSelected() ?
//            posEntryLookup.get(data.getName()) :
//            negEntryLookup.get(data.getName());
//
//    TransactionTablePopUp.Builder.init(tableData)
//        .setContextMenu(this::contextMenuGenerator)
//        .showAndWait();
//  }
//
//  private ContextMenu contextMenuGenerator(TableRow<Transaction> row) {
//    // IMPORTANT: The transaction will only be evaluated when the contextmenu is shown.
//    // Otherwise (if the Transaction would be given at row creation) it would be null.
//    ContextMenu contextMenu = new ContextMenu();
//    Menu categoryChoices = new Menu("Add as rule");
//    for (PieCategory category : Model.getInstance().getProfile().getPieCategories()) {
//      MenuItem menuItem = new MenuItem(category.getName().get());
//      menuItem.setOnAction(event -> openRuleDialogAndSave(row.getItem(), category));
//      categoryChoices.getItems().add(menuItem);
//    }
//    contextMenu.getItems().add(categoryChoices);
//    return contextMenu;
//  }
//
//  private void openRuleDialogAndSave(Transaction t, PieCategory category) {
//    Set<Pair<TransactionField, String>> filteredIdPairs = t.getAsPairSet().stream()
//        .filter(pair -> !pair.getValue().isBlank())
//        .collect(Collectors.toSet());
//    Optional<Rule> optRule = new RuleDialog().editRuleShowAndWait(filteredIdPairs);
//    optRule.ifPresent(category::addRule);
//  }
//
//  public void goToCategories(ActionEvent event) {
//    Main.goTo(UsableScene.CATEGORY_EDITOR);
//    isHandlingVisibleScene.setValue(false);
//  }
//
//  public void goBack(ActionEvent event) {
//    goBack();
//  }
//
//  public void goBack() {
//    isHandlingVisibleScene.setValue(false);
//    Main.goBack();
//  }
//
//  /*
//  Getter
//   */
//  public PieChart getPieChart() {
//    return pieChart;
//  }
//}
