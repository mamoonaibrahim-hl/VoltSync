package voltsync.logging;

public interface Reportable {
    String generateReport();
    void   saveReportToFile(String filePath);
}
