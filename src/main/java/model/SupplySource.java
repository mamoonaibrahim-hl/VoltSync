package model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public abstract class SupplySource extends Thread {
    protected DoubleProperty currentOutput = new SimpleDoubleProperty(0.0);
    protected boolean running = true;
    private String sourceName;

    public SupplySource(String name) {
        this.sourceName = name;
    }

    public abstract void calculateOutput();

    @Override
    public void run() {
        while (running) {
            calculateOutput();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                running = false;
            }
        }
    }

    public DoubleProperty outputProperty() { return currentOutput; }
    public double getCurrentOutput() { return currentOutput.get(); }
    public String getSourceName() { return sourceName; }
    public void stopSource() { this.running = false; this.interrupt(); }
}