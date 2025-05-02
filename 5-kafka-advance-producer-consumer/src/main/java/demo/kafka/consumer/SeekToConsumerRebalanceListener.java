package demo.kafka.consumer;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.common.TopicPartition;

import demo.kafka.producer.model.StockPrice;

import java.util.Collection;

public class SeekToConsumerRebalanceListener implements ConsumerRebalanceListener {
    private final Consumer<String, StockPrice> consumer;
    private final SeekTo seekTo; private boolean done;
    private final long location;
    private final long startTime = System.currentTimeMillis();
    public SeekToConsumerRebalanceListener(final Consumer<String, StockPrice> consumer, final SeekTo seekTo, final long location) {
        this.seekTo = seekTo;
        this.location = location;
        this.consumer = consumer;
    }

    @Override
    public void onPartitionsAssigned(final Collection<TopicPartition> partitions) {
            if (done) return;
            else if (System.currentTimeMillis() - startTime > 30_000) {
                done = true;
                return;
            }
            switch (seekTo) {
                case END:                   //Seek to end
                    consumer.seekToEnd(partitions);
                    break;
                case START:                 //Seek to start
                    consumer.seekToBeginning(partitions);
                    break;
                case LOCATION:              //Seek to a given location
                    partitions.forEach(topicPartition ->
                            consumer.seek(topicPartition, location));
                    break;
            }
    }


    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {

    }

}
