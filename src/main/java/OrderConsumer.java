import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class OrderConsumer {
    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "order-processor");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");

        ObjectMapper mapper = new ObjectMapper();
        double totalAmount = 0.0;

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList("orders"));

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, String> rec : records) {
                    try {
                        Order order = mapper.readValue(rec.value(), Order.class);

                        if (order.amount <= 0) {
                            System.out.printf("Order %d REJECTED (amount=%.2f)%n",
                                    order.orderId, order.amount);
                        } else if (order.amount > 500) {
                            System.out.printf("Order %d from %s: BIG order, amount=%.2f%n",
                                    order.orderId, order.user, order.amount);
                        } else {
                            System.out.printf("Order %d from %s: normal order, amount=%.2f%n",
                                    order.orderId, order.user, order.amount);
                        }

                        totalAmount += order.amount;
                        System.out.printf("Total processed amount so far: %.2f%n", totalAmount);

                    } catch (Exception e) {
                        System.out.println("Failed to parse order JSON: " + rec.value());
                    }
                }
            }
        }
    }
}
