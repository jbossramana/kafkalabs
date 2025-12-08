import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.time.Duration;
import java.util.*;

public class IdempotentConsumer {
    private static Set<String> processedIds = new HashSet<>(); // Use Redis/DB in production

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "idempotent-consumer-group");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("first-topic"));

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
            for (ConsumerRecord<String, String> record : records) {
                String uniqueId = null;
                if (record.headers().lastHeader("unique_id") != null) {
                    uniqueId = new String(record.headers().lastHeader("unique_id").value());
                }

                if (uniqueId != null) {
                    if (processedIds.contains(uniqueId)) {
                        System.out.println("Duplicate message skipped: " + uniqueId);
                        continue; // Skip already processed
                    } else {
                        processedIds.add(uniqueId);
                        // TODO: Process the message
                        System.out.println("Processed message: " + record.value() + " with ID: " + uniqueId);
                    }
                } else {
                    System.out.println("Missing unique_id header. Processing anyway...");
                    // Optionally handle missing header
                }
            }
        }
    }
}
