package com.cloudera.frisch.javaappcdp;

import com.cloudera.frisch.javaappcdp.config.PropertiesLoader;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.regex.Pattern;


public class Main {

    private final static Logger logger = Logger.getLogger(Main.class);

    public static void main(String [] args) {

        logger.info("Starting Application");
        long start = System.currentTimeMillis();

        if(args.length < 1) {
            logger.error("There must be at least one argument : Path to file mapping Java Regex to tag to apply");
            System.exit(1);
        }

        Map<Pattern, String> mapTag = PropertiesLoader.loadTagMapping(args[0]);

        AtlasSetup.setupAtlasConf();
        new AtlasListener().listenToAtlasEntities(mapTag);

        logger.info("Application Finished");
        logger.info("Application took : " + (System.currentTimeMillis()-start) + " ms to run");

    }

}
