package demo.streams;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;

public class StreamingTableApp {
    private static final Logger logger = LogManager.getLogger();

    public static void main(final String[] args) {

        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, AppConfigs.applicationID);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfigs.bootstrapServers);
        props.put(StreamsConfig.STATE_DIR_CONFIG, AppConfigs.stateStoreLocation);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StreamsBuilder streamsBuilder = new StreamsBuilder();
        KTable<String, String> KT0 = streamsBuilder.table(AppConfigs.topicName);
        KT0.toStream().print(Printed.<String, String>toSysOut().withLabel("KT0"));

        KTable<String, String> KT1 = KT0.filter((k, v) -> k.matches(AppConfigs.regExSymbol) && !v.isEmpty(),
            Materialized.as(AppConfigs.stateStoreName));
        KT1.toStream().print(Printed.<String, String>toSysOut().withLabel("KT1"));

        KafkaStreams streams = new KafkaStreams(streamsBuilder.build(), props);

        //Query Server
        QueryServer queryServer = new QueryServer(streams, AppConfigs.queryServerHost, AppConfigs.queryServerPort);
        streams.setStateListener((newState, oldState) -> {
            logger.info("State Changing to " + newState + " from " + oldState);
            queryServer.setActive(newState == KafkaStreams.State.RUNNING && oldState == KafkaStreams.State.REBALANCING);
        });

        streams.start();
        queryServer.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down servers");
            queryServer.stop();
            streams.close();
        }));

    }
}
