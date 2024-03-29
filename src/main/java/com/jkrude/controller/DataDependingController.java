package com.jkrude.controller;

import com.jkrude.UI.SourceChoiceDialog;
import com.jkrude.material.Model;
import com.jkrude.transaction.TransactionContainer;

public abstract class DataDependingController {

  public static TransactionContainer fetchDataWithPossibleDialog() {
    TransactionContainer data;

    if (Model.getInstance().getTransactionContainerList() == null
        || Model.getInstance().getTransactionContainerList().isEmpty()) {

      throw new IllegalStateException("Controller was called but no data is available");
    } else if (Model.getInstance().getTransactionContainerList().size() > 1) {
      data = SourceChoiceDialog
          .showAndWait(Model.getInstance().getTransactionContainerList());
    } else {
      data = Model.getInstance().getTransactionContainerList().get(0);
    }
    return data;
  }
}
