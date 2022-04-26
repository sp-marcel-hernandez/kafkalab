package es.socialpoint;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Arrays;
import java.util.Properties;

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

        application.cleanUp();

        application.start();

        Runtime.getRuntime().addShutdownHook(new Thread(application::close));
    }

    private static Topology defineTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, String> input = builder.stream(
                "wordcount-input",
                Consumed.with(Serdes.String(), Serdes.String())
        );

        KTable<String, Long> wordcount = input
                .mapValues(value -> value.toLowerCase())
                .flatMapValues(value -> Arrays.asList(value.split(" ")))
                .selectKey((key, value) -> value)
                .groupByKey()
                .count();

        wordcount.toStream().to("wordcount-output", Produced.with(Serdes.String(), Serdes.Long()));

        return builder.build();
    }
}
