package io.confluent.examples.clients.basicavro;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;



@Service
public class Consumer {

  @KafkaListener(topics = "new_orders", groupId = "order_id")
  public void consume(ConsumerRecord<String, GenericRecord> record) {

	  GenericRecord order = record.value();
	  System.out.println("insdie order :"+ order);
  }
}