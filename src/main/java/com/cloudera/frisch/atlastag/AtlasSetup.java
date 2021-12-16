package com.cloudera.frisch.atlastag;

import com.cloudera.frisch.atlastag.config.PropertiesLoader;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasException;
import org.apache.atlas.hook.AtlasHook;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

public class AtlasSetup {

  private final static Logger logger = Logger.getLogger(AtlasSetup.class);

  public static void setupAtlasConf() {

    try {

      Configuration atlasConf = ApplicationProperties.get();

      atlasConf.setProperty("atlas.notification.hook.topic.name",
          PropertiesLoader.getProperty("hook-topic"));
      atlasConf.setProperty("atlas.kafka.zookeeper.connect",
          PropertiesLoader.getProperty("zookeeper.connect"));

      atlasConf.setProperty("atlas.kafka.bootstrap.servers",
          PropertiesLoader.getProperty("kafka.brokers"));
      atlasConf.setProperty("atlas.kafka.acks",
          PropertiesLoader.getProperty("kafka.acks"));
      atlasConf.setProperty("atlas.kafka.security.protocol",
          PropertiesLoader.getProperty("kafka.protocol"));
      atlasConf.setProperty("atlas.kafka.entities.group.id",
          PropertiesLoader.getProperty("project.name"));
      atlasConf.setProperty("atlas.kafka.client.id",
          PropertiesLoader.getProperty("project.name"));
      atlasConf.setProperty("atlas.kafka.auto.offset.reset",
          "earliest");
      atlasConf.setProperty(AtlasHook.ATLAS_NOTIFICATION_ASYNCHRONOUS, false);


      if (PropertiesLoader.getProperty("kafka.protocol").contains("SASL")) {
        atlasConf.setProperty("atlas.kafka.sasl.kerberos.service.name",
            PropertiesLoader.getProperty("kafka.kerberos.service-name"));

        atlasConf.setProperty("atlas.jaas.KafkaClient.loginModuleControlFlag",
            PropertiesLoader.getProperty("kafka.kerberos.login-module"));
        atlasConf.setProperty("atlas.jaas.KafkaClient.loginModuleName",
            PropertiesLoader.getProperty("kafka.kerberos.login-module-name"));
        atlasConf.setProperty("atlas.jaas.KafkaClient.option.serviceName",
            PropertiesLoader.getProperty("kafka.kerberos.service-name"));
        atlasConf.setProperty("atlas.jaas.KafkaClient.option.useKeyTab",
            PropertiesLoader.getProperty("kafka.kerberos.use-keytab"));
        atlasConf.setProperty("atlas.jaas.KafkaClient.option.storeKey",
            PropertiesLoader.getProperty("kafka.kerberos.store-key"));
        atlasConf.setProperty("atlas.jaas.KafkaClient.option.useTicketCache",
            PropertiesLoader.getProperty("kafka.kerberos.use-ticket-cache"));
        atlasConf.setProperty("atlas.jaas.KafkaClient.option.keyTab",
            PropertiesLoader.getProperty("keytab"));
        atlasConf.setProperty("atlas.jaas.KafkaClient.option.principal",
            PropertiesLoader.getProperty("kerberos-user"));
      }

      if (PropertiesLoader.getProperty("kafka.protocol").contains("SSL")) {

        atlasConf.setProperty("atlas.kafka.ssl.truststore.location",
            PropertiesLoader.getProperty("kafka.truststore.location"));
        atlasConf.setProperty("atlas.kafka.ssl.truststore.password",
            PropertiesLoader.getProperty("kafka.truststore.password"));

        atlasConf.setProperty("atlas.kafka.ssl.keystore.location",
            PropertiesLoader.getProperty("kafka.keystore.location"));
        atlasConf.setProperty("atlas.kafka.ssl.keystore.password",
            PropertiesLoader.getProperty("kafka.keystore.password"));
        atlasConf.setProperty("atlas.kafka.ssl.key.password",
            PropertiesLoader.getProperty("kafka.keystore.key-password"));

      }
    } catch (AtlasException e) {
      logger.error("Could not load properties");
    }
  }
}
