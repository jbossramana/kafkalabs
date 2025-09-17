package demo.avro.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import demo.avro.User;

@RestController
public class Sender {

  private static final Logger LOGGER = LoggerFactory.getLogger(Sender.class);

  @Value("${spring.kafka.topic}")
  private String avroTopic;

  @Autowired
  private KafkaTemplate<String, User> kafkaTemplate;

  @GetMapping(value = "/info")
  public String info()
  {
	return "good job";  
  }
  
  @PostMapping(value = "/send")
  public void send(@RequestBody User user)
  
  {
	  User user2 = User.newBuilder().setName(user.getName()).setFavoriteColor(user.getFavoriteColor())
		       .setFavoriteNumber(user.getFavoriteNumber()).build();
    LOGGER.info("sending user='{}'", user2.toString());
    kafkaTemplate.send(avroTopic, user2);
  }
}
