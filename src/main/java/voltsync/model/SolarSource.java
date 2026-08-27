package voltsync.model;

public class SolarSource extends PowerSource {
    public SolarSource(String name, double maxKW) { super(name, maxKW); }
    @Override public String getEntityType() { return "Solar"; }
    @Override public void simulateFluctuation() {
        double factor = 0.7 + rand.nextDouble() * 0.6;
        currentOutputKW = Math.min(maxCapacityKW, maxCapacityKW * factor);
    }
}
