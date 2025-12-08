controller and server
=====================

1. kafka-storage.bat random-uuid

kafka-storage.bat format -t wZn-8oPMQhywdQQFJzHetw --standalone -c ..\..\standalone\controller.properties
kafka-storage.bat format -t wZn-8oPMQhywdQQFJzHetw -c ..\..\standalone\broker.properties

2. start controller and server
kafka-server-start.bat ..\..\standalone\controller.properties
kafka-server-start.bat ..\..\standalone\broker.properties

3. Create a topic

> kafka-topics.bat --bootstrap-server localhost:9092 --create --topic test_topic --partitions 3 --replication-factor 1

4. To Describe a topic

> kafka-topics.bat --bootstrap-server localhost:9092 --describe --topic test_topic 

5. To display the logs in readable format

kafka-run-class.bat kafka.tools.DumpLogSegments --deep-iteration --print-data-log --files  D:/newkafka/kraft-combined-logs/test_topic-0/00000000000000000000.log

