package com.jkrude.category;

import com.jkrude.material.Money;
import com.jkrude.transaction.ExtendedTransaction;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TreeChartData {

  private TreeChartData parent;
  private CategoryNode category;
  private ObservableList<ExtendedTransaction> sourceTransactionsList;
  private ObservableList<ExtendedTransaction> matchedTransactions;
  private ReadOnlyObjectWrapper<ObservableList<ExtendedTransaction>> roListWrapper;
  private Money value;
  private Set<TreeChartData> children;

  public TreeChartData(CategoryNode category,
      List<ExtendedTransaction> matchedTransactions,
      ObservableList<ExtendedTransaction> observableTransactions) {

    this.sourceTransactionsList = observableTransactions;
    this.category = category;
    this.matchedTransactions = FXCollections.observableList(matchedTransactions);
    this.roListWrapper = new ReadOnlyObjectWrapper<>(
        this.matchedTransactions);
    this.children = new HashSet<>();
  }

  private TreeChartData(CategoryNode category, ObservableList<ExtendedTransaction> container) {
    this.sourceTransactionsList = container;
    this.category = category;
    this.matchedTransactions = FXCollections.observableArrayList();
    this.roListWrapper = new ReadOnlyObjectWrapper<>(
        this.matchedTransactions);
    this.children = new HashSet<>();
    genChildren();
    calculateValue();
    // If the source is changing -> recalculate the value
    container.addListener((InvalidationListener) observable -> calculateValue());
  }

  public static TreeChartData createTree(
      CategoryNode rootCategory,
      ObservableList<ExtendedTransaction> observableTransactions) {
    return new TreeChartData(rootCategory, observableTransactions);
  }

  private void genChildren() {
    category.childNodesRO().stream()
        .map(categoryNode -> new TreeChartData(categoryNode, sourceTransactionsList))
        .forEach(this::addChildren);

  }

  private void calculateValue() {
    for (ExtendedTransaction t : sourceTransactionsList) {
      for (Rule r : category.rulesRO()) {
        if (r.getPredicate().test(t.getBaseTransaction())) {
          matchedTransactions.add(t);
        }
      }
    }
    this.value = Money.mapSum(matchedTransactions);
    getChildren().forEach(child -> value.add(child.value));
  }

  public void addChildren(TreeChartData treeChartData) {
    treeChartData.setParent(this);
    this.children.add(treeChartData);
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

  public Money getValue() {
    return value;
  }

  public float valueAsFloat() {
    return value.getRawAmount().floatValue();
  }

  public Set<TreeChartData> getChildren() {
    return children;
  }

  public ReadOnlyObjectWrapper<ObservableList<ExtendedTransaction>> matchedTransactionsRO() {
    return roListWrapper;
  }

  public void update(List<ExtendedTransaction> matchedTransactions,
      ObservableList<ExtendedTransaction> negativeTransactions) {
    this.sourceTransactionsList = negativeTransactions;
    this.matchedTransactions.clear();
    this.matchedTransactions.addAll(matchedTransactions);
  }
}
