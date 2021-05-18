package com.jkrude.controller;

import static com.jkrude.controller.SunburstChartViewModel.UNDEFINED_SEGMENT;

import com.jkrude.category.CategoryNode;
import com.jkrude.category.Rule;
import com.jkrude.controller.CategoryEditorView.RuleCell;
import com.jkrude.main.Main;
import com.jkrude.main.Main.UsableScene;
import com.jkrude.material.AlertBox;
import com.jkrude.material.UI.RuleDialog;
import com.jkrude.material.UI.SourceChoiceDialog;
import com.jkrude.material.UI.TransactionTablePopUp;
import com.jkrude.material.UI.TransactionTableView;
import com.jkrude.transaction.ExtendedTransaction;
import com.jkrude.transaction.Transaction;
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
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.layout.AnchorPane;

public class SunburstChartView implements FxmlView<SunburstChartViewModel>, Initializable,
    Prepareable {

  @FXML
  private AnchorPane ttv;
  @FXML
  private TransactionTableView ttvController;
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
    ttvController.setIsActiveColumnShown(false);
    ttvController.itemsProperty().bind(viewModel.ignoredTransactions());
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
    chartHoldingPane.getChildren().clear();
    chartHoldingPane.getChildren().add(SunburstChartBuilder.create()
        .prefSize(800, 662)
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
                this::undefinedContextMenu
                : this::categoryContextMenu)
        .showAndWait();
  }

  private ContextMenu categoryContextMenu(TableRow<ExtendedTransaction> row) {
    ContextMenu contextMenu = new ContextMenu();
    MenuItem ignoreTransaction = new MenuItem("Ignore/Activate transaction");
    ignoreTransaction.setOnAction(event -> row.getItem().switchActive());
    MenuItem findMatchingRule = new MenuItem("Show matched rule");
    findMatchingRule.setOnAction(action -> findMatchingRules(row.getItem()));
    contextMenu.getItems().addAll(ignoreTransaction, findMatchingRule);
    return contextMenu;
  }


  private ContextMenu undefinedContextMenu(
      TableRow<ExtendedTransaction> row) {
    // IMPORTANT: The transaction will only be evaluated when the contextmenu is shown.
    // Otherwise (if the Transaction would be given at row creation) it would be null.
    ContextMenu contextMenu = TransactionTableView.defaultContextMenu(row);
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

  private void findMatchingRules(ExtendedTransaction transaction) {
    List<Rule> matchingRules = viewModel.findMatchingRules(transaction);
    ListView<Rule> rulesList = new ListView<>();
    rulesList.setCellFactory(callback -> new RuleCell() {
      @Override
      protected void updateItem(Rule rule, boolean isEmpty) {
        super.updateItem(rule, isEmpty);
        if (!isEmpty) {
          assert rule.getParent().isPresent();
          setText(
              "Category: " + rule.getParent().orElseThrow().getName() + ",\t Rule: " + getText());
        }
      }
    });
    rulesList.getItems().addAll(matchingRules);
    AlertBox.displayGeneric("Matched Rules", rulesList, 600, 70);
  }

  private void openRuleDialogAndSave(Transaction baseTransaction, CategoryNode categoryNode) {
    Optional<Rule> optRule = new RuleDialog().editRuleShowAndWait(baseTransaction.getAsMap());
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
