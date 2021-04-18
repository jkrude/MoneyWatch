package com.jkrude.controller;

import static com.jkrude.controller.SunburstChartViewModel.UNDEFINED_SEGMENT;

import com.jkrude.category.CategoryNode;
import com.jkrude.category.Rule;
import com.jkrude.main.Main;
import com.jkrude.main.Main.UsableScene;
import com.jkrude.material.UI.RuleDialog;
import com.jkrude.material.UI.SourceChoiceDialog;
import com.jkrude.material.UI.TransactionTablePopUp;
import com.jkrude.transaction.ExtendedTransaction;
import com.jkrude.transaction.Transaction;
import com.jkrude.transaction.Transaction.TransactionField;
import com.jkrude.transaction.TransactionContainer;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import eu.hansolo.fx.charts.SunburstChart.TextOrientation;
import eu.hansolo.fx.charts.SunburstChartBuilder;
import eu.hansolo.fx.charts.data.ChartItem;
import eu.hansolo.fx.charts.data.TreeNode;
import eu.hansolo.fx.charts.event.TreeNodeEvent;
import eu.hansolo.fx.charts.event.TreeNodeEventListener;
import eu.hansolo.fx.charts.event.TreeNodeEventType;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.layout.AnchorPane;
import javafx.util.Pair;

public class SunburstChartView implements FxmlView<SunburstChartViewModel>, Initializable,
    Prepareable {

  @FXML
  private AnchorPane chartHoldingPane;

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
    chartHoldingPane.setVisible(true);
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
    if (chartHoldingPane.isVisible()) { // Otherwise checked by prepare.
      adaptedRoot = viewModel.getAdaptedRoot();
      applyNodeListener(adaptedRoot);
      drawChart();
      viewModel.resolvedInvalidity();
    }
  }

  private void applyNodeListener(TreeNode<ChartItem> node) {
    node.setOnTreeNodeEvent(new TreeNodeEventListener() {
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
    chartHoldingPane.getChildren().clear();
    chartHoldingPane.getChildren().add(SunburstChartBuilder.create()
        .prefSize(1280, 662)
        .tree(adaptedRoot)
        .interactive(true)
        .textOrientation(TextOrientation.HORIZONTAL)
        .build());
  }

  private void openTablePopUp(String categoryName) {

    ObservableList<ExtendedTransaction> transactions = viewModel
        .getTransactionsForSegment(categoryName);
    TransactionTablePopUp.Builder.initBind(
        new ReadOnlyObjectWrapper<>(transactions))
        .setTitle(categoryName)
        .setContextMenu(
            categoryName.equals(UNDEFINED_SEGMENT) ?
                this::contextMenuGenerator
                : this::defaultContextMenu)
        .showAndWait();
  }

  private ContextMenu defaultContextMenu(TableRow<ExtendedTransaction> row) {
    ContextMenu contextMenu = new ContextMenu();
    MenuItem ignoreTransaction = new MenuItem("Ignore/Activate transaction");
    ignoreTransaction.setOnAction(event -> row.getItem().switchActive());
    contextMenu.getItems().add(ignoreTransaction);
    return contextMenu;
  }


  private ContextMenu contextMenuGenerator(
      TableRow<ExtendedTransaction> row) {
    // IMPORTANT: The transaction will only be evaluated when the contextmenu is shown.
    // Otherwise (if the Transaction would be given at row creation) it would be null.
    ContextMenu contextMenu = this.defaultContextMenu(row);
    Menu categoryChoices = new Menu("Add as rule");
    viewModel.collapsedCategories().forEach(
        categoryNode -> {
          if (!categoryNode.isRoot()) {
            // Append category to parent to clarify which one is which.
            var optParent = categoryNode.getParent().map(CategoryNode::getName);
            String text = optParent.isEmpty() ? "" : optParent.get() + "::";
            text += categoryNode.getName();
            MenuItem menuItem = new MenuItem(text);
            menuItem.setOnAction(
                event -> openRuleDialogAndSave(row.getItem().getBaseTransaction(), categoryNode));
            categoryChoices.getItems().add(menuItem);
          }
        }
    );
    contextMenu.getItems().add(categoryChoices);
    return contextMenu;
  }

  private void openRuleDialogAndSave(Transaction baseTransaction, CategoryNode categoryNode) {
    Set<Pair<TransactionField, String>> filteredIdPairs = baseTransaction.getAsPairSet().stream()
        .filter(pair -> !pair.getValue().isBlank())
        .collect(Collectors.toSet());
    Optional<Rule> optRule = new RuleDialog().editRuleShowAndWait(filteredIdPairs);
    optRule.ifPresent(categoryNode::addRule);
  }

  @FXML
  private void goToCategories() {
    chartHoldingPane.setVisible(false);
    Main.goTo(UsableScene.CATEGORY_EDITOR);
  }

  @FXML
  private void goBack() {
    chartHoldingPane.setVisible(false);
    Main.goBack();
  }
}
