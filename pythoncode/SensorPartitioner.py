

import socket
import sys
import math
import mmh3 
from confluent_kafka import Producer

# --- CONFIGURATION ---
BOOTSTRAP_SERVERS = 'localhost:9092'
TOPIC_NAME = "SensorTopic1"
# The key that gets routed to the special 70% segment
SPEED_SENSOR_NAME = "TSS" 

# *** IMPORTANT: Set this to the actual number of partitions for your topic! ***
# Example: Using 10 partitions for clear segmentation
NUM_PARTITIONS = 10 

# Kafka Producer configuration
conf = {
    'bootstrap.servers': BOOTSTRAP_SERVERS,
    'client.id': socket.gethostname()
}

# --- Partitioning Utility Functions ---

def to_positive(data):
    """
    Simulates Kafka's Utils.toPositive (bitwise AND with 0x7fffffff).
    Ensures the hash result is positive before the modulo operation.
    """
    return data & 0x7fffffff

def sensor_partitioner(key_bytes, value_bytes, key_str, num_partitions, speed_sensor_name):
    """
    Implements the 70/30 partition segmentation logic.

    :param key_bytes: The message key as bytes.
    :param value_bytes: The message value as bytes.
    :param key_str: The message key as a string.
    :param num_partitions: Total number of partitions on the topic.
    :param speed_sensor_name: The special key (TSS).
    :return: The target partition ID (integer).
    """
    
    # 1. Calculate the partition segment boundary (70% of partitions)
    # The 'sp' (Special Partition) segment size is the first 70% of partitions (e.g., 7 of 10)
    sp_size = math.floor(num_partitions * 0.7)
    
    # Ensure there's at least one partition in each group
    if sp_size == 0 or sp_size == num_partitions:
        print("Warning: Partition segmentation ineffective. Using default hashing.")
        hash_val = mmh3.hash(key_bytes)
        return to_positive(hash_val) % num_partitions
        
    p = 0
    
    # 2. Logic for the Special Group (TSS messages)
    if key_str == speed_sensor_name:
        # These messages are routed to the segment from 0 up to (sp_size - 1).
        # Partitioning based on VALUE hash (as per your Java logic)
        hash_val = mmh3.hash(value_bytes)
        p = to_positive(hash_val) % sp_size
        print(f"   -> SPECIAL KEY ('{key_str}'): Value-based hash mapped to partition {p} (Range 0-{sp_size-1})")
        
    # 3. Logic for the Normal Group (All other keys)
    else:
        # These messages are routed to the segment from sp_size up to (num_partitions - 1).
        # Partitioning based on KEY hash
        
        normal_segment_size = num_partitions - sp_size
        # Calculate the hash remainder within the normal segment size
        hash_val = mmh3.hash(key_bytes)
        offset_in_segment = to_positive(hash_val) % normal_segment_size
        
        # Add the offset (sp_size) to shift the partition ID into the normal segment
        p = offset_in_segment + sp_size
        print(f"   -> NORMAL KEY ('{key_str}'): Key-based hash mapped to partition {p} (Range {sp_size}-{num_partitions-1})")
    
    return p

# --- Delivery Report Callback (for status updates) ---
def delivery_report(err, msg):
    if err is not None:
        if err.code() != 0: 
            print(f'❌ Message delivery failed: {err}')
    # Note: We omit the success print here to keep the output cleaner with the partitioner logs

# --- Main Producer Execution ---
def produce_data():
    
    producer = Producer(conf)

    # Calculate segment info for printing
    sp_size = math.floor(NUM_PARTITIONS * 0.7)
    normal_start = sp_size
    normal_end = NUM_PARTITIONS - 1

    print(f"--- Sensor Partitioner Segmentation ---")
    print(f"Total Partitions: {NUM_PARTITIONS}")
    print(f"Special Group Size (70%): {sp_size}. Range: 0 to {sp_size-1}")
    print(f"Normal Group Size (30%): {NUM_PARTITIONS - sp_size}. Range: {normal_start} to {normal_end}")
    
    
    # Example Data: 5 Special Keys (TSS) and 5 Normal Keys (SSP)
    messages = []
    for i in range(5):
        # Special keys (TSS)
        messages.append((SPEED_SENSOR_NAME, f"ValueTSS_{i}")) 
        # Normal keys (SSP)
        messages.append((f"SSP_{i}", f"ValueSSP_{i}")) 

    print("\n--- Producing 10 Messages ---")

    for key_str, value_str in messages:
        key_bytes = key_str.encode('utf-8')
        value_bytes = value_str.encode('utf-8')
        
        # 1. Calculate the partition using the custom logic
        partition_id = sensor_partitioner(
            key_bytes, 
            value_bytes, 
            key_str, 
            NUM_PARTITIONS, 
            SPEED_SENSOR_NAME
        )
        
        # 2. Produce the message to the calculated partition
        producer.produce(
            topic=TOPIC_NAME,
            key=key_bytes,
            value=value_bytes,
            partition=partition_id, 
            callback=delivery_report
        )
        producer.poll(0)
        
    producer.flush()
    print("\n✅ Production Complete. Check Kafka logs for message routing.")

if __name__ == '__main__':
    # Ensure the 'SensorTopic1' exists with 10 partitions.
    produce_data()