package voltsync.grid;

import voltsync.exceptions.GridOverloadException;
import voltsync.logging.Loggable;
import voltsync.model.EmergencySector;
import voltsync.model.Sector;
import voltsync.model.SectorPriority;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PriorityAllocationEngine extends AbstractAllocationEngine {

    public PriorityAllocationEngine(Loggable logger) {
        super(logger, 0.85);  
    }

    @Override
    public String getStrategyName() { return "Priority-Based Load Shedding"; }

    @Override
    public double allocate(List<Sector> sectors, double availableKW) throws GridOverloadException {
        double totalDemand = computeTotalDemand(sectors);

        
        if (totalDemand > availableKW * 1.5) {
            throw new GridOverloadException(totalDemand, availableKW);
        }

        checkOverloadRisk(totalDemand, availableKW);

        
        List<Sector> sorted = sectors.stream()
            .filter(Sector::isActive)
            .sorted(Comparator.comparingInt(s -> s.getPriority().getLevel()))
            .collect(Collectors.toList());

        double remaining = availableKW;
        double totalAllocated = 0;

        for (Sector sector : sorted) {
            double demand = sector.getDemandKW();

            
            if (sector instanceof EmergencySector es && es.requiresHardLock()) {
                
                sector.receiveAllocation(demand);
                totalAllocated += demand;
                remaining = Math.max(0, remaining - demand);
                logger.log(String.format("[LOCK] %s — %.1f kW (life support)", sector.getName(), demand));
                continue;
            }

            double give = Math.min(demand, remaining);
            sector.receiveAllocation(give);
            totalAllocated += give;
            remaining = Math.max(0, remaining - give);

            if (give < demand) {
                logger.logWarning(String.format("Load shed on %s: gave %.1f / %.1f kW",
                    sector.getName(), give, demand));
            }
        }

        logger.log(String.format("[Alloc] Distributed %.1f / %.1f kW | Unmet: %.1f kW",
            totalAllocated, totalDemand, totalDemand - totalAllocated));

        return totalAllocated;
    }
}
