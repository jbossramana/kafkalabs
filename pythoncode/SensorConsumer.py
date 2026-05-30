import sys
from confluent_kafka import Consumer, TopicPartition, KafkaException
import mysql.connector

TOPIC_NAME = "SensorTopic1"
BOOTSTRAP_SERVERS = 'localhost:9092'
GROUP_ID = 'manual-db-offset-group'

DB_CONFIG = {
    'user': 'root',
    'password': 'root',
    'host': 'localhost',
    'database': 'demo',
}

conf = {
    'bootstrap.servers': BOOTSTRAP_SERVERS,
    'group.id': GROUP_ID,
    'auto.offset.reset': 'earliest',
    'enable.auto.commit': False
}

def get_db_connection():
    try:
        return mysql.connector.connect(**DB_CONFIG)
    except mysql.connector.Error as err:
        print(f"❌ DB Error: {err}")
        sys.exit(1)

def get_offset_from_db(topic_partition):
    conn = get_db_connection()
    last_offset = 0

    try:
        cursor = conn.cursor()
        cursor.execute(
            "SELECT offset FROM tss_offsets WHERE topic_name=%s AND partitions=%s",
            (topic_partition.topic, topic_partition.partition)
        )
        result = cursor.fetchone()
        last_offset = result[0] if result else 0

    finally:
        conn.close()

    return last_offset


def save_and_commit(record, key, value):
    next_offset = record.offset() + 1
    conn = get_db_connection()

    try:
        conn.start_transaction()
        cursor = conn.cursor()

        cursor.execute(
            "INSERT INTO tss_data (skey, svalue) VALUES (%s, %s)",
            (key, value)
        )

        cursor.execute(
            "SELECT offset FROM tss_offsets WHERE topic_name=%s AND partitions=%s",
            (record.topic(), record.partition())
        )
        result = cursor.fetchone()

        if result is None:
            cursor.execute(
                "INSERT INTO tss_offsets (topic_name, partitions, offset) VALUES (%s, %s, %s)",
                (record.topic(), record.partition(), next_offset)
            )
        else:
            cursor.execute(
                "UPDATE tss_offsets SET offset=%s WHERE topic_name=%s AND partitions=%s",
                (next_offset, record.topic(), record.partition())
            )

        conn.commit()
        print(f"✔ Committed Offset {next_offset} | P:{record.partition()} Key:{key}")

    except mysql.connector.Error as err:
        conn.rollback()
        print(f"❌ DB Transaction Failed: {err}")

    finally:
        conn.close()


def run_consumer():

    consumer = Consumer(conf)

    p0 = TopicPartition(TOPIC_NAME, 0)
    p1 = TopicPartition(TOPIC_NAME, 1)
    p2 = TopicPartition(TOPIC_NAME, 2)

    p0.offset = get_offset_from_db(p0)
    p1.offset = get_offset_from_db(p1)
    p2.offset = get_offset_from_db(p2)

    consumer.assign([p0, p1, p2])
    consumer.seek(p0)
    consumer.seek(p1)
    consumer.seek(p2)

    print(f"\n🚀 Consumer Started | Resuming offsets: {p0.offset}, {p1.offset}, {p2.offset}\n")

    try:
        while True:
            msg = consumer.poll(timeout=5.0)

            if msg is None:
                continue

            if msg.error():
                print(f"⚠ Kafka Error: {msg.error()}")
                continue

            key = msg.key().decode("utf-8") if msg.key() else None
            value = msg.value().decode("utf-8") if msg.value() else None

            print(f"📨 Received | P:{msg.partition()} Offset:{msg.offset()} Key:{key} Value:{value}")

            save_and_commit(msg, key, value)

    except KeyboardInterrupt:
        print("\n⛔ Interrupted by User")

    finally:
        consumer.close()
        print("🔒 Consumer Closed")


if __name__ == '__main__':
    run_consumer()
