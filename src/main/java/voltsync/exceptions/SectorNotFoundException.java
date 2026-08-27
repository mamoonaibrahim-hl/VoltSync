package voltsync.exceptions;

public class SectorNotFoundException extends Exception {
    public SectorNotFoundException(String sectorName) {
        super("[SECTOR NOT FOUND] No sector registered with name: " + sectorName);
    }
}
