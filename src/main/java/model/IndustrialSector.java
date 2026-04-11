package model;

public class IndustrialSector extends Sector {
    public IndustrialSector(String id) {
        super(id, 2);
    }

    @Override
    public void consumePower() {
        double demand = 80 + (Math.random() * 20);
        currentLoad.set(demand);
    }
}