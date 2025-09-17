rem create input topic with two partitions
./kafka-topics.bat --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic word-count-input

rem create output topic
./kafka-topics.bat --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic word-count-output

rem launch a Kafka consumer
./kafka-console-consumer --bootstrap-server localhost:9092 \
    --topic word-count-output  \
    --from-beginning \
    --formatter kafka.tools.DefaultMessageFormatter \
    --property print.key=true \
    --property print.value=true \
    --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
    --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer

rem launch the streams application

rem then produce data to it
./kafka-console-producer --broker-list localhost:9092 --topic word-count-input

rem run the  jar files [two applicaitons]
java -jar <your jar here>.jar

rem list all topics that we have in Kafka (so we can observe the internal topics)
bin\windows\kafka-topics.bat --list --zookeeper localhost:2181


kafka-console-consumer.bat --bootstrap-server localhost:9092   --topic word-count-output  
    --from-beginning  --formatter kafka.tools.DefaultMessageFormatter 
    --property print.key=true   --property print.value=true 
        --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer 
    --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
    
    
    kafka-console-producer.bat --broker-list localhost:9092 --topic word-count-input
    
    kafka-topics.bat --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic word-count-output