package model;

public class HospitalSector extends Sector {
    public HospitalSector(String id) {
        super(id, 1);
    }

    @Override
    public void consumePower() {
        double demand = 50 + (Math.random() * 10);
        currentLoad.set(demand);
    }
}