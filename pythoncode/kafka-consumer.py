from confluent_kafka import Consumer, KafkaException
import json
import sys

# --- Configuration ---
BOOTSTRAP_SERVERS = 'localhost:9092'
TOPIC_NAME = 'test_topic'
GROUP_ID = 'simple-cg'


def consume_messages():
    """Consumes messages from Kafka using confluent-kafka."""

    # Kafka Consumer Configuration
    consumer_conf = {
        'bootstrap.servers': BOOTSTRAP_SERVERS,
        'group.id': GROUP_ID,
        'auto.offset.reset': 'earliest',  # Read from beginning if no offset exists
        'enable.auto.commit': True
    }

    consumer = Consumer(consumer_conf)

    # Subscribe to topic
    consumer.subscribe([TOPIC_NAME])

    print(f"🚀 Consumer started for topic '{TOPIC_NAME}' in consumer group '{GROUP_ID}'.")
    print("📩 Waiting for messages... (Press Ctrl+C to stop)\n")

    try:
        while True:
            msg = consumer.poll(1.0)  # Poll waits for messages

            if msg is None:
                continue  # No message yet
            
            if msg.error():
                raise KafkaException(msg.error())

            # Deserialize JSON value
            value = json.loads(msg.value().decode('utf-8'))
            key = msg.key().decode('utf-8') if msg.key() else None

            print("📦 Received Message:")
            print(f"   🗝 Key       : {key}")
            print(f"   📝 Value     : {value}")
            print(f"   📌 Topic     : {msg.topic()}")
            print(f"   🎯 Partition : {msg.partition()}")
            print(f"   🔢 Offset    : {msg.offset()}")
            print("-" * 40)

    except KeyboardInterrupt:
        print("\n👋 Stopping consumer...")

    finally:
        consumer.close()
        print("🛑 Consumer closed.")


if __name__ == "__main__":
    consume_messages()
