package model;

import java.util.ArrayList;
import java.util.List;
import util.GridOverloadException;

public class PowerGrid {
    private double totalAvailablePower;
    private List<Sector> sectors = new ArrayList<>();
    private List<SupplySource> sources = new ArrayList<>();

    public PowerGrid(double initialPower) {
        this.totalAvailablePower = initialPower;
    }

    public synchronized void allocatePower() throws GridOverloadException {
        double currentDemand = getTotalDemand();
        double available = getTotalAvailablePower();
        if (currentDemand > available) {
            throw new GridOverloadException("Demand: " + String.format("%.1f", currentDemand)
                + " MW exceeds Supply: " + String.format("%.1f", available) + " MW");
        }
    }

    public synchronized void shedLoad() {
        sectors.stream()
            .filter(s -> s.getSectorPriority() == 3)
            .forEach(Sector::shutDown);
        sectors.removeIf(s -> s.getSectorPriority() == 3);
    }

    public double getTotalAvailablePower() {
        if (sources.isEmpty()) return totalAvailablePower;
        return sources.stream().mapToDouble(s -> s.outputProperty().get()).sum();
    }

    public double getTotalDemand() {
        return sectors.stream().mapToDouble(Sector::getCurrentLoad).sum();
    }

    public void addSector(Sector s) { sectors.add(s); }
    public void addSource(SupplySource s) { sources.add(s); }
    public List<Sector> getSectors() { return sectors; }
    public List<SupplySource> getSources() { return sources; }
}