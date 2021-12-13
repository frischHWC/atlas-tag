package com.cloudera.frisch.javaappcdp;


import com.cloudera.frisch.javaappcdp.config.PropertiesLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.atlas.kafka.AtlasKafkaMessage;
import org.apache.atlas.kafka.NotificationProvider;
import org.apache.atlas.model.instance.AtlasClassification;
import org.apache.atlas.model.notification.EntityNotification;
import org.apache.atlas.notification.NotificationConsumer;
import org.apache.atlas.notification.NotificationInterface;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AtlasListener {

  private final static Logger logger = Logger.getLogger(AtlasListener.class);
  private final ObjectMapper objectMapper;
  private HttpClient client;

  public AtlasListener() {

    objectMapper = new ObjectMapper();

    try {
      TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
          TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init(KeyStore.getInstance(
          new File(PropertiesLoader.getProperty("atlas.truststore.path")),
          PropertiesLoader.getProperty("atlas.truststore.password")
              .toCharArray()));
      SSLContext sslContext = SSLContext.getInstance("TLSv1");
      sslContext.init(null, trustManagerFactory.getTrustManagers(),
          new SecureRandom());

      client = HttpClient.newBuilder()
          .authenticator(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
              return new PasswordAuthentication(
                  PropertiesLoader.getProperty("atlas.username"),
                  PropertiesLoader.getProperty("atlas.password").toCharArray());
            }
          })
          .sslContext(sslContext)
          .build();
    } catch (Exception e) {
      logger.error("Could not initialize HTTP Client for Atlas API, due to error: ", e);
    }

  }

  public void listenToAtlasEntities(Map<Pattern, String> mapTag) {
    NotificationInterface notification = NotificationProvider.get();
     NotificationConsumer<Object> consumer =
         (NotificationConsumer<Object>) notification.createConsumers(NotificationInterface.NotificationType.ENTITIES, 1).get(0);

    while(true) {
       Iterator<AtlasKafkaMessage<Object>>
           messages = consumer.receive().iterator();

      while (messages.hasNext()) {
        AtlasKafkaMessage<Object> message = messages.next();

        evaluateTag(mapTag, (EntityNotification) message.getMessage());

        consumer.commit(message.getTopicPartition(), message.getOffset());
      }
    }

    // consumer.close();
  }

  private void evaluateTag(Map<Pattern, String> mapTag, EntityNotification notificationMessage) {
    if(notificationMessage.getType()==
        EntityNotification.EntityNotificationType.ENTITY_NOTIFICATION_V1) {
      logger.info("Does no treat V1 messages, only V2");
      return;
    }
    EntityNotification.EntityNotificationV2 notificationMessageV2 = (EntityNotification.EntityNotificationV2) notificationMessage;
    if(notificationMessageV2.getOperationType()==
        EntityNotification.EntityNotificationV2.OperationType.ENTITY_CREATE || notificationMessageV2.getOperationType()==
        EntityNotification.EntityNotificationV2.OperationType.ENTITY_UPDATE) {

      if(notificationMessageV2.getEntity().getTypeName().equalsIgnoreCase("hive_table") ||
          notificationMessageV2.getEntity().getTypeName().equalsIgnoreCase("hive_column")) {
        List<String> tags = new ArrayList<>();
        mapTag.forEach((pattern, tag) -> {
          if(pattern.matcher(notificationMessageV2.getEntity().getAttribute("name").toString()).matches()) {
            tags.add(tag);
          }
        });
        if(!tags.isEmpty()) {
          publishTag(notificationMessageV2.getEntity().getGuid(), tags);
        }
      } else {
        logger.info(" Does not treat message as it is neither a hive table or column");
      }
    } else {
      logger.info(" Does not treat message as it is neither a creation or an update ");
    }
  }

  private void publishTag(String guid, List<String> tags) {
    List<AtlasClassification> classifications = tags.stream().map(tag -> new AtlasClassification(tag)).collect(Collectors.toList());

    try {
      logger.info("Will update entity with GUID: " + guid +
          " with this list of classifications: " +
          objectMapper.writeValueAsString(classifications));

      HttpRequest request = HttpRequest.newBuilder()
          .uri(new URI(PropertiesLoader.getProperty("atlas.url") + "/v2/entity/guid/" + guid + "/classifications"))
          .headers("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(classifications)))
          .build();

      HttpResponse httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

      if(httpResponse.statusCode()!=200 && httpResponse.statusCode()!=204) {
        logger.error("Error on API call: " + httpResponse.statusCode() + " with " + httpResponse.body().toString());
      } else {
        logger.error("Successful API call: " + httpResponse.body().toString());
      }

    } catch(Exception e) {
      logger.error("Could not make request due to error: ", e);
    }

  }
}
