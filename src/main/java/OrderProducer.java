import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class OrderProducer {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        ObjectMapper mapper = new ObjectMapper();
        Random random = new Random();

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            for (int i = 1; i <= 50; i++) {
                String user = "user" + (i % 5 + 1);
                double amount = 50 + random.nextInt(1001);
                Order order = new Order(i, user, amount);
                String json = mapper.writeValueAsString(order);

                ProducerRecord<String, String> record =
                        new ProducerRecord<>("orders", String.valueOf(order.orderId), json);

                RecordMetadata meta = producer.send(record).get();

                System.out.printf(
                        "Sent order %s (partition=%d, offset=%d)%n",
                        json, meta.partition(), meta.offset()
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
