package com.boot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;



@Service
public class KafkaSender {
	

	  @Autowired
	  private KafkaTemplate<String, String> kafkaTemplate;

	  public void send(String data) {
	 
	    kafkaTemplate.send("simpleTopic", data);
	  }
}