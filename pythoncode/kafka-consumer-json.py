import json
from confluent_kafka import Consumer

consumer = Consumer({
    "bootstrap.servers": "localhost:9092",
    "group.id": "json-group",
    "auto.offset.reset": "earliest"
})

consumer.subscribe(["json-topic"])

while True:
    msg = consumer.poll(1.0)
    if msg is None:
        continue

    data = json.loads(msg.value().decode("utf-8"))
    print("Received:", data)
