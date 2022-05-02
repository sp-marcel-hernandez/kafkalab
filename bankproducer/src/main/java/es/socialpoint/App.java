package es.socialpoint;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

public class App {
    public static void main(String[] args) {
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.RETRIES_CONFIG, "3");
        config.put(ProducerConfig.LINGER_MS_CONFIG, "1");   // /!\ Only for dev
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");

        Producer<String, String> producer = new KafkaProducer<>(config);

        int i = 0;
        while (true) {
            System.out.println("Batch " + i);
            try {
                producer.send(newRandomTransaction("john"));
                Thread.sleep(100);
                producer.send(newRandomTransaction("stephane"));
                Thread.sleep(100);
                producer.send(newRandomTransaction("alice"));
                Thread.sleep(100);
                i++;
            } catch (InterruptedException e) {
                break;
            }
        }

        producer.close();
    }

    private static ProducerRecord<String, String> newRandomTransaction(String name) {
        ObjectNode transaction = JsonNodeFactory.instance.objectNode();

        transaction.put("name", name);
        transaction.put("amount", ThreadLocalRandom.current().nextInt(0, 100));
        transaction.put("time", Instant.now().toString());

        return new ProducerRecord<>("bank-transactions", name, transaction.toString());
    }
}
