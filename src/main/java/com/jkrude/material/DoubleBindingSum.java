package com.jkrude.material;

import java.util.ArrayList;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DoubleBindingSum extends DoubleBinding {

  private final ObservableList<DoubleBinding> dependencies;
  private final InvalidationListener observer = (observable -> invalidate());

  public DoubleBindingSum() {
    dependencies = FXCollections.observableArrayList();
  }

  public DoubleBindingSum(final DoubleBinding... dependencies) {
    this.dependencies = FXCollections.observableArrayList(new ArrayList<>(dependencies.length));
    for (DoubleBinding dependency : dependencies) {
      addDependency(dependency);
    }
  }

  public void addDependency(DoubleBinding doubleBinding) {
    this.dependencies.add(doubleBinding);
    doubleBinding.addListener(observer);
    invalidate();
  }

  public void removeDependency(DoubleBinding doubleBinding) {
    if (this.dependencies.remove(doubleBinding)) {
      doubleBinding.removeListener(observer);
    }
    invalidate();
  }

  @Override
  protected double computeValue() {
    return dependencies.stream().mapToDouble(DoubleBinding::get).sum();
  }

  @Override
  public ObservableList<?> getDependencies() {
    return new ReadOnlyListWrapper<>(dependencies);
  }

  @Override
  public void dispose() {
    dependencies.forEach(doubleBinding -> {
      removeListener(observer);
      dispose();
    });
  }
}
