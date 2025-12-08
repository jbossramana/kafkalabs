package demo.streams;

import java.util.Properties;
import java.util.Arrays;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.SessionWindows;
import org.apache.kafka.streams.kstream.TimeWindowedKStream;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

public class WordCountAppTimeWindow {

    public Topology createTopology(){
        StreamsBuilder builder = new StreamsBuilder();
        // 1 - stream from Kafka
        // <null,"Kafka Kafka Streams">
        KStream<String, String> textLines = builder.stream("word-count-input");
        // store the result, use KTable
        KTable<Windowed<String>, Long> wordCounts = textLines
                // 2 - map values to lowercase
        		// <null, "kafka kafka streams">
                .mapValues(textLine -> textLine.toLowerCase())
                 // 3 - flatmap values split by space
                // <null,"kafka">,<null,"kafka">, <null,"streams">
                .flatMapValues(textLine -> Arrays.asList(textLine.split("\\W+")))
                // 4 - select key to apply a key (we discard the old key)
                // <"kafka","kafka">,<"kafka","kafka">, <"streams","streams">
                .selectKey((key, word) -> word)
                // 5 - group by key before aggregation
                // (<"kafka","kafka">,<"kafka","kafka">), (<"streams","streams">)
                .groupByKey()
                // TimeWindows
                .windowedBy(TimeWindows.of(10000))
        
                // 6 - count occurences
                // <"kafka",2>, <"streams",1>
			
                .count();
        
    
        wordCounts.toStream().foreach((k, v) -> System.out.println("Key= " + k + " Value= " + v));

     
        return builder.build();
    }

    public static void main(String[] args) {
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "wordcount-application");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        WordCountAppTimeWindow wordCountApp = new WordCountAppTimeWindow();

        KafkaStreams streams = new KafkaStreams(wordCountApp.createTopology(), config);
        streams.start();

        // shutdown hook to correctly close the streams application
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

        // Update:
        // print the topology every 10 seconds for learning purposes
        while(true){
            streams.localThreadsMetadata().forEach(data -> System.out.println(data));
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                break;
            }
        }


    }
}
