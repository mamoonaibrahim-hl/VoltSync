package voltsync.ui.fx;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import voltsync.exceptions.SectorNotFoundException;
import voltsync.grid.PowerGrid;
import voltsync.model.EmergencySector;
import voltsync.model.Sector;

public class AdminControlPanel extends VBox {

    private final PowerGrid  grid;
    private final AlertFeed  alertFeed;
    private boolean          authenticated = false;
    private static final String PASSWORD = "volt123";

    public AdminControlPanel(PowerGrid grid, AlertFeed alertFeed) {
        this.grid      = grid;
        this.alertFeed = alertFeed;
        setSpacing(10);
        setPadding(new Insets(12));
        getStyleClass().add("admin-panel");
        buildUI();
    }

    private void buildUI() {
        Label title = new Label("🔧  Admin Control Panel");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        title.setTextFill(Color.web("#E8820C"));

        
        PasswordField pwField = new PasswordField();
        pwField.setPromptText("Password...");
        pwField.setPrefWidth(120);

        Button authBtn = new Button("Unlock");
        authBtn.getStyleClass().add("btn-orange");
        Label authStatus = new Label("🔒 Locked");
        authStatus.setTextFill(Color.web("#F44336"));

        HBox authRow = new HBox(8, pwField, authBtn);
        authRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        HBox statusRow = new HBox(authStatus);
        statusRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        authBtn.setOnAction(e -> {
            if (PASSWORD.equals(pwField.getText())) {
                authenticated = true;
                authStatus.setText("🔓 Unlocked");
                authStatus.setTextFill(Color.web("#4CAF50"));
                pwField.clear();
                alertFeed.addInfo("Admin panel unlocked.");
            } else {
                authStatus.setText("❌ Wrong password");
                authStatus.setTextFill(Color.web("#F44336"));
            }
        });

        
        GridPane grid2 = new GridPane();
        grid2.setHgap(8); grid2.setVgap(6);

        TextField sectorNameField = styledField("Sector name...");
        TextField demandField     = styledField("New demand (kW)...");
        Button adjustBtn = new Button("Set Demand");
        adjustBtn.getStyleClass().add("btn-blue");

        grid2.add(new Label("Sector:"), 0, 0);  grid2.add(sectorNameField, 1, 0);
        grid2.add(new Label("Demand:"), 0, 1);  grid2.add(demandField, 1, 1);
        grid2.add(adjustBtn, 1, 2);

        styleGridLabels(grid2);

        adjustBtn.setOnAction(e -> {
            if (!checkAuth()) return;
            try {
                Sector s = grid.findSector(sectorNameField.getText().trim());
                double d = Double.parseDouble(demandField.getText().trim());
                s.adjustDemand(d, "Admin override via UI");
                alertFeed.addInfo("Demand for " + s.getName() + " set to " + d + " kW");
            } catch (SectorNotFoundException ex) {
                alertFeed.addWarning(ex.getMessage());
            } catch (NumberFormatException ex) {
                alertFeed.addWarning("Invalid demand value.");
            }
        });

        
        TextField toggleField = styledField("Sector name...");
        Button toggleBtn = new Button("Toggle On/Off");
        toggleBtn.getStyleClass().add("btn-blue");

        HBox toggleRow = new HBox(8, toggleField, toggleBtn);

        toggleBtn.setOnAction(e -> {
            if (!checkAuth()) return;
            try {
                Sector s = grid.findSector(toggleField.getText().trim());
                s.setActive(!s.isActive());
                alertFeed.addInfo(s.getName() + " → " + (s.isActive() ? "ONLINE" : "OFFLINE"));
            } catch (SectorNotFoundException ex) {
                alertFeed.addWarning(ex.getMessage());
            }
        });

        
        TextField lifeSupportField = styledField("Emergency sector name...");
        Button lifeSupportBtn = new Button("⚡ Activate Life Support");
        lifeSupportBtn.getStyleClass().add("btn-red");

        HBox lsRow = new HBox(8, lifeSupportField, lifeSupportBtn);

        lifeSupportBtn.setOnAction(e -> {
            if (!checkAuth()) return;
            try {
                Sector s = grid.findSector(lifeSupportField.getText().trim());
                if (s instanceof EmergencySector es) {
                    es.activateLifeSupport();
                    alertFeed.addError("LIFE SUPPORT activated: " + s.getName());
                } else {
                    alertFeed.addWarning(s.getName() + " is not an emergency sector.");
                }
            } catch (SectorNotFoundException ex) {
                alertFeed.addWarning(ex.getMessage());
            }
        });

        
        Button saveBtn = new Button("💾 Save State");
        saveBtn.getStyleClass().add("btn-orange");
        saveBtn.setOnAction(e -> {
            if (!checkAuth()) return;
            grid.saveState();
            alertFeed.addInfo("Grid state saved to disk.");
        });

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #d1d1d6;");

        getChildren().addAll(title, authRow, statusRow, sep,
            sectionLabel("Adjust Demand"), grid2,
            sectionLabel("Toggle Sector"), toggleRow,
            sectionLabel("Life Support"), lsRow,
            new Separator(), saveBtn);
    }

    private boolean checkAuth() {
        if (!authenticated) {
            alertFeed.addWarning("Unlock admin panel first.");
            return false;
        }
        return true;
    }

    private TextField styledField(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setPrefWidth(180);
        return f;
    }

    private Label sectionLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        l.setTextFill(Color.web("#333333"));
        l.setPadding(new Insets(4, 0, 0, 0));
        return l;
    }

    private void styleGridLabels(GridPane g) {
        g.getChildren().stream()
            .filter(n -> n instanceof Label)
            .forEach(n -> {
                ((Label) n).setTextFill(Color.web("#333333"));
                ((Label) n).setFont(Font.font("Segoe UI", 11));
            });
    }
}
