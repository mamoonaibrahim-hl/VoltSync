package model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public abstract class Sector extends Thread {
    private String sectorID;
    private int priority;
    protected DoubleProperty currentLoad = new SimpleDoubleProperty(0.0);
    protected boolean isActive = true;

    public Sector(String sectorID, int priority) {
        this.sectorID = sectorID;
        this.priority = priority;
    }

    public abstract void consumePower();

    @Override
    public void run() {
        while (isActive) {
            consumePower();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                isActive = false;
            }
        }
    }

    public String getSectorID() { return sectorID; }
    public int getSectorPriority() { return priority; }
    public double getCurrentLoad() { return currentLoad.get(); }
    public DoubleProperty loadProperty() { return currentLoad; }
    public void shutDown() { this.isActive = false; this.interrupt(); }
}