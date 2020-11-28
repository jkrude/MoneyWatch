package com.jkrude.controller;

import com.jkrude.category.CategoryNode;
import com.jkrude.category.Rule;
import com.jkrude.category.TreeChartData;
import com.jkrude.main.Main;
import com.jkrude.main.Main.UsableScene;
import com.jkrude.material.Model;
import com.jkrude.material.Money;
import com.jkrude.material.TreeNodeAdapter;
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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.util.Pair;

public class SunburstController extends DataDependingController {

  public static final String UNDEFINED_SEGMENT = "Undefined";

  @FXML
  private ToggleButton negPosTglBtn;
  @FXML
  private AnchorPane chartHoldingPane;

  private BooleanProperty invalidatedProperty;
  private Map<String, TreeChartData> nameToDataMap;
  private ObservableList<ExtendedTransaction> negativeTransactions;
  private TreeChartData undefinedSegment;


  @Override
  public void prepare() {
    chartHoldingPane.setVisible(true);
    if (invalidatedProperty.get()) {
      setDataWithPossibleDialog();
      CategoryNode rootCategory = Model.getInstance().getProfile().getRootCategory();
      rootCategory.streamSubTree().forEach(node -> node.addListener(
          observable -> invalidate()));
      drawChart(rootCategory);
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
      drawChart(Model.getInstance().getProfile().getRootCategory());
    } else {
      invalidatedProperty.set(true);
    }
  }

  private void drawChart(CategoryNode rootCategory) {
    nameToDataMap.clear();
    TreeChartData rootChartData = TreeChartData.createTree(rootCategory, negativeTransactions);
    TreeNode<ChartItem> root = TreeNodeAdapter.asTreeNode(rootChartData, nameToDataMap);
    addUndefinedSegment(root);

    root.setOnTreeNodeEvent(new TreeNodeEventListener() {
      @Override
      public void onTreeNodeEvent(TreeNodeEvent EVENT) {
        if (EVENT.getType() == EventType.NODE_SELECTED) {
          openTablePopUp(EVENT.getSource().getItem().getName());
        }
      }
    });
    chartHoldingPane.getChildren().add(SunburstChartBuilder.create()
        .prefSize(1280, 662)
        .tree(root)
        .interactive(true)
        .textOrientation(TextOrientation.HORIZONTAL)
        .build());
  }

  private void addUndefinedSegment(TreeNode<ChartItem> root) {
    TreeChartData rootChartData = nameToDataMap.get(root.getItem().getName());
    List<ExtendedTransaction> allMatched = new ArrayList<>(negativeTransactions.size());
    rootChartData.stream()
        .map(tData -> tData.matchedTransactionsRO().get())
        .forEach(allMatched::addAll);
    List<ExtendedTransaction> notMatched = new ArrayList<>(negativeTransactions);
    notMatched.removeAll(allMatched);
    TreeNode<ChartItem> undefinedNode = new TreeNode<>(
        new ChartItem(
            UNDEFINED_SEGMENT,
            Money.mapSum(notMatched).getRawAmount().floatValue(),
            Color.rgb(96, 96, 96)),
        root
    );
    root.addNode(undefinedNode);
    if (undefinedSegment == null) {
      undefinedSegment = new TreeChartData(
          new CategoryNode(UNDEFINED_SEGMENT),
          notMatched,
          negativeTransactions);
      nameToDataMap.put(UNDEFINED_SEGMENT, undefinedSegment);
    } else {
      undefinedSegment.update(notMatched, negativeTransactions);
    }
  }

  private void openTablePopUp(String categoryName) {
    if (categoryName.equals(UNDEFINED_SEGMENT)) {
      TransactionTablePopUp.Builder
          .initBind(undefinedSegment.matchedTransactionsRO())
          .setTitle(categoryName)
          .setContextMenu(this::contextMenuGenerator)
          .showAndWait();
    } else {
      TreeChartData treeChartData = nameToDataMap.get(categoryName);
      TransactionTablePopUp.Builder
          .initSet(treeChartData.matchedTransactionsRO().get())
          .setTitle(categoryName)
          .showAndWait();
    }
  }

  private ContextMenu contextMenuGenerator(TableRow<ExtendedTransaction> row) {
    // IMPORTANT: The transaction will only be evaluated when the contextmenu is shown.
    // Otherwise (if the Transaction would be given at row creation) it would be null.
    ContextMenu contextMenu = new ContextMenu();
    Menu categoryChoices = new Menu("Add as rule");
    Model.getInstance().getProfile().getRootCategory().streamSubTree().forEach(
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
      drawChart(Model.getInstance().getProfile().getRootCategory());
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
