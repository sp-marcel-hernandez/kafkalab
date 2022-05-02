package es.socialpoint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;

import java.time.Instant;
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
        config.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);

        Topology topology = defineTopology();
        System.out.println(topology.describe());

        KafkaStreams application = new KafkaStreams(topology, config);

        // Cleans up local RocksDB state. Only use it at dev time /!\
        application.cleanUp();

        application.start();

        Runtime.getRuntime().addShutdownHook(new Thread(application::close));
    }

    private static Topology defineTopology() {
        final Serializer<JsonNode> serializer = new JsonSerializer();
        final Deserializer<JsonNode> deserializer = new JsonDeserializer();
        final Serde<JsonNode> jsonSerde = Serdes.serdeFrom(serializer, deserializer);

        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, JsonNode> transactions = builder.stream("bank-transactions", Consumed.with(Serdes.String(), jsonSerde));

        ObjectNode initialBalance = JsonNodeFactory.instance.objectNode();
        initialBalance.put("count", 0L);
        initialBalance.put("balance", 0L);
        initialBalance.put("time", Instant.ofEpochMilli(0L).toString());

        KTable<String, JsonNode> bankBalance = transactions
                .groupByKey()
                .aggregate(
                        () -> initialBalance,
                        (key, transaction, balance) -> newBalance(transaction, balance),
                        jsonSerde
                )
                .toStream()
                .to("bank-balances");

        return builder.build();
    }

    private static JsonNode newBalance(JsonNode transaction, JsonNode balance) {
        ObjectNode newBalance = JsonNodeFactory.instance.objectNode();
        newBalance.put("count", balance.get("count").asInt() + 1);
        newBalance.put("balance", balance.get("balance").asInt() + transaction.get("amount").asInt());
        newBalance.put("time", Instant.ofEpochMilli(
                Math.max(
                    Instant.parse(balance.get("time").asText()).toEpochMilli(),
                    Instant.parse(transaction.get("time").asText()).toEpochMilli())
                ).toString()
        );

        return newBalance;
    }
}
