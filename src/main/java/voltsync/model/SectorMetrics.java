package voltsync.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SectorMetrics implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String sectorName;
    private int     totalAllocations;
    private double  totalKWReceived;
    private double  totalKWDemanded;
    private int     shortfallCount;        
    private final List<String> eventLog;   

    public SectorMetrics(String sectorName) {
        this.sectorName       = sectorName;
        this.totalAllocations = 0;
        this.totalKWReceived  = 0;
        this.totalKWDemanded  = 0;
        this.shortfallCount   = 0;
        this.eventLog         = new ArrayList<>();
    }

    public void recordAllocation(double received, double demanded) {
        totalAllocations++;
        totalKWReceived += received;
        totalKWDemanded += demanded;
        if (received < demanded * 0.9) shortfallCount++;
    }

    public void logEvent(String event) {
        eventLog.add(event);
    }

    public double getEfficiencyPercent() {
        return totalKWDemanded == 0 ? 100.0 : (totalKWReceived / totalKWDemanded) * 100.0;
    }

    public String getSummaryReport() {
        return String.format(
            "  [%s] Allocations: %d | Avg Received: %.1f kW | Efficiency: %.1f%% | Shortfalls: %d",
            sectorName, totalAllocations,
            totalAllocations > 0 ? totalKWReceived / totalAllocations : 0,
            getEfficiencyPercent(), shortfallCount
        );
    }

    public int    getTotalAllocations()   { return totalAllocations; }
    public double getTotalKWReceived()    { return totalKWReceived; }
    public double getTotalKWDemanded()    { return totalKWDemanded; }
    public int    getShortfallCount()     { return shortfallCount; }
    public List<String> getEventLog()     { return eventLog; }
}
