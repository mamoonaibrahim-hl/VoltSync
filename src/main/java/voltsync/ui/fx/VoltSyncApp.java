package voltsync.ui.fx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import voltsync.analytics.AnalyticsDashboard;
import voltsync.grid.PowerGrid;
import voltsync.grid.PriorityAllocationEngine;
import voltsync.logging.ConsoleLogger;
import voltsync.logging.FileLogger;
import voltsync.logging.Loggable;
import voltsync.model.*;
import voltsync.simulation.SectorThread;
import voltsync.simulation.SupplyThread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoltSyncApp extends Application implements GridObserver {

    
    private PowerGrid          grid;
    private AnalyticsDashboard analytics;
    private List<SectorThread> sectorRunnables;
    private SupplyThread       supplyRunnable;
    private Thread             simThread;

    
    private final Map<String, SectorCard> sectorCards = new HashMap<>();
    private FlowPane      cardPane;
    private SupplyGauge   supplyGauge;
    private AlertFeed     alertFeed;
    private SourcePanel   sourcePanel;

    
    @Override
    public void start(Stage stage) {
        
        initBackend();

        
        BorderPane root = buildLayout();

        Scene scene = new Scene(root, 1280, 780);
        scene.setFill(Color.web("#87CEEB"));
        applyStyles(scene);

        stage.setTitle("VoltSync Pro — Urban Grid Management System");
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> shutdown());
        stage.show();

        
        grid.addObserver(this);

        
        startSimulation();
    }

    
    private void initBackend() {
        Loggable logger = buildCompositeLogger();

        grid = new PowerGrid("City Central Grid",
                new PriorityAllocationEngine(logger), logger);

        
        grid.registerSector(new EmergencySector("Central Hospital",   "HOSPITAL",  280.0));
        grid.registerSector(new EmergencySector("Fire Station Alpha",  "FIRE",       90.0));
        grid.registerSector(new Sector("Industrial Zone A",  SectorPriority.HIGH,   500.0));
        grid.registerSector(new Sector("Water Treatment",    SectorPriority.HIGH,   180.0));
        grid.registerSector(new Sector("Downtown Commercial",SectorPriority.MEDIUM, 350.0));
        grid.registerSector(new Sector("Residential North",  SectorPriority.LOW,    400.0));
        grid.registerSector(new Sector("Residential South",  SectorPriority.LOW,    380.0));

        
        List<PowerSource> sources = List.of(
            new SolarSource("Solar Farm Alpha", 600.0),
            new SolarSource("Solar Farm Beta",  400.0),
            new WindSource( "Wind Park North",  500.0),
            new CoalSource( "Coal Plant Base",  800.0)
        );
        sources.forEach(grid::addPowerSource);
        grid.refreshSupply();

        analytics = new AnalyticsDashboard(grid.getSectors());

        
        sectorRunnables = new ArrayList<>();
        int[] delays = {700, 900, 600, 1100, 800, 1200, 950};
        List<Sector> allSectors = new ArrayList<>(grid.getSectors());
        for (int i = 0; i < allSectors.size(); i++) {
            sectorRunnables.add(new SectorThread(allSectors.get(i), grid, logger, delays[i]));
        }

        
        supplyRunnable = new SupplyThread(grid, new ArrayList<>(grid.getSources()), logger, 1500);
    }

    private Loggable buildCompositeLogger() {
        Loggable console = new ConsoleLogger();
        FileLogger file  = new FileLogger("voltsync.log");
        return new Loggable() {
            @Override public void log(String m)        { console.log(m);        file.log(m); }
            @Override public void logWarning(String m) { console.logWarning(m); file.logWarning(m); }
            @Override public void logError(String m)   { console.logError(m);   file.logError(m); }
            @Override public void flush()              { file.flush(); }
        };
    }

    
    private BorderPane buildLayout() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #87CEEB;");

        
        HBox topBar = new HBox();
        topBar.setStyle("-fx-background-color: #B0E0E6; -fx-padding: 12 20;");
        topBar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        topBar.setSpacing(20);

        Label appTitle = new Label("⚡ VoltSync Pro");
        appTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        appTitle.setTextFill(Color.web("#E8820C"));

        Label subtitle = new Label("Urban Grid Management & Analytics System");
        subtitle.setFont(Font.font("Segoe UI", 13));
        subtitle.setTextFill(Color.web("#333333"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        supplyGauge = new SupplyGauge();

        topBar.getChildren().addAll(appTitle, subtitle, spacer, supplyGauge);
        root.setTop(topBar);

        
        cardPane = new FlowPane();
        cardPane.setHgap(12); cardPane.setVgap(12);
        cardPane.setPadding(new Insets(14));
        cardPane.setStyle("-fx-background-color: #E8F6FF;");

        
        for (Sector s : grid.getSectors()) {
            SectorCard card = new SectorCard(s);
            sectorCards.put(s.getName(), card);
            cardPane.getChildren().add(card);
        }

        ScrollPane centerScroll = new ScrollPane(cardPane);
        centerScroll.setFitToWidth(true);
        centerScroll.setStyle("-fx-background: #E8F6FF; -fx-background-color: #E8F6FF;");

        
        sourcePanel = new SourcePanel();
        
        grid.getSources().forEach(src ->
            sourcePanel.ensureSource(src.getName(), src.getMaxCapacityKW()));

        alertFeed = new AlertFeed();
        alertFeed.addInfo("VoltSync Pro started. Simulation running.");

        AdminControlPanel adminPanel = new AdminControlPanel(grid, alertFeed);
        ScrollPane adminScroll = new ScrollPane(adminPanel);
        adminScroll.setFitToWidth(true);
        adminScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        adminScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        adminScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        SplitPane rightSplit = new SplitPane();
        rightSplit.setOrientation(Orientation.VERTICAL);
        rightSplit.getItems().addAll(sourcePanel, adminScroll);
        rightSplit.setDividerPositions(0.35);
        rightSplit.setPrefWidth(260);
        rightSplit.setStyle("-fx-background-color: #d6efff;");

        
        VBox bottomBar = new VBox(alertFeed);
        bottomBar.setStyle("-fx-background-color: #d6efff; -fx-padding: 4;");

        
        root.setCenter(centerScroll);
        root.setRight(rightSplit);
        root.setBottom(bottomBar);

        return root;
    }

    
    private void startSimulation() {
        
        sectorRunnables.forEach(sr -> {
            Thread t = new Thread(sr, "Sector-" + sr.getSector().getName());
            t.setDaemon(true);
            t.start();
        });

        Thread st = new Thread(supplyRunnable, "SupplyThread");
        st.setDaemon(true);
        st.start();

        
        simThread = new Thread(() -> {
            int cycle = 0;
            while (grid.isGridOnline()) {
                try {
                    Thread.sleep(2000);
                    grid.runAllocationCycle();
                    double demand = grid.getSectors().stream()
                                       .mapToDouble(Sector::getDemandKW).sum();
                    analytics.recordCycle(grid.getTotalSupplyKW(), demand);
                    cycle++;

                    
                    if (cycle == 5) {
                        grid.getSectors().stream()
                            .filter(s -> s instanceof EmergencySector)
                            .map(s -> (EmergencySector) s)
                            .findFirst()
                            .ifPresent(es -> {
                                es.activateLifeSupport();
                                Platform.runLater(() ->
                                    alertFeed.addError("LIFE SUPPORT activated: " + es.getName()));
                            });
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "SimLoop");
        simThread.setDaemon(true);
        simThread.start();
    }

    

    @Override
    public void onCycleComplete(List<Sector> sectors, double supplyKW,
                                double totalDemandKW, int cycleNumber) {
        Platform.runLater(() -> {
            
            supplyGauge.update(supplyKW, totalDemandKW, cycleNumber);

            
            for (Sector s : sectors) {
                SectorCard card = sectorCards.get(s.getName());
                if (card != null) card.updateData(s);
            }

            alertFeed.addInfo(String.format("Cycle #%d — Supply: %.0f kW | Demand: %.0f kW",
                cycleNumber, supplyKW, totalDemandKW));
        });
    }

    @Override
    public void onAlert(String level, String message) {
        Platform.runLater(() -> {
            if ("ERROR".equals(level))     alertFeed.addError(message);
            else if ("WARN".equals(level)) alertFeed.addWarning(message);
            else                           alertFeed.addInfo(message);
        });
    }

    @Override
    public void onSupplyChanged(String sourceName, double outputKW, double maxKW) {
        Platform.runLater(() -> sourcePanel.updateSource(sourceName, outputKW, maxKW));
    }

    
    private void shutdown() {
        grid.shutdown();
        sectorRunnables.forEach(SectorThread::stop);
        supplyRunnable.stop();
        grid.saveState();
        analytics.saveReportToFile("session_report.txt");
        System.out.println("VoltSync Pro shutdown complete.");
    }

    
    private void applyStyles(Scene scene) {
        String css = """
            .sector-card {
                -fx-background-color: #ffffff;
                -fx-background-radius: 8;
                -fx-border-radius: 8;
                -fx-border-color: #d1d1d6;
                -fx-border-width: 1;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0, 0, 2);
            }
            .supply-gauge {
                -fx-background-color: #f2f2f2;
            }
            .source-panel {
                -fx-background-color: #f5f5f8;
            }
            .alert-feed {
                -fx-background-color: #f5f5f8;
            }
            .admin-panel {
                -fx-background-color: #f5f5f8;
            }
            .btn-orange {
                -fx-background-color: #E8820C;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-background-radius: 4;
                -fx-cursor: hand;
            }
            .btn-orange:hover { -fx-background-color: #d4730a; }
            .btn-blue {
                -fx-background-color: #2196F3;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-background-radius: 4;
                -fx-cursor: hand;
            }
            .btn-blue:hover { -fx-background-color: #1976D2; }
            .btn-red {
                -fx-background-color: #F44336;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-background-radius: 4;
                -fx-cursor: hand;
            }
            .btn-red:hover { -fx-background-color: #d32f2f; }
            .text-field, .password-field {
                -fx-background-color: #ffffff;
                -fx-text-fill: #111111;
                -fx-prompt-text-fill: #666666;
                -fx-background-radius: 4;
                -fx-border-color: #c8c8cf;
                -fx-border-radius: 4;
            }
            .progress-bar .track { -fx-background-color: #e0e0e5; -fx-background-radius: 3; }
            .progress-bar .bar   { -fx-background-radius: 3; }
            .split-pane-divider  { -fx-background-color: #d1d1d6; }
            .scroll-bar          { -fx-background-color: #e8e8ed; }
            .scroll-bar .thumb   { -fx-background-color: #c1c1c7; }
            .separator .line     { -fx-border-color: #d1d1d6; -fx-border-width: 1; }
            """;

        scene.getStylesheets().add("data:text/css," +
            css.replace("\n", " ").replace("\"", "\\\""));
    }
}
