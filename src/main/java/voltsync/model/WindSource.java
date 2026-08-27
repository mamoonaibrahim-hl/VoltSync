package voltsync.model;

public class WindSource extends PowerSource {
    public WindSource(String name, double maxKW) { super(name, maxKW); }
    @Override public String getEntityType() { return "Wind"; }
    @Override public void simulateFluctuation() {
        double factor = 0.5 + rand.nextDouble() * 1.0;
        currentOutputKW = Math.min(maxCapacityKW, maxCapacityKW * factor);
    }
}
