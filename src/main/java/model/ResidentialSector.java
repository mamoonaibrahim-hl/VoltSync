package model;

public class ResidentialSector extends Sector {
    public ResidentialSector(String id) {
        super(id, 3);
    }

    @Override
    public void consumePower() {
        double demand = 20 + (Math.random() * 15);
        currentLoad.set(demand);
    }
}