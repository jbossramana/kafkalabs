package io.confluent.examples.clients.basicavro;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;



@Service
public class Consumer {

  @KafkaListener(topics = "new_orders", groupId = "new_order_id")
  public void consume(ConsumerRecord<String, Order> record) {

	  System.out.println("inside new order :"+ record.value());
  }
}