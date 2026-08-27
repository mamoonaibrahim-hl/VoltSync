package voltsync.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public abstract class GridEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    
    private static int entityCount = 0;

    private final int entityId;           
    private String name;
    private final LocalDateTime createdAt;
    private boolean active;

    
    protected GridEntity(String name) {
        this.entityId    = ++entityCount;
        this.name        = name;
        this.createdAt   = LocalDateTime.now();
        this.active      = true;
    }

    
    protected GridEntity(GridEntity other) {
        this.entityId    = ++entityCount;   
        this.name        = other.name + "_copy";
        this.createdAt   = LocalDateTime.now();
        this.active      = other.active;
    }

    
    public abstract String getEntityType();

    
    public abstract String getStatusSummary();

    
    public int    getEntityId()   { return entityId; }
    public String getName()       { return name; }
    public void   setName(String name) { this.name = name; }
    public boolean isActive()     { return active; }
    public void   setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    
    public static int getTotalEntities() { return entityCount; }

    @Override
    public String toString() {
        return String.format("[%s #%d] %s | Active: %s", getEntityType(), entityId, name, active);
    }
}
