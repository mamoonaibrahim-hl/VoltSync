package voltsync.persistence;

import voltsync.model.Sector;
import voltsync.model.PowerSource;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class GridState implements Serializable {

    private static final long serialVersionUID = 1L;

    private final LocalDateTime savedAt;
    private final List<Sector>  sectors;
    private final double        totalSupplyKW;
    private final int           cycleCount;
    private final List<String>  eventHistory;

    public GridState(List<Sector> sectors, double totalSupplyKW, int cycleCount, List<String> events) {
        this.savedAt       = LocalDateTime.now();
        this.sectors       = new ArrayList<>(sectors);
        this.totalSupplyKW = totalSupplyKW;
        this.cycleCount    = cycleCount;
        this.eventHistory  = new ArrayList<>(events);
    }

    public LocalDateTime getSavedAt()       { return savedAt; }
    public List<Sector>  getSectors()       { return sectors; }
    public double        getTotalSupplyKW() { return totalSupplyKW; }
    public int           getCycleCount()    { return cycleCount; }
    public List<String>  getEventHistory()  { return eventHistory; }
}
