package voltsync.grid;

import voltsync.model.Sector;
import voltsync.exceptions.GridOverloadException;

import java.util.List;

public interface Schedulable {
    
    double allocate(List<Sector> sectors, double availableKW) throws GridOverloadException;

    String getStrategyName();
}
