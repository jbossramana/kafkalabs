package demo.kafka.consumer;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import demo.kafka.StockAppConstants;
import demo.kafka.producer.model.StockPrice;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class AdvanceStockPriceConsumer {
    private static final Logger logger =
            LoggerFactory.getLogger(AdvanceStockPriceConsumer.class);


    private static Consumer<String, StockPrice> createConsumer(final SeekTo seekTo,
                                                               final long location) {
        final Properties props = initProperties();

        // Create the consumer using props.
        final Consumer<String, StockPrice> consumer =
                new KafkaConsumer<>(props);

        // Create SeekToConsumerRebalanceListener and assign it to consumerRebalanceListener
        final ConsumerRebalanceListener consumerRebalanceListener =
                new SeekToConsumerRebalanceListener(consumer, seekTo, location);

        // Subscribe to the topic.
        consumer.subscribe(Collections.singletonList(
                StockAppConstants.TOPIC), consumerRebalanceListener);
        return consumer;
    }


    private static Properties initProperties() {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                StockAppConstants.BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG,
                "KafkaExampleConsumer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        //Custom Deserializer
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StockDeserializer.class.getName());
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        
     //   props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
     //   props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 5000);
        
        		return props;
    }


    private static void runConsumer(final SeekTo seekTo, final long location,
                                    final int readCountStatusUpdate) throws InterruptedException {
        final Map<String, StockPrice> map = new HashMap<>();
        try (final Consumer<String, StockPrice> consumer
                     = createConsumer(seekTo, location)) {
          
        	int readCount = 0;
            while (true) {
                pollRecordsAndProcess(readCountStatusUpdate, consumer, map, readCount);
                readCount ++;
            }
        }
    }


    private static void pollRecordsAndProcess(
            int readCountStatusUpdate,
            Consumer<String, StockPrice> consumer,
            Map<String, StockPrice> map, int readCount) {

        final ConsumerRecords<String, StockPrice> consumerRecords =
                consumer.poll(100);

        try {
            startTransaction();         //Start DB Transaction

            //Commit the Kafka offset
            consumer.commitSync();

            //Process the records
            processRecords(map, consumerRecords);

            commitTransaction();        //Commit DB Transaction

        } catch (CommitFailedException ex) {
            logger.error("Failed to commit sync to log", ex);
            rollbackTransaction();      //Rollback Transaction
        } catch (DatabaseException dte) {
            logger.error("Failed to write to DB", dte);
            rollbackTransaction();      //Rollback Transaction
        }


        if (readCount % readCountStatusUpdate == 0) {
            displayRecordsStatsAndStocks(map, consumerRecords);
        }
    }

    private static void displayRecordsStatsAndStocks(
            final Map<String, StockPrice> stockPriceMap,
            final ConsumerRecords<String, StockPrice> consumerRecords) {
        System.out.printf("New ConsumerRecords par count %d count %d\n",
                consumerRecords.partitions().size(),
                consumerRecords.count());
        stockPriceMap.forEach((s, stockPrice) ->
                System.out.printf("ticker %s price %d.%d \n",
                        stockPrice.getName(),
                        stockPrice.getDollars(),
                        stockPrice.getCents()));
        System.out.println();
    }


    public static void main(String... args) throws Exception {

        SeekTo seekTo = SeekTo.END; // SeekTo what?
        long location = -1; // Location to seek to if SeekTo.Location
        int readCountStatusUpdate = 100;
        if (args.length >= 1) {
            seekTo = SeekTo.valueOf(args[0].toUpperCase());
            if (seekTo.equals(SeekTo.LOCATION)) {
                location = Long.parseLong(args[1]);
            }
        }
        if (args.length == 3) {
            readCountStatusUpdate = Integer.parseInt(args[2]);
        }
        runConsumer(seekTo, location, readCountStatusUpdate);
    }


    private static void commitTransaction() {
    }

    private static void rollbackTransaction() {
    }

    private static void startTransaction() {

    }

    private static void processRecords(Map<String, StockPrice> map,
                                       ConsumerRecords<String, StockPrice> consumerRecords) {
        consumerRecords.forEach(record -> {
            map.put(record.key(), record.value());
        });
    }


}
