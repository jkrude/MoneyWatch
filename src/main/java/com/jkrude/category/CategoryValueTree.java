package com.jkrude.category;

import com.jkrude.transaction.ExtendedTransaction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

public class CategoryValueTree implements Observable {

  private CategoryValueNode root;
  private final CategoryNode categoryNodeRoot;
  private ObservableList<ExtendedTransaction> source;
  private final ObservableSet<ExtendedTransaction> unmatchedTransactions;
  private final List<InvalidationListener> listeners;
  private final RecursionAwareListener nodeListener = new RecursionAwareListener() {
    @Override
    protected void onInvalidation() {
      for (InvalidationListener listener : listeners) {
        listener.invalidated(CategoryValueTree.this);
      }
      // This may be called in the initial build step of the root-element.
      if (root != null) {
        collectUnmatchedTransactions();
      }
    }
  };


  public static CategoryValueTree buildTree(CategoryNode categoryNodeRoot,
      ObservableList<ExtendedTransaction> source) {
    return new CategoryValueTree(categoryNodeRoot, source);
  }

  private CategoryValueTree(CategoryNode categoryRoot,
      ObservableList<ExtendedTransaction> source) {
    this.listeners = new ArrayList<>();
    this.categoryNodeRoot = categoryRoot;
    this.source = source;
    this.unmatchedTransactions = FXCollections.observableSet();

    root = new CategoryValueNode(categoryRoot, this, nodeListener);

    collectUnmatchedTransactions();
  }

  public ObservableList<ExtendedTransaction> getSource() {
    return source;
  }

  public CategoryValueNode getRoot() {
    return root;
  }

  public CategoryValueNode findParent(CategoryValueNode node) {
    if (root == node) {
      return root;
    } else {
      Optional<CategoryValueNode> opt = root.stream()
          .filter(categoryValueNode -> categoryValueNode == node).findFirst();
      if (opt.isPresent()) {
        return opt.get();
      }
    }
    // Node could not be found.
    throw new IllegalArgumentException("Node was not within tree" + node.toString());
  }

  public void changeSource(ObservableList<ExtendedTransaction> newSource) {
    this.source = newSource;
    // It is simpler rebuilding the tree than to update every node.
    this.root = new CategoryValueNode(categoryNodeRoot, this, nodeListener);
    collectUnmatchedTransactions();
  }

  private void collectUnmatchedTransactions() {
    Set<ExtendedTransaction> allMatched = new HashSet<>(source.size());
    root.stream()
        .map(t -> t.getMatchedTransactions().getBaseList())
        .forEach(allMatched::addAll);
    List<ExtendedTransaction> notMatched = new ArrayList<>(source
        .filtered(extendedTransaction -> !extendedTransaction.getBaseTransaction().isPositive()));
    notMatched.removeAll(allMatched);
    unmatchedTransactions.removeIf(et -> !notMatched.contains(et));
    unmatchedTransactions.addAll(notMatched);
  }

  public ObservableSet<ExtendedTransaction> getUnmatchedTransactions() {
    return unmatchedTransactions;
  }


  public Map<ExtendedTransaction, List<CategoryValueNode>> calcDoubleMatchedTransactions() {
    Map<ExtendedTransaction, List<CategoryValueNode>> doubleMatched = new HashMap<>();
    root.stream().forEach(node -> node.getMatchedTransactions().forEach(
        extendedTransaction -> {
          if (doubleMatched.containsKey(extendedTransaction)) {
            doubleMatched.get(extendedTransaction).add(node);
          } else {
            var arrayList = new ArrayList<CategoryValueNode>();
            arrayList.add(node);
            doubleMatched.put(extendedTransaction, arrayList);
          }
        }
    ));
    doubleMatched.entrySet().removeIf(entry -> entry.getValue().size() == 1);
    return doubleMatched;
  }

  @Override
  public void addListener(InvalidationListener invalidationListener) {
    listeners.add(invalidationListener);
  }

  @Override
  public void removeListener(InvalidationListener invalidationListener) {
    listeners.remove(invalidationListener);
  }


  public static abstract class RecursionAwareListener {

    private int invalidationCounter = 0;

    public void startInvalidation() {
      invalidationCounter++;
    }

    public void endInvalidation() {
      if (invalidationCounter <= 0) {
        throw new IllegalStateException("Invalidation was ended but not started");
      }
      invalidationCounter--;
      if (invalidationCounter == 0) {
        onInvalidation();
      }
    }

    protected abstract void onInvalidation();

    public void startAndEndInvalidation() {
      startInvalidation();
      endInvalidation();
    }

  }
}
