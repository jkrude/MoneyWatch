package com.jkrude.material;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.util.Callback;

public class PropertyFilteredList<T> implements ObservableList<T> {

  ObservableList<T> baseList;

  FilteredList<T> filteredList;

  public PropertyFilteredList(Callback<T, BooleanProperty> callback) {
    baseList = FXCollections.observableArrayList(t -> new Observable[]{callback.call(t)});
    filteredList = new FilteredList<>(baseList, t -> callback.call(t).get());
  }

  public PropertyFilteredList(Callback<T, BooleanProperty> callback, Collection<T> collection) {
    this(callback);
    baseList.addAll(collection);
  }

  public FilteredList<T> getFilteredList() {
    return filteredList;
  }

  public ObservableList<T> getBaseList() {
    return baseList;
  }

  @Override
  public void addListener(ListChangeListener<? super T> listChangeListener) {
    filteredList.addListener(listChangeListener);
  }

  @Override
  public void removeListener(ListChangeListener<? super T> listChangeListener) {
    filteredList.removeListener(listChangeListener);
  }

  @Override
  public boolean addAll(T... ts) {
    return baseList.addAll(ts);
  }

  @Override
  public boolean setAll(T... ts) {
    return baseList.setAll(ts);
  }

  @Override
  public boolean setAll(Collection<? extends T> collection) {
    return baseList.setAll(collection);
  }

  @Override
  public boolean removeAll(T... ts) {
    return filteredList.removeAll(ts);
  }

  @Override
  public boolean retainAll(T... ts) {
    return filteredList.retainAll(ts);
  }

  @Override
  public void remove(int i, int i1) {
    filteredList.remove(i, i1);
  }

  @Override
  public int size() {
    return filteredList.size();
  }

  @Override
  public boolean isEmpty() {
    return filteredList.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return filteredList.contains(o);
  }

  @Override
  public Iterator<T> iterator() {
    return filteredList.iterator();
  }

  @Override
  public Object[] toArray() {
    return filteredList.toArray();
  }

  @Override
  public <T1> T1[] toArray(T1[] t1s) {
    return filteredList.toArray(t1s);
  }

  @Override
  public boolean add(T t) {
    return baseList.add(t);
  }

  @Override
  public boolean remove(Object o) {
    return filteredList.remove(o);
  }

  @Override
  public boolean containsAll(Collection<?> collection) {
    return filteredList.containsAll(collection);
  }

  @Override
  public boolean addAll(Collection<? extends T> collection) {
    return baseList.addAll(collection);
  }

  @Override
  public boolean addAll(int i, Collection<? extends T> collection) {
    return baseList.addAll(i, collection);
  }

  @Override
  public boolean removeAll(Collection<?> collection) {
    return filteredList.retainAll(collection);
  }

  @Override
  public boolean retainAll(Collection<?> collection) {
    return filteredList.retainAll(collection);
  }

  @Override
  public void clear() {
    baseList.clear();
  }

  @Override
  public T get(int i) {
    return filteredList.get(i);
  }

  @Override
  public T set(int i, T t) {
    return baseList.set(i, t);
  }

  @Override
  public void add(int i, T t) {
    baseList.add(i, t);
  }

  @Override
  public T remove(int i) {
    return filteredList.remove(i);
  }

  @Override
  public int indexOf(Object o) {
    return filteredList.indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return filteredList.lastIndexOf(o);
  }

  @Override
  public ListIterator<T> listIterator() {
    return filteredList.listIterator();
  }

  @Override
  public ListIterator<T> listIterator(int i) {
    return filteredList.listIterator(i);
  }

  @Override
  public List<T> subList(int i, int i1) {
    return filteredList.subList(i, i1);
  }

  @Override
  public void addListener(InvalidationListener invalidationListener) {
    filteredList.addListener(invalidationListener);

  }

  @Override
  public void removeListener(InvalidationListener invalidationListener) {
    filteredList.removeListener(invalidationListener);
  }
}
