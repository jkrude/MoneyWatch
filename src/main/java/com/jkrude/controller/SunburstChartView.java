package com.jkrude.controller;

import static com.jkrude.controller.SunburstChartViewModel.UNDEFINED_SEGMENT;

import com.jkrude.UI.RuleDialog;
import com.jkrude.UI.SourceChoiceDialog;
import com.jkrude.UI.TransactionTablePopUp;
import com.jkrude.UI.TransactionTableView;
import com.jkrude.category.CategoryNode;
import com.jkrude.transaction.ExtendedTransaction;
import com.jkrude.transaction.Transaction;
import com.jkrude.transaction.TransactionContainer;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import eu.hansolo.fx.charts.SunburstChart;
import eu.hansolo.fx.charts.data.ChartItem;
import eu.hansolo.fx.charts.data.TreeNode;
import eu.hansolo.fx.charts.event.TreeNodeEvent;
import eu.hansolo.fx.charts.event.TreeNodeEventListener;
import eu.hansolo.fx.charts.event.TreeNodeEventType;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

public class SunburstChartView implements FxmlView<SunburstChartViewModel>, Initializable,
    Prepareable {

  @FXML private TransactionTableView ttvIgnoredController;
  @FXML private TransactionTableView ttvAllController;
  @FXML private SunburstChart<ChartItem> chart;

  @InjectViewModel
  private SunburstChartViewModel viewModel;

  private TreeNode<ChartItem> adaptedRoot;


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    viewModel.addChangeListener((observableValue, oldValue, newValue) -> {
      if (!oldValue && newValue) {
        invalidate();
      }
    });
  }

  @Override
  public void prepare() {
    chart.setVisible(true);
    if (viewModel.isInvalidated() || adaptedRoot == null) {
      if (!viewModel.hasActiveDataProperty()) {
        setDataWithPossibleDialog();
      }
      adaptedRoot = viewModel.getAdaptedRoot();
      applyNodeListener(adaptedRoot);
      drawChart();
      viewModel.resolvedInvalidity();
    }
  }

  private void invalidate() {
    if (chart.isVisible()) { // Otherwise checked by prepare.
      adaptedRoot = viewModel.getAdaptedRoot();
      applyNodeListener(adaptedRoot);
      drawChart();
      viewModel.resolvedInvalidity();
    }
  }

  private void applyNodeListener(TreeNode<ChartItem> node) {
    node.setOnTreeNodeEvent(new TreeNodeEventListener<>() {
      @Override
      public void onTreeNodeEvent(TreeNodeEvent EVENT) {
        if (EVENT.getType() == TreeNodeEventType.NODE_SELECTED) {
          openTablePopUp(EVENT.getSource().getItem().getName());
        }
      }
    });
  }

  @FXML
  private void changeDataSource() {
    TransactionContainer chosenData = SourceChoiceDialog
        .showAndWait(viewModel.getTransactionContainerList());
    // Chart will update on viewModel invalidation.
    viewModel.possibleActiveDataChange(chosenData);
  }

  private void setDataWithPossibleDialog() {
    viewModel.setActiveData(DataDependingController.fetchDataWithPossibleDialog());
  }

  private void drawChart() {
    chart.setTree(adaptedRoot);
  }

  private void openTablePopUp(String categoryName) {

    ObservableList<ExtendedTransaction> transactions = viewModel
        .getTransactionsForSegment(categoryName);
    var builder = TransactionTablePopUp.Builder.initBind(
            new ReadOnlyObjectWrapper<>(transactions))
        .setTitle(categoryName);
    if (categoryName.equals(UNDEFINED_SEGMENT)) {
      builder.setContextMenu(this::undefinedContextMenu);
    }
    builder.showAndWait();
  }


  private ContextMenu undefinedContextMenu(
      ExtendedTransaction transaction) {
    ContextMenu contextMenu = TransactionTableView.defaultContextMenu(transaction);
    Menu categoryChoices = new Menu("Add as rule");
    viewModel.getRootCategory().childNodesRO().stream().map(node ->
        transformCategoryNodeToMenu(node, transaction.getBaseTransaction())).forEach(
        item -> categoryChoices.getItems().add(item)
    );
    contextMenu.getItems().add(categoryChoices);
    return contextMenu;
  }

  private MenuItem transformCategoryNodeToMenu(
      CategoryNode categoryNode,
      Transaction transaction) {

    if (categoryNode.childNodesRO().isEmpty()) {
      MenuItem menuItem = new MenuItem(categoryNode.getName());
      menuItem.setOnAction(action -> openRuleDialogAndSave(
          transaction,
          categoryNode));
      return menuItem;
    }
    Menu menu = new Menu(categoryNode.getName());
    categoryNode.childNodesRO().stream()
        .map(node -> transformCategoryNodeToMenu(node, transaction))
        .forEach(menuItem -> menu.getItems().add(menuItem));
    return menu;
  }

  private void openRuleDialogAndSave(Transaction baseTransaction, CategoryNode categoryNode) {
    new RuleDialog.Builder()
        .editRule(baseTransaction.getAsMap())
        .showAndWait()
        .ifPresent(categoryNode::addRule);
  }

}
