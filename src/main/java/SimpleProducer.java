import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Properties;

public class SimpleProducer {
    public static void main(String[] args) throws Exception {

        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {

            for (int i = 1; i <= 5; i++) {
                String value = "message-" + i;

                ProducerRecord<String, String> record =
                        new ProducerRecord<>("test-topic", String.valueOf(i), value);

                RecordMetadata meta = producer.send(record).get();

                System.out.printf(
                        "Sent key=%s value=%s partition=%d offset=%d%n",
                        record.key(), record.value(), meta.partition(), meta.offset()
                );
            }
        }
    }
}
