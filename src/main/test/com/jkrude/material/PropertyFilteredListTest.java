package com.jkrude.material;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import org.junit.Before;
import org.junit.Test;

public class PropertyFilteredListTest {

  PropertyFilteredList<BooleanProperty> propList;
  BooleanProperty b1;
  BooleanProperty b2;
  BooleanProperty b3;
  BooleanProperty b4;


  @Before
  public void setUp() {
    b1 = new SimpleBooleanProperty(false);
    b2 = new SimpleBooleanProperty(true);
    b3 = new SimpleBooleanProperty(false);
    b4 = new SimpleBooleanProperty(true);
    var obList = FXCollections.observableArrayList(b1, b2, b3, b4);
    propList = new PropertyFilteredList<>(
        observable -> observable, obList);
  }

  @Test
  public void constructor() {

    assertEquals(4, propList.getBaseList().size());
    assertEquals(2, propList.size());
    assertTrue(propList.contains(b2));
    assertTrue(propList.contains(b4));
    assertFalse(propList.contains(b1));
    assertFalse(propList.contains(b3));
  }


  @Test
  public void changeProperty() {
    assertFalse(propList.contains(b1));

    b1.setValue(true);

    assertTrue(propList.contains(b1));
  }

  @Test
  public void addProperty() {
    BooleanProperty b5 = new SimpleBooleanProperty(true);
    propList.add(b5);

    assertTrue(propList.contains(b5));
    assertTrue(propList.getBaseList().contains(b5));
  }

  @Test
  public void removeProperty() {
    assertTrue(propList.contains(b2));
    propList.remove(b2);
    assertFalse(propList.contains(b2));
    assertFalse(propList.getBaseList().contains(b2));
  }

  @Test
  public void testListener() {
    final int[] calledCount = {0};
    InvalidationListener listener = obs -> calledCount[0]++;

    propList.addListener(listener);
    assertEquals(0, calledCount[0]);

    b1.setValue(true);
    assertEquals(1, calledCount[0]);

    BooleanProperty b5 = new SimpleBooleanProperty(true);
    propList.add(b5);
    assertEquals(2, calledCount[0]);

    propList.remove(b5);
    assertEquals(3, calledCount[0]);

    propList.removeListener(listener);

    b1.setValue(true);
    propList.add(b5);
    propList.remove(b5);
    assertEquals(3, calledCount[0]);


  }
}