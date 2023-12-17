package util;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public record AppConfig(
        String dbClass,
        String dbName,
        String dbUser,
        String dbPassword,
        String dbHost,
        int dbPort,
        int httpPort
) {
    static public AppConfig props = Load();

    static public AppConfig Load() {
        try {
            var appConfigFile = new File("app.properties");
            if (appConfigFile.exists()) {
                try (var in = new FileInputStream(appConfigFile)) {
                    Properties props = new Properties();
                    props.load(in);
                    return new AppConfig(
                            props.getProperty("db.class"),
                            props.getProperty("db.name"),
                            props.getProperty("db.user"),
                            props.getProperty("db.password"),
                            props.getProperty("db.host"),
                            Integer.parseInt(props.getProperty("db.port")),
                            Integer.parseInt(props.getProperty("http.port"))
                    );
                }
            }
        } catch (Exception ignore) {
        }
        throw new RuntimeException("Unable to load app.properties");
    }
}

