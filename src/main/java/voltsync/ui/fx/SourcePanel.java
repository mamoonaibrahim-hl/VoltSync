package voltsync.ui.fx;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.LinkedHashMap;
import java.util.Map;

public class SourcePanel extends VBox {

    private final Map<String, SourceRow> rows = new LinkedHashMap<>();

    public SourcePanel() {
        setSpacing(6);
        setPadding(new Insets(12));
        getStyleClass().add("source-panel");

        Label title = new Label("🔋  Power Sources");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        title.setTextFill(Color.web("#E8820C"));
        getChildren().add(title);
    }

    public void ensureSource(String name, double maxKW) {
        if (!rows.containsKey(name)) {
            SourceRow row = new SourceRow(name, maxKW);
            rows.put(name, row);
            getChildren().add(row);
        }
    }

    public void updateSource(String name, double outputKW, double maxKW) {
        ensureSource(name, maxKW);
        rows.get(name).update(outputKW, maxKW);
    }

    
    private static class SourceRow extends HBox {
        private final ProgressBar bar;
        private final Label       valueLabel;
        private final double      maxKW;

        SourceRow(String name, double maxKW) {
            this.maxKW = maxKW;
            setSpacing(8);
            setPadding(new Insets(2, 0, 2, 0));

            Label nameLbl = new Label(name);
            nameLbl.setFont(Font.font("Segoe UI", 11));
            nameLbl.setTextFill(Color.web("#333333"));
            nameLbl.setMinWidth(80);
            nameLbl.setMaxWidth(120);

            bar = new ProgressBar(0);
            bar.setPrefWidth(90);
            bar.setMaxWidth(Double.MAX_VALUE);
            bar.setPrefHeight(12);
            HBox.setHgrow(bar, Priority.ALWAYS);

            valueLabel = new Label("0 kW");
            valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
            valueLabel.setTextFill(Color.web("#333333"));

            getChildren().addAll(nameLbl, bar, valueLabel);
        }

        void update(double outputKW, double maxKW) {
            double pct = maxKW > 0 ? outputKW / maxKW : 0;
            bar.setProgress(Math.min(1.0, pct));
            valueLabel.setText(String.format("%.0f kW", outputKW));

            String color = pct >= 0.7 ? "#4CAF50" : pct >= 0.4 ? "#FF9800" : "#F44336";
            bar.setStyle("-fx-accent: " + color + ";");
        }
    }
}
