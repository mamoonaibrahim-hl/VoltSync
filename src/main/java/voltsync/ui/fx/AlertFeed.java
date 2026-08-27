package voltsync.ui.fx;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AlertFeed extends VBox {

    private final VBox         logBox;
    private final ScrollPane   scroll;
    private static final int   MAX_ENTRIES = 80;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss");

    public AlertFeed() {
        Label title = new Label("📋  Live Event Log");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        title.setTextFill(Color.web("#E8820C"));
        title.setPadding(new Insets(0, 0, 6, 0));

        logBox = new VBox(3);
        logBox.setPadding(new Insets(6));

        scroll = new ScrollPane(logBox);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(160);
        scroll.setStyle("-fx-background: #f5f5f8; -fx-background-color: #f5f5f8;");

        setSpacing(4);
        setPadding(new Insets(10));
        getStyleClass().add("alert-feed");
        getChildren().addAll(title, scroll);
    }

    public void addInfo(String message) {
        addEntry("INFO", message, "#4CAF50");
    }

    public void addWarning(String message) {
        addEntry("WARN", message, "#FF9800");
    }

    public void addError(String message) {
        addEntry("ERROR", message, "#F44336");
    }

    private void addEntry(String level, String message, String colorHex) {
        String time = LocalTime.now().format(fmt);
        Label entry = new Label(String.format("[%s] [%-5s] %s", time, level, message));
        entry.setFont(Font.font("Consolas", 11));
        entry.setTextFill(Color.web(colorHex));
        entry.setWrapText(true);

        logBox.getChildren().add(0, entry);  

        
        while (logBox.getChildren().size() > MAX_ENTRIES)
            logBox.getChildren().remove(logBox.getChildren().size() - 1);
    }
}
