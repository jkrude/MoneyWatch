package com.jkrude.material;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class PieCategory implements Observable {

  private List<InvalidationListener> invalidationListeners;
  private InvalidationListener ruleListener = new InvalidationListener() {
    @Override
    public void invalidated(Observable observable) {
      invalidationListeners
          .forEach((InvalidationListener listener) -> listener.invalidated(PieCategory.this));
    }
  };
  private StringProperty name;
  private ReadOnlyListWrapper<Rule> rules;


  private PieCategory() {
    this.invalidationListeners = new ArrayList<>();
    this.name = new SimpleStringProperty();
    this.rules = new ReadOnlyListWrapper<>(FXCollections.observableList(new ArrayList<>()));
    this.name.addListener(ruleListener);
    this.rules.addListener(
        (ListChangeListener<? super Rule>) change -> invalidationListeners
            .forEach(invalidationListener -> invalidationListener.invalidated(PieCategory.this)));
  }

  public PieCategory(StringProperty name) {
    this();
    this.name = name;
    this.name.addListener(ruleListener);
  }

  public PieCategory(String name) {
    this();
    this.name.set(name);
  }

  public PieCategory(StringProperty name, ListProperty<Rule> rules) {
    this();
    this.name = name;
    this.name.addListener(ruleListener);
    this.rules = new ReadOnlyListWrapper<>(rules.getValue());
    this.rules.addListener(
        (ListChangeListener<? super Rule>) change -> invalidationListeners
            .forEach(invalidationListener -> invalidationListener.invalidated(PieCategory.this)));
  }

  public void addRule(Rule rule) {
    this.rules.add(rule);
  }

  public StringProperty getName() {
    return name;
  }

  public ObservableList<Rule> getRulesRO() {
    return rules.getValue();
  }

  public ListProperty<Rule> getIdentifierProperty() {
    return rules;
  }

  @Override
  public void addListener(InvalidationListener invalidationListener) {
    invalidationListeners.add(invalidationListener);
  }

  @Override
  public void removeListener(InvalidationListener invalidationListener) {
    invalidationListeners.add(invalidationListener);
  }
}
