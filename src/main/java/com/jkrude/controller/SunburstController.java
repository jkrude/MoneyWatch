package com.jkrude.controller;

import com.jkrude.category.CategoryNode;
import com.jkrude.category.Rule;
import com.jkrude.category.TreeChartData;
import com.jkrude.main.Main;
import com.jkrude.main.Main.UsableScene;
import com.jkrude.material.Model;
import com.jkrude.material.Money;
import com.jkrude.material.TransactionContainer;
import com.jkrude.material.TransactionContainer.Transaction;
import com.jkrude.material.TransactionContainer.TransactionField;
import com.jkrude.material.TreeNodeAdapter;
import com.jkrude.material.UI.RuleDialog;
import com.jkrude.material.UI.TransactionTablePopUp;
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
import javafx.scene.control.Button;
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

  public Button backBtn;
  public ToggleButton negPosTglBtn;
  @FXML
  private AnchorPane chartHoldingPane;
  private BooleanProperty invalidatedProperty;

  private Map<String, TreeChartData> nameToDataMap;

  @Override
  public void prepare() {
    chartHoldingPane.setVisible(true);
    if (invalidatedProperty.get()) {
      ObservableList<Transaction> negativeTransactions = Model.getInstance()
          .getTransactionContainerList().get(0)
          .getSource().stream()
          .filter(t -> !t.isPositive())
          .collect(Collectors.toCollection(FXCollections::observableArrayList));
      super.transactions = new TransactionContainer(negativeTransactions);
      CategoryNode rootCategory = Model.getInstance().getProfile().getRootCategory();
      rootCategory.stream().forEach(node -> node.addListener(
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

  private void invalidate() {
    if (chartHoldingPane.isVisible()) {
      drawChart(Model.getInstance().getProfile().getRootCategory());
    } else {
      invalidatedProperty.set(true);
    }
  }

  private void drawChart(CategoryNode rootCategory) {
    nameToDataMap.clear();
    TreeChartData rootChartData = TreeChartData.createTree(rootCategory, transactions);
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
    List<Transaction> allMatched = new ArrayList<>(transactions.getSource().size());
    rootChartData.stream().map(tData -> tData.matchedTransactionsRO().get())
        .forEach(allMatched::addAll);
    List<Transaction> notMatched = new ArrayList<>(transactions.getSource());
    notMatched.removeAll(allMatched);
    TreeNode<ChartItem> undefinedNode = new TreeNode<>(
        new ChartItem(
            UNDEFINED_SEGMENT,
            Money.sum(notMatched).getRawAmount().floatValue(),
            Color.rgb(96, 96, 96)),
        root
    );
    root.addNode(undefinedNode);
    TreeChartData undefinedSegment = new TreeChartData(
        new CategoryNode(UNDEFINED_SEGMENT),
        notMatched,
        super.transactions);
    nameToDataMap.put(UNDEFINED_SEGMENT, undefinedSegment);
  }

  private void openTablePopUp(String categoryName) {
    var treeChartData = nameToDataMap.get(categoryName);
    //TODO: bind data to popup / refresh popup if data is changed
    var builder = TransactionTablePopUp.Builder
        .init(treeChartData.matchedTransactionsRO());
    if (categoryName.equals(UNDEFINED_SEGMENT)) {
      builder.setContextMenu(this::contextMenuGenerator);
    }
    builder.showAndWait();
  }

  private ContextMenu contextMenuGenerator(TableRow<Transaction> row) {
    // IMPORTANT: The transaction will only be evaluated when the contextmenu is shown.
    // Otherwise (if the Transaction would be given at row creation) it would be null.
    ContextMenu contextMenu = new ContextMenu();
    Menu categoryChoices = new Menu("Add as rule");
    Model.getInstance().getProfile().getRootCategory().stream().forEach(
        categoryNode -> {
          // Rules can only be applied to leafs
          if (categoryNode.isLeaf()) {
            MenuItem menuItem = new MenuItem(categoryNode.getName());
            menuItem.setOnAction(event -> openRuleDialogAndSave(row.getItem(), categoryNode));
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