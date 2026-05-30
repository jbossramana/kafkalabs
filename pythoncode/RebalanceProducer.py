from confluent_kafka import Producer
import time

def delivery_report(err, msg):
    if err is not None:
        print(f"Delivery failed: {err}")
    else:
        print(f"Message delivered to {msg.topic()} [{msg.partition()}] @ offset {msg.offset()}")

config = {
    "bootstrap.servers": "localhost:9092",
    "client.id": "python-producer"
}

producer = Producer(config)

topic = "test-topic"

for i in range(10):
    message_value = f"Hello Kafka {i}"
    print(f"Producing -> {message_value}")

    producer.produce(
        topic,
        value=message_value.encode("utf-8"),
        callback=delivery_report
    )

    # Flush buffer if full
    producer.poll(0)

# Ensure all outstanding messages are delivered
producer.flush()
print("All messages written.")
