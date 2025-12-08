from confluent_kafka import Producer
import json
import time

# --- Configuration ---
BOOTSTRAP_SERVERS = 'localhost:9092'
TOPIC_NAME = 'test_topic'

# Kafka producer configuration
producer_conf = {
    'bootstrap.servers': BOOTSTRAP_SERVERS,
    'client.id': 'python-producer'
}

producer = Producer(producer_conf)


# Optional delivery callback to confirm acknowledgment from broker
def delivery_report(err, msg):
    if err is not None:
        print(f"❌ Delivery failed: {err}")
    else:
        print(f"✅ Delivered to {msg.topic()} [partition {msg.partition()}] @ offset {msg.offset()} | Key={msg.key().decode() if msg.key() else None}")


# --- Data to Send ---
data = [
    ('sensor_A', {'event_id': 101, 'temp': 25.5, 'unit': 'C'}),
    ('sensor_B', {'event_id': 102, 'temp': 72.1, 'unit': 'F'}),
    ('sensor_A', {'event_id': 103, 'temp': 26.0, 'unit': 'C'}),
    ('sensor_C', {'event_id': 104, 'temp': 35.0, 'unit': 'C'}),
    ('sensor_B', {'event_id': 105, 'temp': 73.5, 'unit': 'F'}),
]


print(f"\n🚀 Producing {len(data)} messages to topic '{TOPIC_NAME}'...\n")

# --- Produce Messages ---
for i, (key, value) in enumerate(data):
    producer.produce(
        topic=TOPIC_NAME,
        key=str(key).encode('utf-8'),
        value=json.dumps(value).encode('utf-8'),
        callback=delivery_report
    )

    # Poll triggers the callbacks (important!)
    producer.poll(0.5)
    time.sleep(0.1)

# --- Final Flush ---
print("\n📌 Flushing remaining messages...")
producer.flush(10)
print("🎉 All messages delivered successfully!\n")
