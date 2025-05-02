package io.confluent.examples.clients.basicavro;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class KafkaController {

  @GetMapping
  public String info()
  {
	  return "Reporting service";
  }
}