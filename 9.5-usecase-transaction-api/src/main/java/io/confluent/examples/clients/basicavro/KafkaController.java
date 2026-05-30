package io.confluent.examples.clients.basicavro;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping(value = "/order")
public class KafkaController {

  private final Producer producer;

  @Autowired
  KafkaController(Producer producer) {
    this.producer = producer;
  }

  @PostMapping(value = "/publish")
 public void sendMessageToKafkaTopic(@RequestParam("id") String id, @RequestParam("name") String name) {
   this.producer.sendMessage(new Order(id, name));

  }
}