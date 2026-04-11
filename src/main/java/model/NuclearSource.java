package model;

public class NuclearSource extends SupplySource {
    public NuclearSource() { super("Nuclear Plant"); }

    @Override
    public void calculateOutput() {
        currentOutput.set(500 + (Math.random() * 5));
    }
}