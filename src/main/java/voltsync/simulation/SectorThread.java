package voltsync.simulation;

import voltsync.grid.PowerGrid;
import voltsync.logging.Loggable;
import voltsync.model.Sector;

import java.util.Random;

public class SectorThread implements Runnable {

    private final Sector    sector;
    private final PowerGrid grid;
    private final Loggable  logger;
    private final int       tickIntervalMs;  
    private final Random    rand = new Random();

    private volatile boolean running = true;  

    public SectorThread(Sector sector, PowerGrid grid, Loggable logger, int tickIntervalMs) {
        this.sector         = sector;
        this.grid           = grid;
        this.logger         = logger;
        this.tickIntervalMs = tickIntervalMs;
    }

    @Override
    public void run() {
        logger.log("Thread started for sector: " + sector.getName()
            + " [" + Thread.currentThread().getName() + "]");

        while (running && grid.isGridOnline()) {
            try {
                
                fluctuateDemand();

                
                Thread.sleep(tickIntervalMs);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.logWarning("Sector thread interrupted: " + sector.getName());
                break;
            }
        }

        logger.log("Thread stopped for sector: " + sector.getName());
    }

    
    private void fluctuateDemand() {
        double currentDemand = sector.getDemandKW();
        double change = (rand.nextDouble() * 0.30 - 0.15);  
        double newDemand = currentDemand * (1 + change);

        
        newDemand = Math.max(sector.getDemandKW() * 0.20,
                    Math.min(sector.getDemandKW() * 1.50, newDemand));

        sector.adjustDemand(newDemand, "Auto-fluctuation");
    }

    public void stop() {
        this.running = false;
    }

    public Sector getSector() { return sector; }
}
