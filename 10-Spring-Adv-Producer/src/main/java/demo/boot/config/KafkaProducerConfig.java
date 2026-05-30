package demo.boot.config;

import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.transaction.KafkaTransactionManager;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, Object> producerFactory(
            KafkaProperties properties) {

        Map<String, Object> configs =
                properties.buildProducerProperties();

        // Exactly Once
        configs.put(
                ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,
                true
        );

        configs.put(
                ProducerConfig.ACKS_CONFIG,
                "all"
        );

        configs.put(
                ProducerConfig.RETRIES_CONFIG,
                Integer.MAX_VALUE
        );

        configs.put(
                ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION,
                5
        );

        DefaultKafkaProducerFactory<String, Object> factory =
                new DefaultKafkaProducerFactory<>(configs);

        // REQUIRED FOR TRANSACTIONS
        factory.setTransactionIdPrefix("tx-order-");

        return factory;
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(
            ProducerFactory<String, Object> producerFactory) {

        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public KafkaTransactionManager<String, Object>
    kafkaTransactionManager(
            ProducerFactory<String, Object> producerFactory) {

        return new KafkaTransactionManager<>(
                producerFactory
        );
    }
}