package voltsync.analytics;

import voltsync.logging.Reportable;
import voltsync.model.Sector;
import voltsync.model.SectorPriority;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AnalyticsDashboard implements Reportable {

    private int    totalCycles;
    private double totalKWDistributed;
    private double totalKWDemanded;
    private int    overloadEvents;
    private int    loadSheddingEvents;

    private final List<Sector> sectors;  
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AnalyticsDashboard(List<Sector> sectors) {
        this.sectors = sectors;
    }

    public void recordCycle(double distributed, double demanded) {
        totalCycles++;
        totalKWDistributed += distributed;
        totalKWDemanded    += demanded;
    }

    public void recordOverload()     { overloadEvents++; }
    public void recordLoadShedding() { loadSheddingEvents++; }

    @Override
    public String generateReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("╔══════════════════════════════════════════════════════════╗\n");
        sb.append("║           VoltSync Pro — Session Analytics Report        ║\n");
        sb.append("╠══════════════════════════════════════════════════════════╣\n");
        sb.append(String.format("║  Generated     : %-39s║%n", LocalDateTime.now().format(fmt)));
        sb.append(String.format("║  Total Cycles  : %-39d║%n", totalCycles));
        sb.append(String.format("║  Total Demand  : %-36.1f kW ║%n", totalKWDemanded));
        sb.append(String.format("║  Total Supplied: %-36.1f kW ║%n", totalKWDistributed));
        double eff = totalKWDemanded > 0 ? (totalKWDistributed / totalKWDemanded) * 100 : 100;
        sb.append(String.format("║  Grid Efficiency: %-35.1f %%  ║%n", eff));
        sb.append(String.format("║  Overload Events: %-38d║%n", overloadEvents));
        sb.append(String.format("║  Load Shed Events:%-38d║%n", loadSheddingEvents));
        sb.append("╠══════════════════════════════════════════════════════════╣\n");
        sb.append("║  Per-Sector Summary                                      ║\n");
        sb.append("╠══════════════════════════════════════════════════════════╣\n");

        for (Sector s : sectors) {
            sb.append(s.getMetrics().getSummaryReport()).append("\n");
        }

        sb.append("╚══════════════════════════════════════════════════════════╝\n");
        return sb.toString();
    }

    @Override
    public void saveReportToFile(String filePath) {
        
        
        FileWriter fw = null;
        try {
            fw = new FileWriter(filePath, false);
            fw.write(generateReport());
            System.out.println("[Analytics] Report saved to: " + filePath);
        } catch (IOException e) {
            System.err.println("[Analytics] Could not write report: " + e.getMessage());
        } finally {
            if (fw != null) {
                try { fw.close(); } catch (IOException ignored) {}
            }
        }
    }
}
