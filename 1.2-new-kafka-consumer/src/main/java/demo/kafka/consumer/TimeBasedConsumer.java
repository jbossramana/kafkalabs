package demo.kafka.consumer;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import org.apache.kafka.common.TopicPartition;

public class TimeBasedConsumer {
  private static long kPollTimeout = 100;
  private static int kNumRecordsToProcess = 10;

  public static void main(String[] args) {
    String topic = "first-topic";
    Long startTimestamp = 1573045101420L;
    Properties properties = new Properties();
    properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
    properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
    properties.put(ConsumerConfig.GROUP_ID_CONFIG, "testgroup");
    properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);
    SeekToTimeOnRebalance seekToTimeOnRebalance = new SeekToTimeOnRebalance(consumer, startTimestamp);
   
    // subscribe to the input topic and listen for assignments.
    consumer.subscribe(Arrays.asList(topic), seekToTimeOnRebalance);
    
    int numRecords = 0;
    // poll and process the records.
    while (numRecords < kNumRecordsToProcess) {
      ConsumerRecords<String, String> records = consumer.poll(kPollTimeout );
      for (ConsumerRecord<String, String> record : records) {
        // The offsetsForTimes API returns the earliest offset in a topic-partition with a timestamp
        // greater than or equal to the input timestamp. There could be messages following that offset
        // with timestamps lesser than the input timestamp. Let's skip such messages.
        if (record.timestamp() < startTimestamp) {
          System.out.println("Skipping out of order record with key " + record.key() +
                             " timestamp " + record.timestamp());
          continue;
        }
        numRecords++;
        System.out.println("record value " + record.value() +
                           "record timestamp " + record.timestamp() +
                           "record offset " + record.offset());
      }
    }
    consumer.close();
  }
  
  public static class SeekToTimeOnRebalance implements ConsumerRebalanceListener {
    private Consumer<?, ?> consumer;
    private final Long startTimestamp;

    public SeekToTimeOnRebalance(Consumer<?, ?> consumer, Long startTimestamp) {
      this.consumer = consumer;
      this.startTimestamp = startTimestamp;
    }

    @Override
    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
      Map<TopicPartition, Long> timestampsToSearch = new HashMap<>();
      for (TopicPartition partition : partitions) {
        timestampsToSearch.put(partition,  startTimestamp);
      }
      // for each assigned partition, find the earliest offset in that partition with a timestamp
      // greater than or equal to the input timestamp
      Map<TopicPartition, OffsetAndTimestamp> outOffsets = consumer.offsetsForTimes(timestampsToSearch);
      for (TopicPartition partition : partitions) {
        Long seekOffset = outOffsets.get(partition).offset();
        Long currentPosition = consumer.position(partition);
        // seek to the offset returned by the offsetsForTimes API
        // if it is beyond the current position
        if (seekOffset.compareTo(currentPosition) > 0) {
          consumer.seek(partition, seekOffset);
        }
      }
    }
 
    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
    }

  }

}