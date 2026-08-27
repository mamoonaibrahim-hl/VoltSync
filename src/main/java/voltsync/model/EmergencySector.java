package voltsync.model;

public class EmergencySector extends Sector {

    private static final long serialVersionUID = 1L;

    private final String emergencyType;   
    private boolean lifeSupport;          

    
    public EmergencySector(String name, String emergencyType, double demandKW) {
        super(name, SectorPriority.CRITICAL, demandKW);  
        this.emergencyType = emergencyType;
        this.lifeSupport   = false;
    }

    public EmergencySector(String name, String emergencyType) {
        this(name, emergencyType, 250.0);
    }

    
    @Override
    public String getEntityType() { return "EmergencySector"; }

    @Override
    public String getStatusSummary() {
        return super.getStatusSummary()
             + (lifeSupport ? "  \u001B[31m[LIFE-SUPPORT ACTIVE]\u001B[0m" : "");
    }

    
    public final boolean requiresHardLock() {
        return lifeSupport;
    }

    public void activateLifeSupport() {
        this.lifeSupport = true;
        System.out.println("\u001B[31m[ALERT] Life support activated for " + getName() + "\u001B[0m");
    }

    public String getEmergencyType() { return emergencyType; }
}
