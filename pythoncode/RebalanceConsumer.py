from confluent_kafka import Consumer

from RebalanceListener import RebalanceListener

config = {
    "bootstrap.servers": "localhost:9092",
    "group.id": "python-group-test",
    "enable.auto.commit": False
}

consumer = Consumer(config)

listener = RebalanceListener(consumer)

consumer.subscribe(["test-topic"], on_assign=listener.on_assign, on_revoke=listener.on_revoke)

print("Listening... CTRL+C to quit")

try:
    while True:
        msg = consumer.poll(1.0)

        if msg is None:
            continue
        if msg.error():
            print(f"Error: {msg.error()}")
            continue

        print(f"Read Message: topic={msg.topic()}, partition={msg.partition()}, offset={msg.offset()}, value={msg.value()}")

        # store next offset (like Java logic)
        listener.add_offset(msg.topic(), msg.partition(), msg.offset() + 1)

except KeyboardInterrupt:
    print("Exiting...")

finally:
    consumer.close()
