package com.jkrude.transaction;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

// Wraps real transactions with extra parameter.
public class ExtendedTransaction {

  // The base transaction.
  private final Transaction baseTransaction;

  // Was the transaction created by the user.
  private final BooleanProperty isArtificial;
  // Should the transaction be included in graphics / calculations.
  private BooleanProperty isActive;

  public ExtendedTransaction(final Transaction baseTransaction) {
    this.baseTransaction = baseTransaction;
    this.isArtificial = new SimpleBooleanProperty(false);
    this.isActive = new SimpleBooleanProperty(true);
  }

  public final Transaction getBaseTransaction() {
    return baseTransaction;
  }

  public boolean isIsArtificial() {
    return isArtificial.get();
  }

  public BooleanProperty isArtificialProperty() {
    return isArtificial;
  }

  public void setArtificial(boolean isArtificial) {
    this.isArtificial.set(isArtificial);
  }

  public boolean isActive() {
    return isActive.get();
  }

  public BooleanProperty isActiveProperty() {
    return isActive;
  }

  public void setIsActive(boolean isVisible) {
    this.isActive.set(isVisible);
  }
}