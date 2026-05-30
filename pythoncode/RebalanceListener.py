from confluent_kafka import Consumer, TopicPartition, OFFSET_INVALID


class RebalanceListener:
    def __init__(self, consumer):
        self.consumer = consumer
        self.current_offsets = {}

    def add_offset(self, topic, partition, offset):
        tp = TopicPartition(topic, partition, offset)
        self.current_offsets[tp] = offset

    def on_assign(self, consumer, partitions):
        print("Following Partitions Assigned ....")
        for p in partitions:
            print(f"{p.partition},")
        consumer.assign(partitions)

    def on_revoke(self, consumer, partitions):
        print("Following Partitions Revoked ....")
        for p in partitions:
            print(f"{p.partition},")

        print("Committing stored offsets before revocation...")
        offsets_to_commit = []
        for tp, offset in self.current_offsets.items():
            print(f"Commit partition: {tp.partition}, offset: {offset}")
            offsets_to_commit.append(TopicPartition(tp.topic, tp.partition, offset))

        if offsets_to_commit:
            consumer.commit(offsets=offsets_to_commit, asynchronous=False)

        self.current_offsets.clear()
