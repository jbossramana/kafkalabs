package demo;


import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
 
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
 
import scala.Tuple2;
import java.util.*;
 
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
 
public class JavaSparkApp {
 
    public static void main(String[] args) throws InterruptedException, StreamingQueryException {
 
        //Set log level to warn
        Logger.getLogger("org").setLevel(Level.WARN);
 
        // Create a local StreamingContext and batch interval of 10 second
        // local[*]  to run worker node threads on all available local logical cores 
        SparkConf conf = new SparkConf().setMaster("local").setAppName("Kafka Spark Integration");
        JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(10));
 
        //Define Kafka parameter
        Map<String, Object> kafkaParams = new HashMap<String, Object>();
        kafkaParams.put("bootstrap.servers", "localhost:9092");
        kafkaParams.put("key.deserializer", StringDeserializer.class);
        kafkaParams.put("value.deserializer", StringDeserializer.class);
        kafkaParams.put("group.id", "spark1");
        // Automatically reset the offset to the earliest offset
        kafkaParams.put("auto.offset.reset", "earliest");
        kafkaParams.put("enable.auto.commit", false);
 
        //Define a list of Kafka topic to subscribe
        Collection<String> topics = Arrays.asList("spark-topic");
 
        //Create an input Dstream which consume message from Kafka topics
        JavaInputDStream<ConsumerRecord<String, String>> stream;
        stream = KafkaUtils.createDirectStream(jssc,LocationStrategies.PreferConsistent(),ConsumerStrategies.Subscribe(topics, kafkaParams));
 
 
        // Read value of each message from Kafka
        JavaDStream<String> lines = stream.map((Function<ConsumerRecord<String, String>, String>) kafkaRecord -> kafkaRecord.value());
        lines.print();
        // Split message into words
        JavaDStream<String> words = lines.flatMap((FlatMapFunction<String, String>) line -> Arrays.asList(line.split(" ")).iterator());
 
        words.print();
        // Take every word and return Tuple with (word,1)
        JavaPairDStream<String,Integer> wordMap = words.mapToPair((PairFunction<String, String, Integer>) word -> new Tuple2<>(word,1));
 
        wordMap.print();
        
        // Count occurance of each word
        JavaPairDStream<String,Integer> wordCount = wordMap.reduceByKey((Function2<Integer, Integer, Integer>) (first, second) -> first+second);
 
        //Print the word count
        wordCount.print();
 
        // Start the computation
        jssc.start();
        jssc.awaitTermination();
    }
 
}