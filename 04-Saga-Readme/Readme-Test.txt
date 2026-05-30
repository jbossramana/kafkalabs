kafka-topics.bat --create --topic new-orders --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1

kafka-topics.bat --create --topic new-payments --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1

kafka-topics.bat --create --topic new-inventory --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1

kafka-topics.bat --create --topic reversed-orders --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1

kafka-topics.bat --create --topic reversed-payments --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1

kafka-topics.bat --create --topic reversed-inventory --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1




Success scenario:

post localhost:8082/inventory

{
"item":"books",
"quantity":200
}

post  localhost:8080/orders

{
"item":"books",
"quantity":10,
"amount":1000,
"address":"chennai",
"paymentMode":"credit card"
}

Failure Scenario:
------------------

Let’s place an order for an item which is not in the inventory.



post  localhost:8080/orders

{
"item":"computer",
"quantity":10,
"amount":30000,
"address":"chennai",
"paymentMode":"credit card"
}


Check the database for all the services to understand the status



