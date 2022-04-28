package es.socialpoint;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Arrays;
import java.util.Properties;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "wordcount");
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

        KStream<String, String> input = builder.stream(
                "favcolors-input",
                Consumed.with(Serdes.String(), Serdes.String())
        );

        KStream<String, String> part1 = input
                .filter((key, value) -> value.contains(","))
                .selectKey((key, value) -> value.split(",")[0].toLowerCase())
                .mapValues(value -> value.split(",")[1].toLowerCase())
                .filter((user, color) -> Arrays.asList("red", "green", "blue").contains(color));

        part1.to("favcolors-intermediate-table", Produced.with(Serdes.String(), Serdes.String()));

        KTable<String, String> part2 = builder.table("favcolors-intermediate-table");
        KTable<String, Long> part3 = part2
                .groupBy((user, color) -> new KeyValue<>(color, color))
                .count();

        part3.toStream().to("favcolors-output", Produced.with(Serdes.String(), Serdes.Long()));

        return builder.build();
    }
}
