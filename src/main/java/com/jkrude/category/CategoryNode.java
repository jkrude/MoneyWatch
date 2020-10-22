package com.jkrude.category;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;

public class CategoryNode implements Observable {

  private List<InvalidationListener> invalidationListenerList;
  private ReadOnlyListWrapper<CategoryNode> children;
  private ReadOnlyListWrapper<Rule> leafs;
  private StringProperty name;
  private CategoryNode parent;
  private int depth;
  private static final int MAX_DEPTH = 3;

  public CategoryNode(String name) {
    this(new SimpleStringProperty(name));
  }

  public CategoryNode(StringProperty name) {
    this.name = name;
    this.invalidationListenerList = new ArrayList<>();
    this.children = new ReadOnlyListWrapper<>(FXCollections.observableArrayList());
    this.leafs = new ReadOnlyListWrapper<>(FXCollections.observableArrayList());
    this.depth = 0;

    InvalidationListener invalidateOnNameChange = observable -> invalidationListenerList
        .forEach(
            (InvalidationListener listener) -> listener.invalidated(this));
    this.name.addListener(invalidateOnNameChange);
    ListChangeListener<? super CategoryNode> invalidateOnListChangeNode =
        change -> invalidationListenerList
            .forEach(invalidationListener -> invalidationListener.invalidated(this));
    ListChangeListener<? super Rule> invalidateOnListChangeRule =
        change -> invalidationListenerList
            .forEach(invalidationListener -> invalidationListener.invalidated(this));
    this.children.addListener(invalidateOnListChangeNode);
    this.leafs.addListener(invalidateOnListChangeRule);
  }

  public CategoryNode(StringProperty name, CategoryNode parent) {
    this(name);
    this.parent = parent;
  }

  public CategoryNode(StringProperty name, List<CategoryNode> children, List<Rule> leafs) {
    this(name);
    children.forEach(this::addCategoryIfPossible);
    this.leafs.addAll(leafs);
  }

  public CategoryNode(String name, List<Rule> leafs) {
    this(name);
    this.leafs.addAll(leafs);
  }

  public boolean addAllRules(Collection<Rule> rules) {
    return leafs.addAll(rules);
  }

  public boolean addRule(Rule rule) {
    rule.setParent(this);
    return leafs.add(rule);
  }

  public boolean removeRule(Rule rule) {
    return leafs.remove(rule);
  }

  public boolean addCategoryIfPossible(CategoryNode categoryNode) {
    int otherDepth = categoryNode.depth;
    int depthIfAdded = Math.max(this.depth + 1, otherDepth);
    if (depthIfAdded <= MAX_DEPTH) {
      this.depth = depthIfAdded;
      categoryNode.setParent(this);
      return children.add(categoryNode);
    } else {
      return false;
    }
  }

  public boolean removeCategory(CategoryNode categoryNode) {
    if (children.remove(categoryNode)) {
      categoryNode.setParent(null);
      this.depth = children.stream().mapToInt(CategoryNode::getDepth).max().orElse(0);
      return true;
    } else {
      return false;
    }
  }

  public CategoryNode getRoot() {
    if (getParent().isEmpty()) {
      return this;
    }
    CategoryNode nextParent = getParent().get();
    while (nextParent.getParent().isPresent()) {
      nextParent = nextParent.getParent().get();
    }
    return nextParent;
  }

  public boolean isLeaf() {
    return children.get().isEmpty();
  }

  public boolean isRoot() {
    return parent == null;
  }

  public Stream<CategoryNode> stream() {
    if (childrenRO().isEmpty()) {
      return Stream.of(this);
    } else {
      return childrenRO().stream()
          .map(CategoryNode::stream)
          .reduce(Stream.of(this), Stream::concat);
    }
  }

  @Override
  public void addListener(InvalidationListener invalidationListener) {
    invalidationListenerList.add(invalidationListener);

  }

  @Override
  public void removeListener(InvalidationListener invalidationListener) {
    invalidationListenerList.remove(invalidationListener);
  }

  public ReadOnlyListWrapper<CategoryNode> childrenRO() {
    return children;
  }

  public ReadOnlyListWrapper<Rule> leafsRO() {
    return leafs;
  }

  public String getName() {
    return name.get();
  }

  public StringProperty nameProperty() {
    return name;
  }

  private void setParent(CategoryNode node) {
    this.parent = node;
  }

  public Optional<CategoryNode> getParent() {
    return Optional.ofNullable(parent);
  }

  public boolean hasParent() {
    return parent != null;
  }

  public int getDepth() {
    return depth;
  }

  public static int getMaxDepth() {
    return MAX_DEPTH;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CategoryNode other = (CategoryNode) o;
    boolean r = depth == other.depth &&
        children.equals(other.children) &&
        leafs.equals(other.leafs) &&
        getName().equals(other.getName()) &&
        this.hasParent() == other.hasParent();
    if (r && other.getParent().isPresent()) {
      r = this.parent.getName().equals(other.getParent().get().getName());
    }
    return r;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName());
  }
}
