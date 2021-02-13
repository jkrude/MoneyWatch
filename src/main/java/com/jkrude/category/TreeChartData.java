package com.jkrude.category;

import com.jkrude.material.DoubleBindingSum;
import com.jkrude.material.PropertyFilteredList;
import com.jkrude.material.Utility;
import com.jkrude.transaction.ExtendedTransaction;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.stream.Stream;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class TreeChartData {

  private TreeChartData parent;
  private CategoryNode category;
  private ObservableList<ExtendedTransaction> source;
  private final PropertyFilteredList<ExtendedTransaction> matchedTransactions;
  private final DoubleProperty valueProperty;
  private final DoubleBindingSum accumulatedValues;
  private final ObservableList<TreeChartData> children;


  private TreeChartData(
      CategoryNode categoryNode,
      ObservableList<ExtendedTransaction> source,
      PropertyFilteredList<ExtendedTransaction> matchedTransactions) {
    this.category = categoryNode;
    this.source = source;
    this.children = FXCollections.observableArrayList();
    this.matchedTransactions = matchedTransactions;
    this.valueProperty = Utility.bindToSumOfList(this.matchedTransactions.getFilteredList());
    this.accumulatedValues = new DoubleBindingSum(Utility.asBinding(valueProperty));
    this.registerListener();
  }

  public TreeChartData(CategoryNode category,
      List<ExtendedTransaction> matchedTransactions,
      ObservableList<ExtendedTransaction> source) {
    this(category, source, new PropertyFilteredList<>(
        ExtendedTransaction::isActiveProperty, matchedTransactions));
  }

  private TreeChartData(CategoryNode category, ObservableList<ExtendedTransaction> source) {
    this(category, source, new PropertyFilteredList<>(ExtendedTransaction::isActiveProperty));
    genChildren();
    calculateMatchedTransactions();
  }


  public static TreeChartData createTree(
      CategoryNode rootCategory,
      ObservableList<ExtendedTransaction> observableTransactions) {

    return new TreeChartData(rootCategory, observableTransactions);
  }

  public static void changeSource(
      ObservableList<ExtendedTransaction> negativeTransactions,
      TreeChartData root) {
    root.changeSourceRec(negativeTransactions);
  }

  private void changeSourceRec(ObservableList<ExtendedTransaction> transactions) {
    this.children.forEach(node -> node.changeSourceRec(transactions));
    this.source.clear();
    this.source.addAll(transactions);
    calculateMatchedTransactions();
  }

  private void registerListener() {
    category.rulesRO().addListener(
        (InvalidationListener) observable -> calculateMatchedTransactions());
    category.childNodesRO().addListener(new ListChangeListener<CategoryNode>() {
      @Override
      public void onChanged(Change<? extends CategoryNode> change) {
        while (change.next()) {
          if (change.wasAdded()) {
            change.getAddedSubList().forEach(categoryNode ->
                TreeChartData.this.addChildren(new TreeChartData(categoryNode, source)));
          }
          if (change.wasRemoved()) {
            removeChildren(change.getRemoved());

          }
        }
      }
    });
    this.children.addListener(new ListChangeListener<TreeChartData>() {
      @Override
      public void onChanged(Change<? extends TreeChartData> change) {
        while (change.next()) {
          if (change.wasAdded()) {
            for (TreeChartData added : change.getAddedSubList()) {
              accumulatedValues.addDependency(added.getValueBinding());
            }
          }
          if (change.wasRemoved()) {
            for (TreeChartData removed : change.getRemoved()) {
              accumulatedValues.removeDependency(removed.getValueBinding());
            }
          }
        }
      }
    });
  }

  private void genChildren() {
    category.childNodesRO().stream()
        .map(categoryNode -> new TreeChartData(categoryNode, source))
        .forEach(this::addChildren);
  }

  private void calculateMatchedTransactions() {
    matchedTransactions.clear();
    for (ExtendedTransaction t : source) {
      for (Rule r : category.rulesRO()) {
        if (r.getPredicate().test(t.getBaseTransaction())) {
          matchedTransactions.add(t);
        }
      }
    }
  }

  private void addChildren(TreeChartData treeChartData) {
    treeChartData.setParent(this);
    this.children.add(treeChartData);
  }

  private void removeChildren(List<? extends CategoryNode> categoryNodes) {
    ListIterator<TreeChartData> iterator = this.children.listIterator();
    while (iterator.hasNext()) {
      var next = iterator.next();
      if (categoryNodes.contains(next.category)) {
        next.parent = null;
        iterator.remove();
      }
    }
  }

  public void update(List<ExtendedTransaction> matchedTransactions,
      ObservableList<ExtendedTransaction> negativeTransactions) {
    this.source = negativeTransactions;
    this.matchedTransactions.clear();
    this.matchedTransactions.addAll(matchedTransactions);
  }

  public Stream<TreeChartData> stream() {
    if (getChildren().isEmpty()) {
      return Stream.of(this);
    } else {
      return getChildren().stream()
          .map(TreeChartData::stream)
          .reduce(Stream.of(this), Stream::concat);
    }
  }


  /*
   * Getter / Setter
   */
  public Optional<TreeChartData> getParent() {
    return Optional.ofNullable(parent);
  }

  public void setParent(TreeChartData parent) {
    this.parent = parent;
  }

  public CategoryNode getCategory() {
    return category;
  }

  public void setCategory(CategoryNode category) {
    this.category = category;
  }

  public DoubleBinding getValueBinding() {
    return accumulatedValues;
  }

  public double getValue() {
    return accumulatedValues.get();
  }

  public ObservableList<TreeChartData> getChildren() {
    return children;
  }

  public PropertyFilteredList<ExtendedTransaction> getMatchedTransactions() {
    return matchedTransactions;
  }
}
