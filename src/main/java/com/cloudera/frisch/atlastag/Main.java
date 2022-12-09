/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudera.frisch.atlastag;

import com.cloudera.frisch.atlastag.config.PropertiesLoader;
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
