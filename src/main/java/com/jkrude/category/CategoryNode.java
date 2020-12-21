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

  private final List<InvalidationListener> invalidationListenerList;
  private final ReadOnlyListWrapper<CategoryNode> childNodes;
  private final ReadOnlyListWrapper<Rule> rules;
  private final StringProperty name;
  private CategoryNode parent;
  private static final int MAX_DEPTH = 3;

  public CategoryNode(String name) {
    this(new SimpleStringProperty(name));
  }

  public CategoryNode(StringProperty name) {
    this.name = name;
    this.invalidationListenerList = new ArrayList<>();
    this.childNodes = new ReadOnlyListWrapper<>(FXCollections.observableArrayList());
    this.rules = new ReadOnlyListWrapper<>(FXCollections.observableArrayList());

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
    this.childNodes.addListener(invalidateOnListChangeNode);
    this.rules.addListener(invalidateOnListChangeRule);
  }

  public CategoryNode(StringProperty name, CategoryNode parent) {
    this(name);
    this.parent = parent;
  }

  public CategoryNode(StringProperty name, List<CategoryNode> childNodes, List<Rule> rules) {
    this(name);
    childNodes.forEach(this::addCategory);
    this.rules.addAll(rules);
  }

  public CategoryNode(String name, List<Rule> rules) {
    this(name);
    this.rules.addAll(rules);
  }

  public boolean addAllRules(Collection<Rule> rules) {
    return this.rules.addAll(rules);
  }

  public boolean addRule(Rule rule) {
    rule.setParent(this);
    return rules.add(rule);
  }

  public boolean removeRule(Rule rule) {
    return rules.remove(rule);
  }

  public void addCategory(CategoryNode categoryNode) {
    categoryNode.setParent(this);
    childNodes.add(categoryNode);

  }

  public boolean removeCategory(CategoryNode categoryNode) {
    if (childNodes.remove(categoryNode)) {
      Optional<CategoryNode> optParent = categoryNode.getParent();
      if (optParent.isPresent() && optParent.get().equals(this)) {
        categoryNode.setParent(null);
      }
      return true;
    }
    return false;
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
    return childNodes.get().isEmpty();
  }

  public boolean isRoot() {
    return parent == null;
  }

  public Stream<CategoryNode> streamCollapse() {
    if (childNodesRO().isEmpty()) {
      return Stream.of(this);
    } else {
      return childNodesRO().stream()
          .map(CategoryNode::streamCollapse)
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

  public ReadOnlyListWrapper<CategoryNode> childNodesRO() {
    return childNodes;
  }

  public ReadOnlyListWrapper<Rule> rulesRO() {
    return rules;
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
    boolean r =
        childNodes.equals(other.childNodes) &&
            rules.equals(other.rules) &&
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
