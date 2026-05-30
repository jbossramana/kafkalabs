package io.confluent.examples.clients.basicavro;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;



@Service
public class Producer {

  @Value("${topic.name}")
  private String TOPIC;

  private final KafkaTemplate<String, Order> kafkaTemplate;

  @Autowired
  public Producer(KafkaTemplate<String, Order> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  void sendMessage(Order order) {
    this.kafkaTemplate.send(this.TOPIC, order.getId().toString(), order);
  
  }
}
