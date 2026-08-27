package voltsync.ui.fx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import voltsync.model.Sector;
import voltsync.model.SectorPriority;

public class SectorCard extends VBox {

    private final Label       nameLabel;
    private final Label       priorityLabel;
    private final Label       demandLabel;
    private final Label       allocLabel;
    private final ProgressBar loadBar;
    private final Label       percentLabel;
    private final Label       statusLabel;

    public SectorCard(Sector sector) {
        
        setSpacing(6);
        setPadding(new Insets(12));
        setPrefWidth(210);
        setAlignment(Pos.TOP_LEFT);
        getStyleClass().add("sector-card");

        
        nameLabel = new Label(sector.getName());
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        nameLabel.setWrapText(true);

        
        priorityLabel = new Label("● " + sector.getPriority().getLabel());
        priorityLabel.setFont(Font.font("Segoe UI", 11));
        priorityLabel.setTextFill(getColorForPriority(sector.getPriority()));

        
        loadBar = new ProgressBar(0);
        loadBar.setPrefWidth(180);
        loadBar.setPrefHeight(14);

        
        demandLabel  = new Label("Demand:    0 kW");
        allocLabel   = new Label("Allocated: 0 kW");
        percentLabel = new Label("0%");
        percentLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        statusLabel = new Label("ONLINE");
        statusLabel.setFont(Font.font("Segoe UI", 10));
        statusLabel.setTextFill(Color.web("#4CAF50"));

        for (Label l : new Label[]{demandLabel, allocLabel}) {
            l.setFont(Font.font("Segoe UI", 11));
            l.setTextFill(Color.web("#333333"));
        }

        getChildren().addAll(nameLabel, priorityLabel, loadBar,
                             percentLabel, demandLabel, allocLabel, statusLabel);
        updateData(sector);
    }

    
    public void updateData(Sector sector) {
        double demand = sector.getDemandKW();
        double alloc  = sector.getAllocatedKW();
        double pct    = demand > 0 ? alloc / demand : 1.0;

        demandLabel.setText(String.format("Demand:    %.0f kW", demand));
        allocLabel.setText( String.format("Allocated: %.0f kW", alloc));
        percentLabel.setText(String.format("%.0f%%", pct * 100));

        loadBar.setProgress(Math.min(1.0, pct));

        
        String barColor;
        if (pct >= 0.9)      barColor = "#4CAF50";  
        else if (pct >= 0.6) barColor = "#FF9800";  
        else                 barColor = "#F44336";  

        loadBar.setStyle("-fx-accent: " + barColor + ";");
        percentLabel.setTextFill(Color.web(barColor));

        boolean active = sector.isActive();
        statusLabel.setText(active ? "ONLINE" : "OFFLINE");
        statusLabel.setTextFill(Color.web(active ? "#4CAF50" : "#F44336"));

        
        if (sector.isUnderserved()) {
            setStyle("-fx-border-color: #FF5722; -fx-border-width: 2;");
        } else {
            setStyle("-fx-border-color: transparent;");
        }
    }

    
    private Color getColorForPriority(SectorPriority p) {
        return switch (p) {
            case CRITICAL -> Color.web("#F44336");
            case HIGH     -> Color.web("#FF9800");
            case MEDIUM   -> Color.web("#2196F3");
            case LOW      -> Color.web("#4CAF50");
        };
    }
}
