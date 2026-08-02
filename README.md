1. Generate Cluster ID
> kafka-storage.bat random-uuid

2. Format Storage
> kafka-storage.bat format --standalone -t <cluster-id> -c ..\..\config\server.properties

3. Start Kafka Server
> kafka-server-start.bat ..\..\config\server.properties

4. Create a Topic

> kafka-topics.bat --create --topic test-topic --bootstrap-server localhost:9092

