package demo.kafka.producer.support;

import io.advantageous.boon.core.Lists;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import demo.kafka.StockAppConstants;
import demo.kafka.producer.model.StockPrice;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class StockPriceKafkaProducer {

    private static Producer<String, StockPrice>
                                    createProducer() {
        final Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                StockAppConstants.BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "StockPriceKafkaProducer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StockPriceSerializer.class.getName());
        props.put(ProducerConfig.LINGER_MS_CONFIG, 100);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG,  16_384 * 4);
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG,
                StockPricePartitioner.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        
        props.put("importantStocks", "A1,C1");
        return new KafkaProducer<>(props);
    }



    private static final Logger logger =
            LoggerFactory.getLogger(StockPriceKafkaProducer.class);



    public static void main(String... args)
            throws Exception {
        //Create Kafka Producer
        final Producer<String, StockPrice> producer = createProducer();
        //Create StockSender list
        final List<StockSender> stockSenders = getStockSenderList(producer);

        //Create a thread pool so every stock sender gets it own.
        final ExecutorService executorService =
                Executors.newFixedThreadPool(stockSenders.size());

        //Run each stock sender in its own thread.
        stockSenders.forEach(executorService::submit);


        //Register nice shutdown of thread pool, then flush and close producer.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            executorService.shutdown();
            try {
                executorService.awaitTermination(200, TimeUnit.MILLISECONDS);
                logger.info("Flushing and closing producer");
                producer.flush();
                producer.close(Duration.ofMillis(10000));
            } catch (InterruptedException e) {
                logger.warn("shutting down", e);
            }
        }));
    }




    private static List<StockSender> getStockSenderList(
            final Producer<String, StockPrice> producer) {
        return Lists.list(
                new StockSender(StockAppConstants.TOPIC,
                        new StockPrice("A1", 100, 99),
                        new StockPrice("A1", 50, 10),
                        producer,
                        100, 1000
                ),
                new StockSender(
                        StockAppConstants.TOPIC,
                        new StockPrice("B1", 1000, 99),
                        new StockPrice("B1", 50, 0),
                        producer,
                        100, 1000                ),
                new StockSender(
                        StockAppConstants.TOPIC,
                        new StockPrice("C1", 100, 99),
                        new StockPrice("C1", 50, 10),
                        producer,
                        100, 1000
                ),
                new StockSender(
                        StockAppConstants.TOPIC,
                        new StockPrice("D1", 500, 99),
                        new StockPrice("D1", 400, 10),
                        producer,
                        100, 1000
                ),
                new StockSender(
                        StockAppConstants.TOPIC,
                        new StockPrice("E1", 100, 99),
                        new StockPrice("E1", 50, 10),
                        producer,
                        100, 1000               
                        )
                
                        );

    }








}














