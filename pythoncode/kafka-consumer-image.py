from confluent_kafka import Consumer

consumer = Consumer({
    "bootstrap.servers": "localhost:9092",
    "group.id": "image-group",
    "auto.offset.reset": "earliest"
})

consumer.subscribe(["image-topic"])

while True:
    msg = consumer.poll(1.0)
    if msg is None:
        continue
    if msg.error():
        print("Error:", msg.error())
        continue

    # Save received image
    with open("received_photo.jpg", "wb") as file:
        file.write(msg.value())

    print("Image received and saved!")
    break

consumer.close()
