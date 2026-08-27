package voltsync.exceptions;

public class GridOverloadException extends Exception {

    private final double demand;
    private final double supply;

    public GridOverloadException(double demand, double supply) {
        super(String.format(
            "[GRID OVERLOAD] Demand %.1f kW exceeds supply %.1f kW (deficit: %.1f kW)",
            demand, supply, demand - supply));
        this.demand = demand;
        this.supply = supply;
    }

    public double getDeficit()  { return demand - supply; }
    public double getDemand()   { return demand; }
    public double getSupply()   { return supply; }
}
