from confluent_kafka import Consumer, KafkaException
import json
import sys

# --- Configuration ---
BOOTSTRAP_SERVERS = 'localhost:9092'
SINGLE_TOPIC_NAME = 'customer_updates' # Topic used in the producer example
GROUP_ID = 'transactional-reader-group'

# Kafka Consumer configuration
conf = {
    'bootstrap.servers': BOOTSTRAP_SERVERS,
    'group.id': GROUP_ID,
    'auto.offset.reset': 'earliest', # Start reading from the beginning if no offset is found
    'enable.auto.commit': True,
    
    # --- CRITICAL SETTING FOR TRANSACTIONS ---
    # This ensures the consumer only reads messages that were part of a successfully 
    # committed transaction. It ignores messages from aborted transactions.
    'isolation.level': 'read_committed' 
}

# --- Main Consumer Logic ---
def consume_transactional_messages():
    """Reads messages only after they have been committed by the producer."""
    
    try:
        consumer = Consumer(conf)
    except Exception as e:
        print(f"Error creating Kafka Consumer: {e}")
        sys.exit(1)

    try:
        # Subscribe to the topic
        consumer.subscribe([SINGLE_TOPIC_NAME])
        print(f"🚀 Consumer started for topic '{SINGLE_TOPIC_NAME}' in group '{GROUP_ID}'")
        print(f"   Isolation Level: read_committed. Waiting for COMMITTED messages... (Press Ctrl+C to stop)")
        
        # --- Consume Loop ---
        while True:
            # Poll for new messages (timeout 1.0 second)
            msg = consumer.poll(1.0)
            
            if msg is None:
                continue
            
            if msg.error():
                if msg.error().code() == KafkaException._PARTITION_EOF:
                    # End of partition event (normal)
                    continue
                else:
                    # Log other errors
                    print(f"❌ Consumer error: {msg.error()}")
                    break

            # Process the committed message
            key = msg.key().decode('utf-8') if msg.key() else 'None'
            value_json = msg.value().decode('utf-8')
            
            print("---")
            print(f"Key: {key}")
            # Attempt to parse as JSON for cleaner output
            try:
                print(f"Value: {json.loads(value_json)}")
            except json.JSONDecodeError:
                print(f"Value: {value_json}")
                
            print(f"Offset: {msg.offset}, Partition: {msg.partition}")

    except KeyboardInterrupt:
        print("\n👋 Stopping consumer...")
        
    finally:
        # Close the consumer connection
        consumer.close()

if __name__ == '__main__':
    consume_transactional_messages()