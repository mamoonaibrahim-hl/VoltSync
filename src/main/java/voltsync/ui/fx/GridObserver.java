package voltsync.ui.fx;

import voltsync.model.Sector;
import java.util.List;

public interface GridObserver {

    
    void onCycleComplete(List<Sector> sectors, double supplyKW,
                         double totalDemandKW, int cycleNumber);

    
    void onAlert(String level, String message);

    
    void onSupplyChanged(String sourceName, double outputKW, double maxKW);
}
