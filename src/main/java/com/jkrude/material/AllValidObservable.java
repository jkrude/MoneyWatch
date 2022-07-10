package com.jkrude.material;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

/**
 * Invalidate if any dependency is invalid.
 */
public class AllValidObservable implements Observable {

  private final List<InvalidationListener> listeners = new ArrayList<>();

  private final InvalidationListener notifier = observable -> listeners.forEach(l -> l.invalidated(this));
  private final Observable[] dependencies;

  public AllValidObservable(Observable... observables) {
    dependencies = observables;
    for (Observable observable : observables) {
      observable.addListener(notifier);
    }
  }

  public void deregister() {
    for (Observable dependency : dependencies) {
      dependency.removeListener(notifier);
    }
  }

  @Override
  public void addListener(InvalidationListener invalidationListener) {
    listeners.add(invalidationListener);
  }

  @Override
  public void removeListener(InvalidationListener invalidationListener) {
    listeners.add(invalidationListener);
  }
}
