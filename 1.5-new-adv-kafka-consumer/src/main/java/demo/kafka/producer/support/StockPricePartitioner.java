package demo.kafka.producer.support;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;

import java.util.*;

public class StockPricePartitioner implements Partitioner{

    private final Set<String> importantStocks;
    public StockPricePartitioner() {
        importantStocks = new HashSet<>();
    }

    @Override
    public int partition(final String topic,
                         final Object objectKey,
                         final byte[] keyBytes,
                         final Object value,
                         final byte[] valueBytes,
                         final Cluster cluster) {

        final List<PartitionInfo> partitionInfoList =
                cluster.availablePartitionsForTopic(topic);
        final int partitionCount = partitionInfoList.size();
        final int importantPartition = partitionCount -1;
        final int normalPartitionCount = partitionCount -1;

        final String key = ((String) objectKey);

        if (importantStocks.contains(key)) {
            return importantPartition;
        } else {
            return Math.abs(key.hashCode()) % normalPartitionCount;
        }

    }

    @Override
    public void close() {
    }

    @Override
    public void configure(Map<String, ?> configs) {
        final String importantStocksStr = (String) configs.get("importantStocks");
        Arrays.stream(importantStocksStr.split(","))
                .forEach(importantStocks::add);
    }

}
