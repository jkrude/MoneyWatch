package com.jkrude.controller;

import com.jkrude.category.CategoryNode;
import com.jkrude.category.Rule;
import com.jkrude.category.TreeChartData;
import com.jkrude.category.TreeNodeAdapter;
import com.jkrude.main.Main;
import com.jkrude.main.Main.UsableScene;
import com.jkrude.material.Model;
import com.jkrude.material.UI.RuleDialog;
import com.jkrude.material.UI.SourceChoiceDialog;
import com.jkrude.material.UI.TransactionTablePopUp;
import com.jkrude.transaction.ExtendedTransaction;
import com.jkrude.transaction.Transaction;
import com.jkrude.transaction.Transaction.TransactionField;
import com.jkrude.transaction.TransactionContainer;
import eu.hansolo.fx.charts.SunburstChart.TextOrientation;
import eu.hansolo.fx.charts.SunburstChartBuilder;
import eu.hansolo.fx.charts.data.ChartItem;
import eu.hansolo.fx.charts.data.TreeNode;
import eu.hansolo.fx.charts.event.EventType;
import eu.hansolo.fx.charts.event.TreeNodeEvent;
import eu.hansolo.fx.charts.event.TreeNodeEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.util.Pair;

public class SunburstController extends DataDependingController {

  public static final String UNDEFINED_SEGMENT = "Undefined";
  private final InvalidationListener invalidator = (observable -> invalidate());

  @FXML
  private AnchorPane chartHoldingPane;

  private BooleanProperty invalidatedProperty;
  private Map<String, TreeChartData> nameToDataMap;
  private ObservableList<ExtendedTransaction> negativeTransactions;
  private TreeChartData undefinedSegment;
  private TreeNode<ChartItem> adaptedRoot;
  private TreeChartData root;


  @Override
  public void prepare() {
    chartHoldingPane.setVisible(true);
    if (invalidatedProperty.get()) {
      setDataWithPossibleDialog();
      CategoryNode rootCategory = Model.getInstance().getProfile().getRootCategory();
      buildTree(rootCategory);
      drawChart();
      invalidatedProperty.set(false);
    }
  }

  @FXML
  public void initialize() {
    nameToDataMap = new HashMap<>();
    invalidatedProperty = new SimpleBooleanProperty(true);
  }

  private void setDataWithPossibleDialog() {
    this.transactions = super.fetchDataWithPossibleDialog();
    // TODO enable switch between positive and negative transactions.
    this.negativeTransactions = filterTransactions(this.transactions);
  }

  private ObservableList<ExtendedTransaction> filterTransactions(TransactionContainer data) {
    return data.getSource().stream()
        .filter(t -> !t.getBaseTransaction().isPositive())
        .collect(Collectors.toCollection(FXCollections::observableArrayList));
  }

  private void invalidate() {
    if (chartHoldingPane.isVisible()) {
      if (root == null || adaptedRoot == null) {
        createUndefinedSegment();
        buildTree(Model.getInstance().getProfile().getRootCategory());
      }

      updateUndefinedSegment();
      drawChart();
    } else {
      invalidatedProperty.set(true);
    }
  }

  private void buildTree(CategoryNode rootCategory) {
    nameToDataMap.clear();
    root = TreeChartData.createTree(rootCategory, negativeTransactions);
    adaptedRoot = TreeNodeAdapter.asTreeNode(root, nameToDataMap);
    registerInvalidationListener(root);
    createUndefinedSegment();

    adaptedRoot.setOnTreeNodeEvent(new TreeNodeEventListener() {
      @Override
      public void onTreeNodeEvent(TreeNodeEvent EVENT) {
        if (EVENT.getType() == EventType.NODE_SELECTED) {
          openTablePopUp(EVENT.getSource().getItem().getName());
        }
      }
    });
  }

  private void drawChart() {
    chartHoldingPane.getChildren().add(SunburstChartBuilder.create()
        .prefSize(1280, 662)
        .tree(adaptedRoot)
        .interactive(true)
        .textOrientation(TextOrientation.HORIZONTAL)
        .build());
  }


  private void registerInvalidationListener(TreeChartData rootChartData) {
    rootChartData.stream().forEach(treeChartData -> {
      treeChartData.getValueBinding().addListener(invalidator);
      treeChartData.getCategory().addListener(invalidator);
    });
  }

  private void createUndefinedSegment() {
    assert root != null;
    assert adaptedRoot != null;
    assert transactions != null;
    assert nameToDataMap != null;
    assert !nameToDataMap.containsKey(UNDEFINED_SEGMENT);
    assert undefinedSegment == null;

    List<ExtendedTransaction> notMatched = getUnmatchedTransactions();
    undefinedSegment = new TreeChartData(
        new CategoryNode(UNDEFINED_SEGMENT),
        notMatched,
        negativeTransactions);
    nameToDataMap.put(UNDEFINED_SEGMENT, undefinedSegment);
    ChartItem item = new ChartItem();
    item.setName(UNDEFINED_SEGMENT);
    item.valueProperty().bind(undefinedSegment.getValueBinding());
    item.setFill(Color.GRAY);
    TreeNode<ChartItem> undefinedNode = new TreeNode<>(item, adaptedRoot);
    adaptedRoot.addNode(undefinedNode);
  }

  private List<ExtendedTransaction> getUnmatchedTransactions() {
    List<ExtendedTransaction> allMatched = new ArrayList<>(negativeTransactions.size());
    root.stream()
        .map(t -> t.getMatchedTransactions().getBaseList())
        .forEach(allMatched::addAll);
    List<ExtendedTransaction> notMatched = new ArrayList<>(negativeTransactions);
    notMatched.removeAll(allMatched);
    return notMatched;
  }

  private void updateUndefinedSegment() {
    List<ExtendedTransaction> notMatched = getUnmatchedTransactions();
    this.undefinedSegment.update(notMatched, this.negativeTransactions);
  }

  private void openTablePopUp(String categoryName) {
    if (categoryName.equals(UNDEFINED_SEGMENT)) {
      TransactionTablePopUp.Builder
          .initBind(
              new ReadOnlyObjectWrapper<>(undefinedSegment.getMatchedTransactions().getBaseList()))
          .setTitle(categoryName)
          .setContextMenu(this::contextMenuGenerator)
          .showAndWait();
    } else {
      TreeChartData treeChartData = nameToDataMap.get(categoryName);
      TransactionTablePopUp.Builder
          .initBind(
              new ReadOnlyObjectWrapper<>(treeChartData.getMatchedTransactions().getBaseList()))
          .setTitle(categoryName)
          .setContextMenu(this::defaultContextMenu)
          .showAndWait();
    }
  }

  private ContextMenu defaultContextMenu(TableRow<ExtendedTransaction> row) {
    ContextMenu contextMenu = new ContextMenu();
    MenuItem ignoreTransaction = new MenuItem("Ignore/Activate transaction");
    ignoreTransaction.setOnAction(event -> row.getItem().switchActive());
    contextMenu.getItems().add(ignoreTransaction);
    return contextMenu;
  }

  private ContextMenu contextMenuGenerator(TableRow<ExtendedTransaction> row) {
    // IMPORTANT: The transaction will only be evaluated when the contextmenu is shown.
    // Otherwise (if the Transaction would be given at row creation) it would be null.
    ContextMenu contextMenu = this.defaultContextMenu(row);
    Menu categoryChoices = new Menu("Add as rule");
    Model.getInstance().getProfile().getRootCategory().streamCollapse().forEach(
        categoryNode -> {
          // Rules can only be applied to leaves.
          if (categoryNode.isLeaf()) {
            String parent = categoryNode.getParent().map(CategoryNode::getName).orElse("");
            MenuItem menuItem = new MenuItem(parent + "::" + categoryNode.getName());
            menuItem.setOnAction(
                event -> openRuleDialogAndSave(row.getItem().getBaseTransaction(), categoryNode));
            categoryChoices.getItems().add(menuItem);
          }
        }
    );
    contextMenu.getItems().add(categoryChoices);
    return contextMenu;
  }

  private void openRuleDialogAndSave(Transaction t, CategoryNode category) {
    Set<Pair<TransactionField, String>> filteredIdPairs = t.getAsPairSet().stream()
        .filter(pair -> !pair.getValue().isBlank())
        .collect(Collectors.toSet());
    Optional<Rule> optRule = new RuleDialog().editRuleShowAndWait(filteredIdPairs);
    optRule.ifPresent(category::addRule);
  }

  public void changeDataSource(ActionEvent event) {
    if (Model.getInstance().getTransactionContainerList().isEmpty()) {
      throw new IllegalStateException("No data to chose from");
    }
    TransactionContainer chosenData = SourceChoiceDialog
        .showAndWait(Model.getInstance().getTransactionContainerList());
    if (!this.transactions.equals(chosenData)) {
      this.transactions = chosenData;
      negativeTransactions = filterTransactions(chosenData);
      TreeChartData.updateSource(negativeTransactions, root);
      drawChart();
    }
  }

  public void goToCategories(ActionEvent event) {
    chartHoldingPane.setVisible(false);
    Main.goTo(UsableScene.CATEGORY_EDITOR);
  }

  public void goBack(ActionEvent event) {
    chartHoldingPane.setVisible(false);
    Main.goBack();
  }
}
