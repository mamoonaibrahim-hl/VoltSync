package voltsync.model;

import java.io.Serializable;
import java.util.Random;

public abstract class PowerSource extends GridEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    protected double maxCapacityKW;    
    protected double currentOutputKW;
    protected boolean online;
    protected final Random rand = new Random();

    protected PowerSource(String name, double maxCapacityKW) {
        super(name);
        this.maxCapacityKW   = maxCapacityKW;
        this.currentOutputKW = maxCapacityKW;
        this.online          = true;
    }

    
    public abstract void simulateFluctuation();

    
    @Override
    public abstract String getEntityType();

    @Override
    public String getStatusSummary() {
        return String.format("%-20s | Output: %6.1f / %6.1f kW | Online: %s",
                getName(), currentOutputKW, maxCapacityKW, online ? "YES" : "NO");
    }

    public double getCurrentOutputKW() { return online ? currentOutputKW : 0; }
    public double getMaxCapacityKW()   { return maxCapacityKW; }
    public boolean isOnline()          { return online; }
    public void    setOnline(boolean v){ this.online = v; }
}

