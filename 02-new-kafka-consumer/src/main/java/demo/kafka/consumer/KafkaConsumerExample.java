package demo.kafka.consumer;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

public class KafkaConsumerExample {

    private final static String TOPIC = "first-topic";
    private final static String BOOTSTRAP_SERVERS =
            "localhost:9092";


    private static Consumer<Long, String> createConsumer() {
        final Properties props = new Properties();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                                    BOOTSTRAP_SERVERS);

        props.put(ConsumerConfig.GROUP_ID_CONFIG,
                                    "KafkaExampleConsumer");

        props.put(ConsumerConfig.CLIENT_ID_CONFIG,"abc");
        		
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                LongDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());

        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        
          props.put("auto.offset.reset","earliest");  //from the last committed offset
        //  props.put("auto.offset.reset","latest");  

        // Create the consumer using props.
        final Consumer<Long, String> consumer =
                                    new KafkaConsumer<>(props);

        // Subscribe to the topic.
        consumer.subscribe(Collections.singletonList(TOPIC));
        
       
        return consumer;
    }




    static void runConsumer() throws InterruptedException {
        final Consumer<Long, String> consumer = createConsumer();
        try {
            final int giveUp = 1000; int noRecordsCount = 0;

            while (true) {
                final ConsumerRecords<Long, String> consumerRecords =
                        consumer.poll(Duration.ofMillis(1000));

                if (consumerRecords.count() == 0) {
                    noRecordsCount++;
                    if (noRecordsCount > giveUp) break;
                    else continue;
                }

                System.out.printf("New ConsumerRecords partitions count %d count %d\n",
                        consumerRecords.partitions().size(),
                        consumerRecords.count());

                consumerRecords.forEach(record -> {
                    System.out.printf("Consumer Record:(%d, %s, %d, %d)\n",
                            record.key(), record.value(),
                            record.partition(), record.offset());
                });
                Thread.sleep(100);
               // consumer.commitAsync();
                
                consumer.commitAsync(new OffsetCommitCallback() {

                    @Override
                    public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception e) {
                        if (e != null) {
                        	e.printStackTrace();
                        }
                    }
                });
            }
        }
                
            catch (Exception e) 
            {
                throw new KafkaException(e);
            }
            
       
        
        finally {
            consumer.close();
        }

        System.out.println("DONE");
    }


    public static void main(String... args) throws Exception {
        runConsumer();
    }


}
