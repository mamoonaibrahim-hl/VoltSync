package model;

public class WindSource extends SupplySource {
    public WindSource() { super("Wind Farm"); }

    @Override
    public void calculateOutput() {
        currentOutput.set(50 + (Math.random() * 100));
    }
}