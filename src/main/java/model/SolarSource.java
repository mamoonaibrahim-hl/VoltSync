package model;

public class SolarSource extends SupplySource {
    private int hourOfDay = 8;

    public SolarSource() { super("Solar Array"); }

    @Override
    public void calculateOutput() {
        double efficiency = Math.max(0, 1 - Math.abs(12 - hourOfDay) / 6.0);
        currentOutput.set(200 * efficiency);
        hourOfDay = (hourOfDay + 1) % 24;
    }
}