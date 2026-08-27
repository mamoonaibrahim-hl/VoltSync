package voltsync.grid;

import voltsync.exceptions.GridOverloadException;
import voltsync.exceptions.InvalidSupplyException;
import voltsync.exceptions.SectorNotFoundException;
import voltsync.logging.Loggable;
import voltsync.model.PowerSource;
import voltsync.model.Sector;
import voltsync.persistence.GridPersistence;
import voltsync.persistence.GridState;
import voltsync.ui.fx.GridObserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class PowerGrid {
    private final String gridName;
    private static int instanceCount = 0;
    private final List<Sector>       sectors;
    private final List<PowerSource>  sources;
    private final Schedulable        engine;
    private final Loggable           logger;
    private final List<String>       eventHistory;
    private final List<GridObserver> observers = new CopyOnWriteArrayList<>();
    private double  totalSupplyKW = 0;
    private int     cycleCount    = 0;
    private boolean gridOnline    = true;
    private final ReentrantLock lock = new ReentrantLock(true);

    public PowerGrid(String gridName, Schedulable engine, Loggable logger) {
        this.gridName     = gridName;
        this.engine       = engine;
        this.logger       = logger;
        this.sectors      = new ArrayList<>();
        this.sources      = new ArrayList<>();
        this.eventHistory = Collections.synchronizedList(new ArrayList<>());
        instanceCount++;
    }

    public void addObserver(GridObserver o)    { observers.add(o); }
    public void removeObserver(GridObserver o) { observers.remove(o); }

    private void notifyCycle() {
        List<Sector> snap = new ArrayList<>(sectors);
        double demand = snap.stream().mapToDouble(Sector::getDemandKW).sum();
        for (GridObserver o : observers)
            o.onCycleComplete(snap, totalSupplyKW, demand, cycleCount);
    }
    private void notifyAlert(String level, String msg) {
        for (GridObserver o : observers) o.onAlert(level, msg);
    }
    public void notifySupplyChanged(String name, double out, double max) {
        for (GridObserver o : observers) o.onSupplyChanged(name, out, max);
    }

    public void registerSector(Sector sector) {
        lock.lock();
        try {
            sectors.add(sector);
            logger.log("Sector registered: " + sector.getName() + " [" + sector.getPriority().getLabel() + "]");
        } finally { lock.unlock(); }
    }

    public Sector findSector(String name) throws SectorNotFoundException {
        return sectors.stream().filter(s -> s.getName().equalsIgnoreCase(name))
            .findFirst().orElseThrow(() -> new SectorNotFoundException(name));
    }

    public void addPowerSource(PowerSource source) {
        if (source.getCurrentOutputKW() < 0)
            throw new InvalidSupplyException(source.getName(), source.getCurrentOutputKW());
        lock.lock();
        try { sources.add(source); logger.log("Source added: " + source.getName()); }
        finally { lock.unlock(); }
    }

    public void refreshSupply() {
        lock.lock();
        try {
            double s = sources.stream().mapToDouble(PowerSource::getCurrentOutputKW).sum();
            if (s < 0) throw new InvalidSupplyException("Grid", s);
            this.totalSupplyKW = s;
            for (PowerSource src : sources)
                notifySupplyChanged(src.getName(), src.getCurrentOutputKW(), src.getMaxCapacityKW());
        } catch (InvalidSupplyException e) { logger.logError(e.getMessage()); }
        finally { lock.unlock(); }
    }

    public void runAllocationCycle() {
        lock.lock();
        try {
            cycleCount++;
            logger.log("Cycle #" + cycleCount + " | Supply: " + String.format("%.1f", totalSupplyKW) + " kW");
            try {
                engine.allocate(sectors, totalSupplyKW);
            } catch (GridOverloadException e) {
                logger.logError(e.getMessage());
                eventHistory.add("[Cycle " + cycleCount + "] " + e.getMessage());
                notifyAlert("ERROR", e.getMessage());
                performEmergencyLoadShed();
            }
            notifyCycle();
        } finally { lock.unlock(); }
    }

    private void performEmergencyLoadShed() {
        logger.logWarning("EMERGENCY LOAD SHEDDING");
        notifyAlert("WARN", "Emergency load shedding activated");
        sectors.stream().filter(s -> s.getPriority().getLevel() >= 3)
            .forEach(s -> { s.receiveAllocation(0); logger.logWarning("Shed: " + s.getName()); });
    }

    public void saveState() {
        lock.lock();
        try {
            GridPersistence.saveState(new GridState(sectors, totalSupplyKW, cycleCount, eventHistory));
            logger.log("Grid state saved.");
        } finally { lock.unlock(); }
    }

    public boolean loadState() {
        lock.lock();
        try {
            GridState st = GridPersistence.loadState();
            if (st == null) return false;
            sectors.clear(); sectors.addAll(st.getSectors());
            this.totalSupplyKW = st.getTotalSupplyKW();
            this.cycleCount    = st.getCycleCount();
            eventHistory.addAll(st.getEventHistory());
            logger.log("Restored from cycle #" + st.getCycleCount());
            return true;
        } finally { lock.unlock(); }
    }

    public void printDashboard() {
        lock.lock();
        try {
            System.out.println("\n\u001B[36m=== VoltSync Cycle #" + cycleCount + " | Supply: " + String.format("%.1f", totalSupplyKW) + " kW ===\u001B[0m");
            sectors.forEach(s -> System.out.println("  " + s.getStatusSummary()));
        } finally { lock.unlock(); }
    }

    public String         getGridName()      { return gridName; }
    public double         getTotalSupplyKW() { return totalSupplyKW; }
    public int            getCycleCount()    { return cycleCount; }
    public boolean        isGridOnline()     { return gridOnline; }
    public void           shutdown()         { this.gridOnline = false; }
    public List<Sector>   getSectors()       { return Collections.unmodifiableList(sectors); }
    public List<String>   getEventHistory()  { return eventHistory; }
    public List<PowerSource> getSources()    { return Collections.unmodifiableList(sources); }
    public static int     getInstanceCount() { return instanceCount; }
}
