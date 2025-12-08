from confluent_kafka import Producer

producer = Producer({"bootstrap.servers": "localhost:9092"})
topic = "image-topic"

# Read image file as bytes
with open("nature.jpg", "rb") as file:
    image_bytes = file.read()

producer.produce(topic, value=image_bytes)
producer.flush()

print("Image sent successfully!")
