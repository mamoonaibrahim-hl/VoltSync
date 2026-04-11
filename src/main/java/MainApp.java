import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.*;
import util.*;

public class MainApp extends Application {

    private PowerGrid grid;
    private Label demandLabel;
    private Label supplyLabel;
    private Label statusLabel;
    private VBox sectorBox;
private ResidentialSector restoredResidential;

    @Override
    public void start(Stage primaryStage) {
        FileLogger.init();

        ConfigLoader config = new ConfigLoader();
        grid = new PowerGrid(config.getInitialPower());

        HospitalSector hospital = new HospitalSector("City General Hospital");
        ResidentialSector residential = new ResidentialSector("North Residential");
        IndustrialSector industrial = new IndustrialSector("East Industrial Zone");

        grid.addSector(hospital);
        grid.addSector(residential);
        grid.addSector(industrial);

        SolarSource solar = new SolarSource();
        WindSource wind = new WindSource();
        NuclearSource nuclear = new NuclearSource();

        grid.addSource(solar);
        grid.addSource(wind);
        grid.addSource(nuclear);

        demandLabel = new Label("Total Demand: 0.0 MW");
        demandLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        supplyLabel = new Label("Total Supply: 0.0 MW");
        supplyLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: green;");

        statusLabel = new Label("Status: NORMAL");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: green;");

        sectorBox = new VBox(5);
        for (Sector s : grid.getSectors()) {
            Label lbl = new Label(s.getSectorID() + " [P" + s.getSectorPriority() + "]: 0.0 MW");
            lbl.setStyle("-fx-padding: 5; -fx-border-color: gray; -fx-border-radius: 4;");
            s.loadProperty().addListener((obs, oldVal, newVal) ->
                Platform.runLater(() ->
                    lbl.setText(s.getSectorID() + " [P" + s.getSectorPriority() + "]: "
                        + String.format("%.1f", newVal.doubleValue()) + " MW")
                )
            );
            sectorBox.getChildren().add(lbl);
        }

        VBox sourceBox = new VBox(5);
        for (SupplySource src : grid.getSources()) {
            Label lbl = new Label(src.getSourceName() + ": 0.0 MW");
            lbl.setStyle("-fx-padding: 5; -fx-border-color: blue; -fx-border-radius: 4;");
            src.outputProperty().addListener((obs, oldVal, newVal) ->
                Platform.runLater(() ->
                    lbl.setText(src.getSourceName() + ": " + String.format("%.1f", newVal.doubleValue()) + " MW")
                )
            );
            sourceBox.getChildren().add(lbl);
        }

        Button checkBtn = new Button("Check Grid Status");
        checkBtn.setOnAction(e -> checkGrid());

       Button shedBtn = new Button("Shed Residential Load");
        shedBtn.setStyle("-fx-background-color: orange;");

        Button restoreBtn = new Button("Restore Residential Load");
        restoreBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white;");
        restoreBtn.setDisable(true);

        shedBtn.setOnAction(e -> {
            grid.shedLoad();
            FileLogger.log("Manual load shedding triggered.");
            statusLabel.setText("Status: LOAD SHED");
            statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: orange;");
            shedBtn.setDisable(true);
            restoreBtn.setDisable(false);
        });

        restoreBtn.setOnAction(e -> {
            ResidentialSector newResidential = new ResidentialSector("North Residential");
            grid.addSector(newResidential);
            Label lbl = new Label(newResidential.getSectorID() + " [P" + newResidential.getSectorPriority() + "]: 0.0 MW");
            lbl.setStyle("-fx-padding: 5; -fx-border-color: gray; -fx-border-radius: 4;");
            newResidential.loadProperty().addListener((obs, oldVal, newVal) ->
                Platform.runLater(() ->
                    lbl.setText(newResidential.getSectorID() + " [P" + newResidential.getSectorPriority() + "]: "
                        + String.format("%.1f", newVal.doubleValue()) + " MW")
                )
            );
            sectorBox.getChildren().add(lbl);
            newResidential.setDaemon(true);
            newResidential.start();
            FileLogger.log("Residential load restored.");
            statusLabel.setText("Status: RESTORED ✅");
            statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: green;");
            restoreBtn.setDisable(true);
            shedBtn.setDisable(false);
        });

        VBox root = new VBox(10,
            new Label("⚡ VoltSync Pro Dashboard"),
            new Separator(),
            new Label("--- Supply Sources ---"),
            sourceBox,
            supplyLabel,
            new Separator(),
            new Label("--- Sector Demand ---"),
            sectorBox,
            demandLabel,
            new Separator(),
            statusLabel,
            new HBox(10, checkBtn, shedBtn, restoreBtn)
        );
        root.setPadding(new Insets(15));

        hospital.setDaemon(true); hospital.start();
        residential.setDaemon(true); residential.start();
        industrial.setDaemon(true); industrial.start();
        solar.setDaemon(true); solar.start();
        wind.setDaemon(true); wind.start();
        nuclear.setDaemon(true); nuclear.start();

        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(2), ev -> checkGrid())
        );
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timeline.play();

        primaryStage.setOnCloseRequest(e -> FileLogger.close());

        Scene scene = new Scene(root, 420, 560);
scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("VoltSync Pro");
        primaryStage.show();
    }

    private void checkGrid() {
        double demand = grid.getTotalDemand();
        double supply = grid.getTotalAvailablePower();
        Platform.runLater(() -> {
            demandLabel.setText("Total Demand: " + String.format("%.1f", demand) + " MW");
            supplyLabel.setText("Total Supply: " + String.format("%.1f", supply) + " MW");
        });
        try {
            grid.allocatePower();
            Platform.runLater(() -> {
                statusLabel.setText("Status: NORMAL ✅");
                statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: green;");
            });
        } catch (GridOverloadException ex) {
            FileLogger.log("OVERLOAD: " + ex.getMessage());
            Platform.runLater(() -> {
                statusLabel.setText("Status: OVERLOAD ⚠️");
                statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: red;");
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Grid Overload!");
                alert.setHeaderText("Power Overload Detected");
                alert.setContentText(ex.getMessage());
                alert.show();
            });
        }
    }

    public static void main(String[] args) { launch(args); }
}