import json
from confluent_kafka import Producer

from Customer import Customer

producer = Producer({"bootstrap.servers": "localhost:9092"})
topic = "customer-topic"

cust = Customer(101, "Ram", "Engineer")
json_data = json.dumps(cust.to_dict()).encode("utf-8")

producer.produce(topic, value=json_data)
producer.flush()

print("Customer object sent!")
