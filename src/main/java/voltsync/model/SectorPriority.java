package voltsync.model;

public enum SectorPriority {
    CRITICAL(1, "CRITICAL", "\u001B[31m"),   
    HIGH    (2, "HIGH",     "\u001B[33m"),   
    MEDIUM  (3, "MEDIUM",  "\u001B[34m"),   
    LOW     (4, "LOW",     "\u001B[32m");   

    private final int level;        
    private final String label;
    private final String ansiColor;

    SectorPriority(int level, String label, String ansiColor) {
        this.level     = level;
        this.label     = label;
        this.ansiColor = ansiColor;
    }

    public int    getLevel()     { return level; }
    public String getLabel()     { return label; }
    public String colored()      { return ansiColor + label + "\u001B[0m"; }
}
