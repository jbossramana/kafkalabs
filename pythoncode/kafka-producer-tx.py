from confluent_kafka import Producer
import socket
import sys

# --- Configuration ---
BOOTSTRAP_SERVERS = 'localhost:9092'
SINGLE_TOPIC_NAME = 'customer_updates'
# REQUIRED for transactions
TRANSACTIONAL_ID = 'customer-data-transaction-producer-1' 

# Kafka Producer configuration
conf = {
    'bootstrap.servers': BOOTSTRAP_SERVERS,
    'client.id': socket.gethostname(),
    'transactional.id': TRANSACTIONAL_ID, 
    'enable.idempotence': True # REQUIRED for transactions
}

# --- Main Transactional Producer Logic ---
def produce_single_topic_transactions():
    """Demonstrates sending two separate transactions to a single topic."""

    try:
        producer = Producer(conf)
    except Exception as e:
        print(f"Error creating Kafka Producer: {e}")
        sys.exit(1)

    # 1. Initialize Transactions
    try:
        producer.init_transactions()
        print(f"✅ Transactional producer initialized with ID: {TRANSACTIONAL_ID}")
    except Exception as e:
        print(f"❌ Failed to initialize transactions: {e}")
        sys.exit(1)

    # ------------------------------------------------------------------
    # --- TRANSACTION 1: Account Creation ---
    # ------------------------------------------------------------------
    
    print("\n--- Starting TRANSACTION 1 (Account Creation) ---")
    
    producer.begin_transaction()
    
    try:
        # Message 1: Record creation for customer 123
        key_1 = 'customer_123'
        value_1 = '{"status": "Created", "details": "Initial Account Setup"}'
        
        producer.produce(
            topic=SINGLE_TOPIC_NAME,
            key=key_1.encode('utf-8'),
            value=value_1.encode('utf-8')
        )
        print(f"   Produced (T1): Key {key_1}, Message 1/1")
            
        producer.flush(timeout=5)
        
        # 3. Commit the Transaction
        producer.commit_transaction()
        print("✅ TRANSACTION 1 COMMITTED successfully. Account 123 is now visible.")
        
    except Exception as e:
        # 4. Abort on failure
        print(f"❌ TRANSACTION 1 FAILED: {e}. Aborting transaction.")
        producer.abort_transaction()


    # ------------------------------------------------------------------
    # --- TRANSACTION 2: Address and Status Update ---
    # ------------------------------------------------------------------
    
    print("\n--- Starting TRANSACTION 2 (Address/Status Update) ---")
    
    producer.begin_transaction()
    
    try:
        # Message 1: Address Update for customer 123
        key_2_1 = 'customer_123'
        value_2_1 = '{"update": "Address", "data": "101 Main St"}'
        
        producer.produce(
            topic=SINGLE_TOPIC_NAME,
            key=key_2_1.encode('utf-8'),
            value=value_2_1.encode('utf-8')
        )
        print(f"   Produced (T2): Key {key_2_1}, Message 1/2 (Address Update)")

        # Message 2: Status Change for customer 123
        key_2_2 = 'customer_123'
        value_2_2 = '{"update": "Status", "data": "Active_Premium"}'
        
        producer.produce(
            topic=SINGLE_TOPIC_NAME,
            key=key_2_2.encode('utf-8'),
            value=value_2_2.encode('utf-8')
        )
        print(f"   Produced (T2): Key {key_2_2}, Message 2/2 (Status Change)")
        
        producer.flush(timeout=5)
        
        # 3. Commit the Transaction
        # Both updates are committed together or neither is.
        producer.commit_transaction()
        print("✅ TRANSACTION 2 COMMITTED successfully. Address and Status update are visible.")
        
    except Exception as e:
        # 4. Abort on failure
        print(f"❌ TRANSACTION 2 FAILED: {e}. Aborting transaction.")
        producer.abort_transaction()

# --- Run the Code ---
if __name__ == '__main__':
    # Ensure the topic 'customer_updates' exists and the broker is running.
    produce_single_topic_transactions()