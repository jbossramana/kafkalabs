import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.UUID;

public class IdempotentProducer {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        String topic = "first-topic";
        String value = "Test message";
        String uniqueId = UUID.randomUUID().toString();  // Unique ID for idempotency

        ProducerRecord<String, String> record = new ProducerRecord<>(topic, value);
        record.headers().add(new RecordHeader("unique_id", uniqueId.getBytes()));

        producer.send(record, (metadata, exception) -> {
            if (exception == null) {
                System.out.println("Message sent with ID: " + uniqueId);
            } else {
                exception.printStackTrace();
            }
        });

        producer.close();
    }
}
