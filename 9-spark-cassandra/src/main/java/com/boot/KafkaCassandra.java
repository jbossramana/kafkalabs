package com.boot;


import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.*;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.boot.model.UserBean;
import com.datastax.spark.connector.japi.CassandraJavaUtil;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import scala.Tuple2;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class KafkaCassandra implements Serializable {

	private static final long serialVersionUID = 1L;

	public static void main(String[] argv) throws Exception {

		String keySpaceName = "test01";
		String tableName = "users";

		JSONParser parser = new JSONParser();
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

		// Configure Spark to connect to Kafka running on local machine
		Map<String, Object> kafkaParams = new HashMap<>();
		kafkaParams.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		kafkaParams.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
				"org.apache.kafka.common.serialization.StringDeserializer");
		kafkaParams.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
				"org.apache.kafka.common.serialization.StringDeserializer");
		kafkaParams.put(ConsumerConfig.GROUP_ID_CONFIG, "group1");
		kafkaParams.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
		kafkaParams.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);

		// Configure Spark to listen messages in topic test
		Collection<String> topics = Arrays.asList("test01");

		SparkConf conf = new SparkConf().setMaster("local[2]").setAppName("KafkaCassandra");

		conf.set("spark.cassandra.connection.host", "127.0.0.1");
		conf.set("spark.cassandra.connection.port", "9042");
		conf.set("spark.cassandra.connection.timeout_ms", "5000");
		conf.set("spark.cassandra.read.timeout_ms", "200000");

		// Creating spark context
		JavaSparkContext javaSparkContext = new JavaSparkContext(conf);

		// Creating Straming context by using spark context. Read messages in batch of 30 seconds
		JavaStreamingContext jssc = new JavaStreamingContext(javaSparkContext, Durations.seconds(30));

		// Start reading messages from Kafka and get DStream
		final JavaInputDStream<ConsumerRecord<String, String>> stream = KafkaUtils.createDirectStream(jssc,
				LocationStrategies.PreferConsistent(),
				ConsumerStrategies.<String, String> Subscribe(topics, kafkaParams));

		// Read value of each message from Kafka and return it
		JavaDStream<String> lines = stream.map(new Function<ConsumerRecord<String, String>, String>() {

			@Override
			public String call(ConsumerRecord<String, String> kafkaRecord) throws Exception {
				return kafkaRecord.value();
			}
		});

		// Read each message and cast to object. 
		lines.foreachRDD((VoidFunction<JavaRDD<String>>) rdd -> {

			if (rdd.count() != 0) {
				System.out.println(rdd.first());

					JSONObject jsonObj = (JSONObject) parser.parse(rdd.first());
					UserBean userObj = mapper.readValue(jsonObj.toString(), UserBean.class);
					userObj.setId(UUID.randomUUID());
					List<UserBean> list = new ArrayList<>();
					list.add(userObj);
					JavaRDD<UserBean> userRDD = javaSparkContext.parallelize(list);
					
					// Save into cassandra 
					CassandraJavaUtil.javaFunctions(userRDD)
							.writerBuilder(keySpaceName, tableName, CassandraJavaUtil.mapToRow(UserBean.class))
							.saveToCassandra();

			}

		});

		jssc.start();
		jssc.awaitTermination();

	}

}