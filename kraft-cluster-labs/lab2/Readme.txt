Cluster-commands
================

1. kafka-storage.bat random-uuid


kafka-storage.bat format -t wZn-8oPMQhywdQQFJzHetw -c ..\..\cluster\controller-1.properties
kafka-storage.bat format -t wZn-8oPMQhywdQQFJzHetw   -c ..\..\cluster\controller-2.properties
kafka-storage.bat format -t wZn-8oPMQhywdQQFJzHetw -c ..\..\cluster\controller-3.properties

kafka-storage.bat format -t wZn-8oPMQhywdQQFJzHetw  -c ..\..\cluster\broker-1.properties
kafka-storage.bat format -t wZn-8oPMQhywdQQFJzHetw  -c ..\..\cluster\broker-2.properties
kafka-storage.bat format -t wZn-8oPMQhywdQQFJzHetw   -c ..\..\cluster\broker-3.properties

kafka-server-start.bat ..\..\cluster\controller-1.properties
kafka-server-start.bat ..\..\cluster\controller-2.properties
kafka-server-start.bat ..\..\cluster\controller-3.properties

kafka-server-start.bat ..\..\cluster\broker-1.properties
kafka-server-start.bat ..\..\cluster\broker-2.properties
kafka-server-start.bat ..\..\cluster\broker-3.properties

3. Create a topic

> kafka-topics.bat --bootstrap-server localhost:9092 --create --topic test_topic --partitions 3 --replication-factor 1



