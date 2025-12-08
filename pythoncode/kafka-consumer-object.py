import json
from confluent_kafka import Consumer

from Customer import Customer

consumer = Consumer({
    "bootstrap.servers": "localhost:9092",
    "group.id": "customer-group",
    "auto.offset.reset": "earliest"
})

consumer.subscribe(["customer-topic"])

while True:
    msg = consumer.poll(1.0)
    if msg is None:
        continue

    data_dict = json.loads(msg.value().decode("utf-8"))

    cust = Customer(data_dict["customer_id"], data_dict["name"], data_dict["role"])

    print("Customer received from Kafka:")
    print(cust.customer_id, cust.name, cust.role)
    break

consumer.close()