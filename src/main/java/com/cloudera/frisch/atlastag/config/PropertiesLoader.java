package com.cloudera.frisch.atlastag.config;

import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;


public class PropertiesLoader {

    private final static Logger logger = Logger.getLogger(PropertiesLoader.class);

    public final static Properties properties = loadProperties();

    private static Properties loadProperties() {
        // Load config file
        Properties properties = new Properties();
        try {
            FileInputStream fileInputStream = new FileInputStream("config.properties");
            properties.load(fileInputStream);
        } catch (IOException e) {
            logger.error("Property file not found !", e);
        }
        return properties;
    }

    public static String getProperty(String key) {
        String property = "null";
        try {
             property = properties.getProperty(key);
            if(property.substring(0,2).equalsIgnoreCase("${")) {
                property = PropertiesLoader.getProperty(property.substring(2,property.length()-1));
            }
        } catch (Exception e) {
            logger.warn("Could not get property : " + key + " due to following error: ", e);
        }
        return property;
    }

    public static Map<Pattern, String> loadTagMapping(String path) {
        try {
            Map<Pattern, String> mapTag = new HashMap<>();
            Files.readAllLines(Path.of(path)).forEach(line -> {
                String[] lineSplit = line.split("=");
                mapTag.put(Pattern.compile(lineSplit[0]), lineSplit[1]);
            });
            return mapTag;
        } catch (IOException e) {
            logger.info("Could not read model file: " + path);
            return Collections.emptyMap();
        }
    }
}
