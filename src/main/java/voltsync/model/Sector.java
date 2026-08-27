package voltsync.model;

import java.io.Serializable;

public class Sector extends GridEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private final SectorPriority priority;
    private double demandKW;          
    private double allocatedKW;       
    private SectorMetrics metrics;    

    
    public Sector(String name, SectorPriority priority, double demandKW) {
        super(name);                  
        this.priority    = priority;
        this.demandKW    = demandKW;
        this.allocatedKW = 0.0;
        this.metrics     = new SectorMetrics(name);  
    }

    
    public Sector(String name, SectorPriority priority) {
        this(name, priority, 100.0);  
    }

    
    public Sector(Sector other) {
        super(other);
        this.priority    = other.priority;
        this.demandKW    = other.demandKW;
        this.allocatedKW = 0.0;
        this.metrics     = new SectorMetrics(other.getName() + "_copy");
    }

    
    @Override
    public String getEntityType() { return "Sector"; }

    @Override
    public String getStatusSummary() {
        double pct = demandKW > 0 ? (allocatedKW / demandKW) * 100 : 0;
        return String.format("%-22s | Priority: %-8s | Demand: %6.1f kW | Allocated: %6.1f kW | %.0f%%",
                getName(), priority.colored(), demandKW, allocatedKW, pct);
    }

    
    public void adjustDemand(double newDemandKW) {
        this.demandKW = Math.max(0, newDemandKW);
    }

    public void adjustDemand(double newDemandKW, String reason) {
        this.demandKW = Math.max(0, newDemandKW);
        metrics.logEvent("Demand adjusted to " + newDemandKW + " kW: " + reason);
    }

    public void receiveAllocation(double kw) {
        this.allocatedKW = kw;
        metrics.recordAllocation(kw, demandKW);
    }

    public boolean isUnderserved() {
        return allocatedKW < demandKW * 0.9;
    }

    
    public SectorPriority getPriority()     { return priority; }
    public double         getDemandKW()     { return demandKW; }
    public double         getAllocatedKW()  { return allocatedKW; }
    public SectorMetrics  getMetrics()      { return metrics; }
}
