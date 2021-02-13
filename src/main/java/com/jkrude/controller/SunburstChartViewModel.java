package com.jkrude.controller;

import com.jkrude.category.CategoryNode;
import com.jkrude.category.TreeChartData;
import com.jkrude.category.TreeNodeAdapter;
import com.jkrude.material.Model;
import com.jkrude.material.PropertyFilteredList;
import com.jkrude.transaction.ExtendedTransaction;
import com.jkrude.transaction.TransactionContainer;
import de.saxsys.mvvmfx.ViewModel;
import eu.hansolo.fx.charts.data.ChartItem;
import eu.hansolo.fx.charts.data.TreeNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

public class SunburstChartViewModel implements ViewModel {

  public static final String UNDEFINED_SEGMENT = "Undefined";

  private final BooleanProperty invalidatedProperty;

  private final Model globalModel;

  private final Map<String, TreeChartData> nameToDataMap;
  private ObservableList<ExtendedTransaction> negativeTransactions;
  private final TreeChartData undefinedSegment;
  private final TreeChartData root;


  public SunburstChartViewModel() {
    globalModel = Model.getInstance();
    invalidatedProperty = new SimpleBooleanProperty();
    nameToDataMap = new HashMap<>();

    negativeTransactions =
        hasActiveDataProperty()
            ? filterTransactions(globalModel.getActiveData())
            : FXCollections.observableArrayList();
    // Build tree.
    CategoryNode rootCategory = globalModel.getProfile().getRootCategory();
    nameToDataMap.clear();
    root = TreeChartData.createTree(rootCategory, negativeTransactions);
    // Add undefined segment.
    List<ExtendedTransaction> notMatched = getUnmatchedTransactions();
    undefinedSegment = new TreeChartData(
        new CategoryNode(UNDEFINED_SEGMENT),
        notMatched,
        negativeTransactions);
    nameToDataMap.put(UNDEFINED_SEGMENT, undefinedSegment);
    // Respond to dependencies.
    registerInvalidationListener();

    // Invalid if no real data is present.
    invalidatedProperty.set(!hasActiveDataProperty());

  }

  public void addInvalidationListener(InvalidationListener listener) {
    invalidatedProperty.addListener(listener);
  }

  public TreeNode<ChartItem> getAdaptedRoot() {
    TreeNode<ChartItem> adaptedRoot = TreeNodeAdapter.asTreeNode(this.root, this.nameToDataMap);
    ChartItem item = new ChartItem();
    item.setName(UNDEFINED_SEGMENT);
    item.valueProperty().bind(undefinedSegment.getValueBinding());
    item.setFill(Color.GRAY);
    TreeNode<ChartItem> undefinedNode = new TreeNode<>(item, adaptedRoot);
    adaptedRoot.addNode(undefinedNode);
    return adaptedRoot;
  }

  public boolean possibleActiveDataChange(TransactionContainer chosenData) {
    if (!chosenData.equals(globalModel.getActiveData())) {
      activeDataChange(chosenData);
      return true;
    }
    return false;
  }

  private void activeDataChange(TransactionContainer chosenData) {
    globalModel.setActiveData(chosenData);
    negativeTransactions = filterTransactions(chosenData);
    TreeChartData.changeSource(negativeTransactions, root);
    updateUndefinedSegment();
  }

  private void registerInvalidationListener() {
    // Any valueBinding changes (within the tree) are propagated to the top.
    root.stream()
        .forEach(treeChartData -> {
          treeChartData.getValueBinding()
              .addListener(new AndInvalidationListener(observable -> updateUndefinedSegment()));
          treeChartData.getCategory().colorProperty()
              .addListener((observable -> invalidatedProperty.set(true)));
        });
    globalModel.activeDataProperty()
        .addListener(new AndInvalidationListener(
            (observable) -> activeDataChange(globalModel.getActiveData())));
  }


  private ObservableList<ExtendedTransaction> filterTransactions(TransactionContainer data) {
    return data.getSource().stream()
        .filter(t -> !t.getBaseTransaction().isPositive())
        .collect(Collectors.toCollection(FXCollections::observableArrayList));
  }

  private void updateUndefinedSegment() {
    List<ExtendedTransaction> notMatched = getUnmatchedTransactions();
    this.undefinedSegment.update(notMatched, this.negativeTransactions);
  }

  /*
   * Getter
   */

  public boolean isInvalidated() {
    return invalidatedProperty.get();
  }

  public void resolvedInvalidity() {
    invalidatedProperty.set(false);
  }

  public boolean hasActiveDataProperty() {
    return globalModel.activeDataProperty().isNotNull().get();
  }

  public PropertyFilteredList<ExtendedTransaction> getTransactionsForSegment(String segmentName) {
    if (!nameToDataMap.containsKey(segmentName)) {
      throw new IllegalArgumentException("Segment does not exist");
    }
    return nameToDataMap.get(segmentName).getMatchedTransactions();
  }

  public Stream<CategoryNode> collapsedCategories() {
    return globalModel.getProfile().getRootCategory().streamCollapse();
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

  public List<TransactionContainer> getTransactionContainerList() {
    return globalModel.getTransactionContainerList();
  }

  /*
   * Setter
   */

  public void setActiveData(TransactionContainer container) {
    // TODO enable switch between positive and negative transactions.
    this.negativeTransactions = filterTransactions(container);

  }

  private class AndInvalidationListener implements InvalidationListener {

    private final InvalidationListener listener;

    public AndInvalidationListener(InvalidationListener listener) {
      this.listener = listener;
    }

    @Override
    public void invalidated(Observable observable) {
      SunburstChartViewModel.this.invalidatedProperty.set(true);
      listener.invalidated(observable);
    }
  }

}
