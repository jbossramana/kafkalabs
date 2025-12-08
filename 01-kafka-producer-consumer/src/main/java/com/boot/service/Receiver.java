package com.boot.service;



import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class Receiver {

  private static final Logger LOGGER = LoggerFactory.getLogger(Receiver.class);

  
  @KafkaListener(topics = "simpleTopic")
  public void receive(ConsumerRecord<?, ?> consumerRecord) {
    LOGGER.info("received payload='{}'", consumerRecord.toString());

  }
}
