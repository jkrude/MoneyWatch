package com.jkrude.category;

import com.jkrude.material.Money;
import com.jkrude.material.TransactionContainer;
import com.jkrude.material.TransactionContainer.Transaction;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TreeChartData {

  private TreeChartData parent;
  private CategoryNode category;
  private TransactionContainer transactionCollection;
  private ObservableList<Transaction> matchedTransactions;
  private Money value;
  private Set<TreeChartData> children;

  public TreeChartData(CategoryNode category, List<Transaction> matchedTransactions,
      TransactionContainer transactionContainer) {
    this.transactionCollection = transactionContainer;
    this.category = category;
    this.matchedTransactions = FXCollections.observableList(matchedTransactions);
    this.children = new HashSet<>();
  }

  private TreeChartData(CategoryNode category, TransactionContainer container) {
    this.transactionCollection = container;
    this.category = category;
    this.matchedTransactions = FXCollections.observableArrayList();
    this.children = new HashSet<>();
    genChildren();
    calculateValue();
    // If the source is changing -> recalculate the value
    container.getSource().addListener((InvalidationListener) observable -> calculateValue());
  }

  public static TreeChartData createTree(CategoryNode rootCategory,
      TransactionContainer container) {
    return new TreeChartData(rootCategory, container);
  }

  private void genChildren() {
    category.childrenRO().stream()
        .map(categoryNode -> new TreeChartData(categoryNode, transactionCollection))
        .forEach(this::addChildren);

  }

  private void calculateValue() {
    for (Transaction t : transactionCollection.getSource()) {
      for (Rule r : category.leafsRO()) {
        if (r.getPredicate().test(t)) {
          matchedTransactions.add(t);
        }
      }
    }
    this.value = Money.sum(matchedTransactions);
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

  public ReadOnlyListWrapper<Transaction> matchedTransactionsRO() {
    return new ReadOnlyListWrapper<>(matchedTransactions);
  }
}
