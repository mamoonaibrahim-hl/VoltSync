package voltsync.persistence;

import java.io.*;

public class GridPersistence {

    private static final String DEFAULT_PATH = "grid_state.ser";

    
    public static void saveState(GridState state, String path) {
        
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(path));
            oos.writeObject(state);
            System.out.println("[Persistence] Grid state saved to: " + path);
        } catch (IOException e) {
            System.err.println("[Persistence] Save failed: " + e.getMessage());
        } finally {
            if (oos != null) {
                try { oos.close(); } catch (IOException ignored) {}
            }
        }
    }

    public static void saveState(GridState state) {
        saveState(state, DEFAULT_PATH);
    }

    
    public static GridState loadState(String path) {
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(path));
            GridState state = (GridState) ois.readObject();
            System.out.println("[Persistence] Grid state loaded from: " + path
                + " (saved at: " + state.getSavedAt() + ")");
            return state;
        } catch (FileNotFoundException e) {
            System.out.println("[Persistence] No saved state found at: " + path);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[Persistence] Load failed: " + e.getMessage());
        } finally {
            if (ois != null) {
                try { ois.close(); } catch (IOException ignored) {}
            }
        }
        return null;
    }

    public static GridState loadState() {
        return loadState(DEFAULT_PATH);
    }
}
