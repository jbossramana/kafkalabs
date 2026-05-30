package demo.boot.config;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import demo.boot.model.OrderEvent;

//================================

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class KafkaConsumerConfig {

 @Bean
 public ConsumerFactory<String, OrderEvent> consumerFactory(
         KafkaProperties properties) {

     Map<String, Object> configs =
             properties.buildConsumerProperties();

     configs.put(
             ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
             StringDeserializer.class
     );

     configs.put(
             ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
             JsonDeserializer.class
     );

     configs.put(
             JsonDeserializer.TRUSTED_PACKAGES,
             "*"
     );

     configs.put(
             JsonDeserializer.VALUE_DEFAULT_TYPE,
             "demo.boot.model.OrderEvent"
     );

     return new DefaultKafkaConsumerFactory<>(configs);
 }

 @Bean
 public ConcurrentKafkaListenerContainerFactory<String, OrderEvent>
 kafkaListenerContainerFactory(
         ConsumerFactory<String, OrderEvent> consumerFactory,
         KafkaTemplate<String, Object> kafkaTemplate) {

     ConcurrentKafkaListenerContainerFactory<String, OrderEvent> factory =
             new ConcurrentKafkaListenerContainerFactory<>();

     factory.setConsumerFactory(consumerFactory);

     // batch processing
     factory.setBatchListener(true);

     // parallel consumers
     factory.setConcurrency(3);

     // manual commit
     factory.getContainerProperties()
             .setAckMode(
                     org.springframework.kafka.listener.ContainerProperties
                             .AckMode.MANUAL
             );

     // DLT logic
     DeadLetterPublishingRecoverer recoverer =
             new DeadLetterPublishingRecoverer(
                     kafkaTemplate,
                     (record, ex) ->
                             new TopicPartition(
                                     "orders.DLT",
                                     record.partition()
                             )
             );

     // retry 3 times with 2 sec delay
     DefaultErrorHandler errorHandler =
             new DefaultErrorHandler(
                     recoverer,
                     new FixedBackOff(2000L, 3)
             );

     factory.setCommonErrorHandler(errorHandler);

     return factory;
 }
}