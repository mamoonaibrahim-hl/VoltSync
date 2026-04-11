package util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private Properties props = new Properties();

    public ConfigLoader() {
        try (InputStream is = getClass().getResourceAsStream("/config.properties")) {
            if (is != null) {
                props.load(is);
            } else {
                System.err.println("config.properties not found in resources, using defaults.");
            }
        } catch (IOException e) {
            System.err.println("Config load failed: " + e.getMessage());
        }
    }

    public double getInitialPower() {
        return Double.parseDouble(props.getProperty("initialPower", "500"));
    }

    public int getCheckInterval() {
        return Integer.parseInt(props.getProperty("checkInterval", "1000"));
    }
}