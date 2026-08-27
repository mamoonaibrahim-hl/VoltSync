package voltsync.simulation;

import voltsync.grid.PowerGrid;
import voltsync.logging.Loggable;
import voltsync.model.PowerSource;

import java.util.List;

public class SupplyThread implements Runnable {

    private final PowerGrid        grid;
    private final List<PowerSource> sources;
    private final Loggable         logger;
    private final int              tickIntervalMs;

    private volatile boolean running = true;

    public SupplyThread(PowerGrid grid, List<PowerSource> sources,
                        Loggable logger, int tickIntervalMs) {
        this.grid           = grid;
        this.sources        = sources;
        this.logger         = logger;
        this.tickIntervalMs = tickIntervalMs;
    }

    @Override
    public void run() {
        logger.log("Supply simulation thread started [" + Thread.currentThread().getName() + "]");

        while (running && grid.isGridOnline()) {
            try {
                
                sources.forEach(PowerSource::simulateFluctuation);

                
                grid.refreshSupply();

                Thread.sleep(tickIntervalMs);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        logger.log("Supply thread stopped.");
    }

    public void stop() { this.running = false; }
}
