package es.socialpoint;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import java.util.Properties;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "bankstream");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        Topology topology = defineTopology();
        System.out.println(topology.describe());

        KafkaStreams application = new KafkaStreams(topology, config);

        // Cleans up local RocksDB state. Only use it at dev time /!\
        application.cleanUp();

        application.start();

        Runtime.getRuntime().addShutdownHook(new Thread(application::close));
    }

    private static Topology defineTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        // TODO

        return builder.build();
    }
}
