package com.boot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.boot.model.UserBean;

@Service
public class KafkaSender {
	

	  @Autowired
	  private KafkaTemplate<String, UserBean> kafkaTemplate;

	  public void send(UserBean user) {
	 
	    kafkaTemplate.send("test01", user);
	  }
}