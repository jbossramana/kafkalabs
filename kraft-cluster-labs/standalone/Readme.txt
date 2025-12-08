1. Generate a Cluster UUID

You need a unique Cluster ID for your KRaft cluster. The shell script has a Windows 
batch equivalent.

> kafka-storage.bat random-uuid

2. Format Log Directories (Initialize Storage)

> kafka-storage.bat format --standalone -t  u4u8EfwBTqCxvUWs8jn6Tg  -c ..\..\config\server.properties

3.  Start the server

> kafka-server-start.bat ..\..\config\server.properties

3. Create a topic

> kafka-topics.bat --bootstrap-server localhost:9092 --create --topic test_topic --partitions 3 --replication-factor 1

4. To Describe a topic

> kafka-topics.bat --bootstrap-server localhost:9092 --describe --topic test_topic 

5. To display the logs in readable format

kafka-run-class.bat kafka.tools.DumpLogSegments --deep-iteration --print-data-log --files  D:/newkafka/kraft-combined-logs/test_topic-0/00000000000000000000.log
