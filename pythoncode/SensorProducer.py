import socket
from confluent_kafka import Producer
import time
import sys

# --- Configuration ---
TOPIC_NAME = "SensorTopic1"
BOOTSTRAP_SERVERS = 'localhost:9092'
# This constant mimics the 'speed.sensor.name' property used in Java
SPEED_SENSOR_NAME = "TSS" 

# Assuming the custom partitioner routes 'TSS' to a specific partition 
# (e.g., partition 0, as a common Partitioner custom logic)
TARGET_PARTITION = 0

# Kafka Producer configuration
conf = {
    'bootstrap.servers': BOOTSTRAP_SERVERS,
    'client.id': socket.gethostname()
    # Note: confluent-kafka does not accept 'partitioner.class' like Java.
    # We handle partitioning logic directly in the send loop.
}

# --- Delivery Report Callback ---
def delivery_report(err, msg):
    """Called once for each message produced to indicate delivery result."""
    if err is not None:
        # Ignore non-fatal errors like partition EOF
        if err.code() != 0: 
            print(f'❌ Message delivery failed: {err}')
    else:
        key = msg.key().decode('utf-8') if msg.key() else 'None'
        print(f'✅ Delivered Key: {key} to Partition: {msg.partition()} at Offset: {msg.offset()}')

# --- Main Producer Logic ---
def produce_sensor_data():
    """Creates a producer and sends 20 sensor records."""
    
    try:
        producer = Producer(conf)
    except Exception as e:
        print(f"Error creating Kafka Producer: {e}")
        sys.exit(1)

    print(f"🚀 Starting producer for topic '{TOPIC_NAME}'.")

    # 1. Batch 1: Messages with keys 'SSP0' through 'SSP9'
    # These messages will use Kafka's default key-hashing partitioner.
    print("\n--- Producing Batch 1 (Keys SSP0-SSP9: Default Partitioning) ---")
    for i in range(10):
        key = "SSP" + str(i)
        value = "500" + str(i)
        
        producer.produce(
            topic=TOPIC_NAME,
            key=key.encode('utf-8'),
            value=value.encode('utf-8'),
            callback=delivery_report
        )
        producer.poll(0)
        
    # 2. Batch 2: Messages with the specific key 'TSS'
    # This simulates the custom partitioner routing all 'TSS' keys to TARGET_PARTITION (e.g., 0).
    print(f"\n--- Producing Batch 2 (Key: '{SPEED_SENSOR_NAME}': Explicit Partition {TARGET_PARTITION}) ---")
    for i in range(10):
        key = SPEED_SENSOR_NAME  # "TSS"
        value = "500" + str(i)
        
        producer.produce(
            topic=TOPIC_NAME,
            key=key.encode('utf-8'),
            value=value.encode('utf-8'),
            partition=TARGET_PARTITION, # Explicitly route this specific key
            callback=delivery_report
        )
        producer.poll(0)

    # 3. Wait for all buffered messages to be delivered
    print("\n⏳ Waiting for all messages to be delivered...")
    remaining_messages = producer.flush(10) # Wait up to 10 seconds

    if remaining_messages == 0:
        print("\n✅ SimpleProducer Completed.")
    else:
        print(f"\n⚠️ Warning: {remaining_messages} messages failed to deliver.")

# --- Run the Code ---
if __name__ == '__main__':
    # Ensure your Kafka broker is running and the topic 'SensorTopic1' exists.
    produce_sensor_data()