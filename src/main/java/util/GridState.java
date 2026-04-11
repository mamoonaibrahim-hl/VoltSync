package util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class GridState implements Serializable {
    private static final long serialVersionUID = 1L;
    private Map<String, Double> sectorLoads = new HashMap<>();
    private double totalAvailablePower;

    public void recordLoad(String id, double load) { sectorLoads.put(id, load); }
    public void setTotalAvailablePower(double p) { this.totalAvailablePower = p; }
    public Map<String, Double> getSectorLoads() { return sectorLoads; }
    public double getTotalAvailablePower() { return totalAvailablePower; }
}