package com.jkrude.material;

import com.jkrude.transaction.ExtendedTransaction;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.collections.ObservableList;

/**
 * Small record holding an indicator whether the data is valid and the data itself. This is useful if invalidation
 * events triggered by updates to the list should not invalidate the complete setup.
 */
public class ActiveTransactionsList {

  public final Observable isActive;

  public final ReadOnlyListWrapper<ExtendedTransaction> listProperty;

  public ActiveTransactionsList(
      Observable isActiveIndicator,
      ObservableList<ExtendedTransaction> transactions
  ) {
    listProperty = new ReadOnlyListWrapper<>(transactions);
    isActive = isActiveIndicator;
  }
}
