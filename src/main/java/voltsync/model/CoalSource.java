package voltsync.model;

public class CoalSource extends PowerSource {
    public CoalSource(String name, double maxKW) { super(name, maxKW); }
    @Override public String getEntityType() { return "Coal"; }
    @Override public void simulateFluctuation() {
        double factor = 0.95 + rand.nextDouble() * 0.1;
        currentOutputKW = Math.min(maxCapacityKW, maxCapacityKW * factor);
    }
}
