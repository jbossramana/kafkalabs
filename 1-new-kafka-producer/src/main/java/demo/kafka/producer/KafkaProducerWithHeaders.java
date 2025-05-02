package demo.kafka.producer;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class KafkaProducerWithHeaders {

    private static final String TOPIC_NAME = "test-topic";
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        try (Producer<String, String> producer = new KafkaProducer<>(props)) {

            // Create headers
            RecordHeaders headers = new RecordHeaders();
            headers.add(new RecordHeader("flag", "true".getBytes()));

            // Create a record with headers
            ProducerRecord<String, String> record = new ProducerRecord<String, String>(TOPIC_NAME, null, "m1", "Hello Kafka with Headers!", headers);

            // Send the record
            producer.send(record, new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception exception) {
                    if (exception == null) {
                        System.out.printf("Sent record with headers successfully to topic %s%n", metadata.topic());
                    } else {
                        System.err.println("Error sending record with headers: " + exception.getMessage());
                    }
                }
            });

            // Flush and close the producer
            producer.flush();
        }
    }
}
