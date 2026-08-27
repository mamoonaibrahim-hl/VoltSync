package voltsync.ui;

import voltsync.exceptions.SectorNotFoundException;
import voltsync.grid.PowerGrid;
import voltsync.logging.Loggable;
import voltsync.model.EmergencySector;
import voltsync.model.Sector;

import java.util.Scanner;

public class AdminPanel {

    private final PowerGrid grid;
    private final Loggable  logger;
    private final Scanner   scanner;

    
    private static final String ADMIN_PASSWORD = "volt123";
    private boolean authenticated = false;

    public AdminPanel(PowerGrid grid, Loggable logger) {
        this.grid    = grid;
        this.logger  = logger;
        this.scanner = new Scanner(System.in);
    }

    public void launch() {
        if (!authenticate()) {
            System.out.println("Access denied.");
            return;
        }

        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();

            
            switch (choice) {
                case "1" -> printGridStatus();
                case "2" -> adjustSectorDemand();
                case "3" -> toggleSector();
                case "4" -> activateLifeSupport();
                case "5" -> saveAndExit();
                case "6" -> { running = false; }
                default  -> System.out.println("Invalid option.");
            }
        }
    }

    private boolean authenticate() {
        System.out.print("\n[VoltSync Admin] Enter password: ");
        String input = scanner.nextLine().trim();
        authenticated = ADMIN_PASSWORD.equals(input);
        if (authenticated) logger.log("Admin panel accessed.");
        return authenticated;
    }

    private void printMenu() {
        System.out.println("\n╔═══════════════════════════════╗");
        System.out.println("║   VoltSync Admin Control      ║");
        System.out.println("╠═══════════════════════════════╣");
        System.out.println("║ 1. View Grid Status           ║");
        System.out.println("║ 2. Adjust Sector Demand       ║");
        System.out.println("║ 3. Toggle Sector On/Off       ║");
        System.out.println("║ 4. Activate Life Support      ║");
        System.out.println("║ 5. Save State & Exit          ║");
        System.out.println("║ 6. Exit Without Saving        ║");
        System.out.println("╚═══════════════════════════════╝");
    }

    private void printGridStatus() {
        grid.printDashboard();
    }

    private void adjustSectorDemand() {
        System.out.print("Sector name: ");
        String name = scanner.nextLine().trim();
        System.out.print("New demand (kW): ");

        
        try {
            double demand = Double.parseDouble(scanner.nextLine().trim());
            Sector s = grid.findSector(name);
            s.adjustDemand(demand, "Admin override");
            logger.log("Admin: demand for " + name + " set to " + demand + " kW");
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
        } catch (SectorNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    private void toggleSector() {
        System.out.print("Sector name: ");
        String name = scanner.nextLine().trim();
        try {
            Sector s = grid.findSector(name);
            s.setActive(!s.isActive());
            System.out.println(name + " is now " + (s.isActive() ? "ONLINE" : "OFFLINE"));
            logger.logWarning("Admin toggled sector: " + name + " -> " + (s.isActive() ? "ON" : "OFF"));
        } catch (SectorNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    private void activateLifeSupport() {
        System.out.print("Emergency sector name: ");
        String name = scanner.nextLine().trim();
        try {
            Sector s = grid.findSector(name);
            
            if (s instanceof EmergencySector es) {
                es.activateLifeSupport();
                logger.logWarning("LIFE SUPPORT activated for: " + name);
            } else {
                System.out.println(name + " is not an emergency sector.");
            }
        } catch (SectorNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    private void saveAndExit() {
        grid.saveState();
        System.out.println("State saved. Exiting admin panel.");
    }
}
