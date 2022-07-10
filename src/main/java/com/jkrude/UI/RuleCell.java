package com.jkrude.UI;

import com.jkrude.category.Rule;
import com.jkrude.transaction.Transaction.TransactionField;
import java.util.Map.Entry;
import java.util.Optional;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

public class RuleCell extends ListCell<Rule> {

  @Override
  protected void updateItem(Rule rule, boolean isEmpty) {
    super.updateItem(rule, isEmpty);
    if (isEmpty) {
      setGraphic(null);
      setContextMenu(null);
    } else {
      FlowPane ruleHolder = new FlowPane();
      ruleHolder.getStyleClass().clear();
      ruleHolder.getStyleClass().add("rule-holder-pane");
      for (Entry<TransactionField, String> entry : rule.getIdentifierPairs().entrySet()) {
        Label keyLabel = new Label(entry.getKey().toString());
        keyLabel.getStyleClass().clear();
        keyLabel.getStyleClass().add("rule-key-label");
        Label valueLabel = new Label(entry.getValue());
        valueLabel.getStyleClass().clear();
        valueLabel.getStyleClass().add("rule-value-label");
        HBox pairBox = new HBox(keyLabel, valueLabel);
        ruleHolder.getChildren().add(pairBox);
      }

      Optional<String> optNote = rule.getNote();
      if (optNote.isEmpty()) {
        setGraphic(ruleHolder);
        return;
      }
      SVGPath svgIcon = new SVGPath();
      svgIcon.setContent( // icons/question_mark_outlined.svg
          "M24.2 35.65q.8 0 1.35-.55t.55-1.35q0-.8-.55-1.35t-1.35-.55q-.8 0-1.35.55t-.55 1.35q0 "
              + ".8.55 1.35t1.35.55Zm-1.75-7.3h2.95q0-1.3.325-2.375T27.75 23.5q1.55-1.3 2.2-2.55"
              + ".65-1.25.65-2.75 0-2.65-1.725-4.25t-4.575-1.6q-2.45 0-4.325 1.225T17.25 16.95l2.65"
              + " 1q.55-1.4 1.65-2.175 1.1-.775 2.6-.775 1.7 0 2.75.925t1.05 2.375q0 1.1-.65 2.075-"
              + ".65.975-1.9 2.025-1.5 1.3-2.225 2.575-.725 1.275-.725 3.375ZM24 44q-4.1 0-7.75-1.5"
              + "75-3.65-1.575-6.375-4.3-2.725-2.725-4.3-6.375Q4 28.1 4 24q0-4.15 1.575-7.8 1.575-3"
              + ".65 4.3-6.35 2.725-2.7 6.375-4.275Q19.9 4 24 4q4.15 0 7.8 1.575 3.65 1.575 6.35 4."
              + "275 2.7 2.7 4.275 6.35Q44 19.85 44 24q0 4.1-1.575 7.75-1.575 3.65-4.275 6.375t-6."
              + "35 4.3Q28.15 44 24 44Zm0-3q7.1 0 12.05-4.975Q41 31.05 41 24q0-7.1-4.95-12.05Q31."
              + "1 7 24 7q-7.05 0-12.025 4.95Q7 16.9 7 24q0 7.05 4.975 12.025Q16.95 41 24 "
              + "41Zm0-17Z");
      svgIcon.setFill(Paint.valueOf("white"));
      Region boundingRegion = new Region();
      boundingRegion.setShape(svgIcon);
      boundingRegion.setMaxSize(28, 28);
      boundingRegion.setMinSize(28, 28);
      boundingRegion.setStyle("-fx-background-color: -fx-def-elevated-light;");
      BorderPane.setAlignment(boundingRegion, Pos.CENTER);
      Tooltip tlp = new Tooltip(optNote.get());
      tlp.setPrefWidth(150);
      tlp.setWrapText(true);
      tlp.setShowDelay(Duration.ONE);
      Tooltip.install(boundingRegion, tlp);
      BorderPane topContainer = new BorderPane(ruleHolder);
      topContainer.setRight(boundingRegion);
      setGraphic(topContainer);
    }
  }
}

