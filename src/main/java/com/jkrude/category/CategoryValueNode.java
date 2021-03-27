package com.jkrude.category;

import com.jkrude.category.CategoryValueTree.RecursionAwareListener;
import com.jkrude.material.DoubleBindingSum;
import com.jkrude.material.PropertyFilteredList;
import com.jkrude.material.Utility;
import com.jkrude.transaction.ExtendedTransaction;
import java.util.stream.Stream;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class CategoryValueNode {

  private final CategoryValueTree tree;
  private final CategoryNode category;
  private final PropertyFilteredList<ExtendedTransaction> matchedTransactions;
  private final DoubleProperty valueProperty;
  private final DoubleBindingSum accumulatedValues;
  private final ObservableList<CategoryValueNode> children;
  private final RecursionAwareListener listener;


  public CategoryValueNode(CategoryNode categoryNode, CategoryValueTree tree,
      RecursionAwareListener listener) {
    this.category = categoryNode;
    this.tree = tree;
    this.listener = listener;
    this.children = FXCollections.observableArrayList();
    this.matchedTransactions = new PropertyFilteredList<>(ExtendedTransaction::isActiveProperty);
    this.valueProperty = Utility.bindToSumOfList(this.matchedTransactions.getFilteredList());
    this.accumulatedValues = new DoubleBindingSum(Utility.asBinding(valueProperty));
    genChildren();
    calculateMatchedTransactions();
    registerListener();
  }


  private void registerListener() {
    category.rulesRO().addListener(
        (InvalidationListener) observable -> {
          listener.startInvalidation();
          calculateMatchedTransactions();
          listener.endInvalidation();
        }
    );

    // If children of category changes -> adjust and inform the tree.
    category.childNodesRO().addListener(new ListChangeListener<>() {
      @Override
      public void onChanged(Change<? extends CategoryNode> change) {
        listener.startInvalidation();
        while (change.next()) {
          if (change.wasAdded()) {
            change.getAddedSubList().forEach(categoryNode ->
                CategoryValueNode.this.children.add(new CategoryValueNode(categoryNode, tree,
                    listener)));
          }
          if (change.wasRemoved()) {
            for (var removedCategoryNode : change.getRemoved()) {
              CategoryValueNode.this.children
                  .removeIf(node -> node.category.equals(removedCategoryNode));
            }
          }
        }
        listener.endInvalidation();
      }
    });

    // Listen for changes of own children -> register their value as dependency.
    children.addListener(new ListChangeListener<>() {
      @Override
      public void onChanged(Change<? extends CategoryValueNode> change) {
        while (change.next()) {
          if (change.wasAdded()) {
            for (CategoryValueNode added : change.getAddedSubList()) {
              accumulatedValues.addDependency(added.getValueBinding());
            }
          }
          if (change.wasRemoved()) {
            for (CategoryValueNode removed : change.getRemoved()) {
              accumulatedValues.removeDependency(removed.getValueBinding());
            }
          }
        }
      }
    });

    // Add dependency for all existing children.
    children.forEach(child -> accumulatedValues.addDependency(child.getValueBinding()));

    // If a transaction gets activated / deactivated -> inform tree about change.
    valueProperty.addListener(observable -> listener.startAndEndInvalidation());
  }

  private void genChildren() {
    category.childNodesRO().stream()
        .map(categoryNode -> new CategoryValueNode(categoryNode, tree, listener))
        .forEach(this.children::add);
  }

  private void calculateMatchedTransactions() {
    matchedTransactions.clear();
    for (ExtendedTransaction t : tree.getSource()) {
      for (Rule r : category.rulesRO()) {
        if (r.getPredicate().test(t.getBaseTransaction())) {
          matchedTransactions.add(t);
        }
      }
    }
  }

  public Stream<CategoryValueNode> stream() {
    if (getChildren().isEmpty()) {
      return Stream.of(this);
    } else {
      return getChildren().stream()
          .map(CategoryValueNode::stream)
          .reduce(Stream.of(this), Stream::concat);
    }
  }

  /*
   * Getter / Setter
   */

  public CategoryNode getCategory() {
    return category;
  }

  public DoubleBinding getValueBinding() {
    return accumulatedValues;
  }

  public double getValue() {
    return accumulatedValues.get();
  }

  public ObservableList<CategoryValueNode> getChildren() {
    return children;
  }

  public PropertyFilteredList<ExtendedTransaction> getMatchedTransactions() {
    return matchedTransactions;
  }

  @Override
  public String toString() {
    return "CategoryValueNode{" +
        "category=" + category.getName() +
        ", accumulatedValues=" + accumulatedValues.getValue() +
        '}';
  }

}
