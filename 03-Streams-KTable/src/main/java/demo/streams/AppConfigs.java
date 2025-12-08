

package demo.streams;

class AppConfigs {
    final static String applicationID = "StreamingTable";
    final static String bootstrapServers = "localhost:9092";
    final static String topicName = "stock-tick-table";
    final static String stateStoreLocation = "tmp/state-store";
    final static String stateStoreName = "kt01-store";
    final static String regExSymbol = "(?i)ABC|XYZ";
    final static String queryServerHost = "localhost";
    final static int queryServerPort = 7010;

}
