package voltsync.ui.fx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class SupplyGauge extends VBox {

    private final Label      supplyVal;
    private final Label      demandVal;
    private final Label      balanceLabel;
    private final ProgressBar balanceBar;
    private final Label      cycleLabel;

    public SupplyGauge() {
        setSpacing(8);
        setPadding(new Insets(14));
        getStyleClass().add("supply-gauge");

        Label title = new Label("⚡  Grid Supply / Demand");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        title.setTextFill(Color.web("#E8820C"));

        cycleLabel = new Label("Cycle: 0");
        cycleLabel.setFont(Font.font("Segoe UI", 11));
        cycleLabel.setTextFill(Color.web("#666666"));

        HBox row1 = makeRow("Supply:", Color.web("#333333"));
        supplyVal = (Label) row1.getChildren().get(1);

        HBox row2 = makeRow("Demand:", Color.web("#333333"));
        demandVal = (Label) row2.getChildren().get(1);

        balanceLabel = new Label("Balance: OK");
        balanceLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        balanceLabel.setTextFill(Color.web("#4CAF50"));

        balanceBar = new ProgressBar(0);
        balanceBar.setPrefWidth(340);
        balanceBar.setPrefHeight(18);

        getChildren().addAll(title, cycleLabel, row1, row2, balanceLabel, balanceBar);
    }

    private HBox makeRow(String labelText, Color color) {
        Label lbl = new Label(labelText);
        lbl.setFont(Font.font("Segoe UI", 12));
        lbl.setTextFill(Color.web("#cccccc"));
        lbl.setMinWidth(70);

        Label val = new Label("0 kW");
        val.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        val.setTextFill(Color.web("#333333"));

        HBox row = new HBox(8, lbl, val);
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(val, Priority.ALWAYS);
        return row;
    }

    public void update(double supplyKW, double demandKW, int cycle) {
        supplyVal.setText(String.format("%.0f kW", supplyKW));
        demandVal.setText(String.format("%.0f kW", demandKW));
        cycleLabel.setText("Cycle: " + cycle);

        double ratio = demandKW > 0 ? Math.min(1.0, supplyKW / demandKW) : 1.0;
        balanceBar.setProgress(ratio);

        if (ratio >= 0.95) {
            balanceLabel.setText("Balance: STABLE ✓");
            balanceLabel.setTextFill(Color.web("#4CAF50"));
            balanceBar.setStyle("-fx-accent: #4CAF50;");
        } else if (ratio >= 0.75) {
            balanceLabel.setText("Balance: STRAINED ⚠");
            balanceLabel.setTextFill(Color.web("#FF9800"));
            balanceBar.setStyle("-fx-accent: #FF9800;");
        } else {
            balanceLabel.setText("Balance: CRITICAL ✗");
            balanceLabel.setTextFill(Color.web("#F44336"));
            balanceBar.setStyle("-fx-accent: #F44336;");
        }
    }
}
