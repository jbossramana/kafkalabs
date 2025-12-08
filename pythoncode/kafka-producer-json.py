import json
from confluent_kafka import Producer

producer = Producer({"bootstrap.servers": "localhost:9092"})
topic = "json-topic"

data = {
    "id": 101,
    "name": "Ram",
    "role": "Engineer"
}

json_bytes = json.dumps(data).encode("utf-8")

producer.produce(topic, value=json_bytes)
producer.flush()
print("JSON object sent!")
