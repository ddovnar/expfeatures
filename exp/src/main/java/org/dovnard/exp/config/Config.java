package org.dovnard.exp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static Logger logger = LoggerFactory.getLogger(Config.class);
    private static Config _instance;
    private static Properties props;
    private static final String CONFIG_FILE_NAME = "config.properties";
    private Config() {
        logger.info("Create config loader");
        props = new Properties();
        InputStream fileProps = null;
        try {
            File file = new File(CONFIG_FILE_NAME);
            if (file.exists()) {
                fileProps = new FileInputStream(file);
            } else {
                logger.info("Try load config properties from resource folder");
                ClassLoader loader = Thread.currentThread().getContextClassLoader();
                fileProps = loader.getResourceAsStream(CONFIG_FILE_NAME);

                if (fileProps != null) {
                    logger.info("Config properties loaded");
                }
            }
            props.load(fileProps);
            logger.info("load config properties success");
        } catch(IOException ex) {
            throw new RuntimeException("Can't load config.properties file!!!");
        } finally {
            try {
                if (fileProps != null)
                    fileProps.close();
            } catch(IOException ex) {
            }
        }
    }
    public static Config getInstance() {
        if (_instance == null) {
            _instance = new Config();
        }
        return _instance;
    }
    public String getProperty(String key) {
        return props.getProperty(key);
    }
}
