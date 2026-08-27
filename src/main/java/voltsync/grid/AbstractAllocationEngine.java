package voltsync.grid;

import voltsync.model.Sector;
import voltsync.exceptions.GridOverloadException;
import voltsync.logging.Loggable;

import java.util.List;

public abstract class AbstractAllocationEngine implements Schedulable {

    protected Loggable logger;           
    protected double overloadThreshold;  

    protected AbstractAllocationEngine(Loggable logger, double overloadThreshold) {
        this.logger             = logger;
        this.overloadThreshold  = overloadThreshold;
    }

    
    protected double computeTotalDemand(List<Sector> sectors) {
        return sectors.stream()
                      .filter(s -> s.isActive())
                      .mapToDouble(Sector::getDemandKW)
                      .sum();
    }

    
    @Override
    public abstract double allocate(List<Sector> sectors, double availableKW) throws GridOverloadException;

    
    protected void checkOverloadRisk(double demand, double supply) {
        if (supply > 0 && demand / supply > overloadThreshold) {
            logger.logWarning(String.format(
                "Overload risk! Demand %.1f kW vs Supply %.1f kW (%.0f%%)",
                demand, supply, (demand / supply) * 100));
        }
    }
}
