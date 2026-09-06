1. Generate Cluster ID
> kafka-storage.bat random-uuid

2. Format Storage
> kafka-storage.bat format --standalone -t random-uuid -c ..\..\config\server.properties

3. Start Kafka Server
> kafka-server-start.bat ..\..\config\server.properties

4. Create a Topic

> kafka-topics.bat --create --topic order-topic --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1

