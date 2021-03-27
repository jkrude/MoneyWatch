package com.jkrude.controller;

import com.jkrude.category.CategoryNode;
import com.jkrude.category.CategoryValueNode;
import com.jkrude.category.CategoryValueTree;
import com.jkrude.category.TreeNodeAdapter;
import com.jkrude.material.Model;
import com.jkrude.material.Utility;
import com.jkrude.transaction.ExtendedTransaction;
import com.jkrude.transaction.TransactionContainer;
import de.saxsys.mvvmfx.ViewModel;
import eu.hansolo.fx.charts.data.ChartItem;
import eu.hansolo.fx.charts.data.TreeNode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

public class SunburstChartViewModel implements ViewModel {

  public static final String UNDEFINED_SEGMENT = "Undefined";

  private final BooleanProperty invalidatedProperty;

  private final Model globalModel;

  private final Map<String, CategoryValueNode> nameToDataMap;
  private ObservableList<ExtendedTransaction> negativeTransactions;
  //private final Cate undefinedSegment;
  private final CategoryValueTree categoryValueTree;


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
    categoryValueTree = CategoryValueTree.buildTree(rootCategory, negativeTransactions);
    // Respond to dependencies.
    registerInvalidationListener();

    // Invalid if no real data is present.
    invalidatedProperty.set(!hasActiveDataProperty());

  }

  public void addChangeListener(ChangeListener<Boolean> listener) {
    invalidatedProperty.addListener(listener);
  }

  public TreeNode<ChartItem> getAdaptedRoot() {
    TreeNode<ChartItem> adaptedRoot = TreeNodeAdapter
        .asTreeNode(this.categoryValueTree, this.nameToDataMap);
    ChartItem item = new ChartItem();
    item.setName(UNDEFINED_SEGMENT);
    item.valueProperty().bind(Utility.bindToSumOfSet(categoryValueTree.getUnmatchedTransactions()));
    item.setFill(Color.GRAY);
    TreeNode<ChartItem> undefinedNode = new TreeNode<>(item, adaptedRoot);
    adaptedRoot.addNode(undefinedNode);
    return adaptedRoot;
  }

  public void possibleActiveDataChange(TransactionContainer chosenData) {
    if (!chosenData.equals(globalModel.getActiveData())) {
      // Implicitly triggers onActiveDataChange.
      globalModel.setActiveData(chosenData);
    }
  }

  private void onActiveDataChange(TransactionContainer chosenData) {
    globalModel.setActiveData(chosenData);
    negativeTransactions = filterTransactions(chosenData);
    categoryValueTree.changeSource(negativeTransactions);
  }

  private void registerInvalidationListener() {
    // Any valueBinding changes (within the tree) are propagated to the top.
    categoryValueTree.addListener(observable -> invalidatedProperty.set(true));
    globalModel.getProfile().getRootCategory().streamCollapse().forEach(
        categoryNode -> {
          categoryNode.colorProperty()
              .addListener(observable -> invalidatedProperty.set(true));
          categoryNode.nameProperty().addListener(observable -> invalidatedProperty.set(true));
        });
    globalModel.activeDataProperty()
        .addListener(observable -> {
          onActiveDataChange(globalModel.getActiveData());
          invalidatedProperty.set(true);
        });
  }


  private ObservableList<ExtendedTransaction> filterTransactions(TransactionContainer data) {
    return data.getSourceRO().stream()
        .filter(t -> !t.getBaseTransaction().isPositive())
        .collect(Collectors.toCollection(FXCollections::observableArrayList));
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

  public ObservableList<ExtendedTransaction> getTransactionsForSegment(String segmentName) {
    if (segmentName.equals(UNDEFINED_SEGMENT)) {
      return Utility.bindList2Set(categoryValueTree.getUnmatchedTransactions());

    }
    if (!nameToDataMap.containsKey(segmentName)) {
      throw new IllegalArgumentException("Segment does not exist");
    }
    return nameToDataMap.get(segmentName).getMatchedTransactions().getBaseList();
  }

  public Stream<CategoryNode> collapsedCategories() {
    return globalModel.getProfile().getRootCategory().streamCollapse();
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

}
