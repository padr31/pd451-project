package uk.ac.cam.pd451.dissertation.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Props {
    private static Properties prop = null;

    private static void loadProperties() {
        prop = new Properties();
        try(FileInputStream in = new FileInputStream("configuration.properties")) {
            prop.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String get(String key) {
        if(prop == null) {
            loadProperties();
        }
        return prop.getProperty(key);
    }
}
