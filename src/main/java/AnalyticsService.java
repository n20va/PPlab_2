import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class AnalyticsService {
    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "analytics-service");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");

        ObjectMapper mapper = new ObjectMapper();

        int totalOrders = 0;
        int bigOrders = 0;
        double totalAmount = 0.0;
        Map<String, Integer> ordersPerUser = new HashMap<>();

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList("orders"));

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

                if (records.isEmpty()) {
                    continue;
                }

                for (ConsumerRecord<String, String> rec : records) {
                    Order order = mapper.readValue(rec.value(), Order.class);

                    totalOrders++;
                    totalAmount += order.amount;

                    if (order.amount > 500) {
                        bigOrders++;
                    }

                    ordersPerUser.merge(order.user, 1, Integer::sum);
                }

                System.out.println("=== ANALYTICS ===");
                System.out.printf("Total orders: %d%n", totalOrders);
                System.out.printf("Total amount: %.2f%n", totalAmount);
                System.out.printf("Big orders (>500): %d%n", bigOrders);
                System.out.println("Orders per user:");
                for (Map.Entry<String, Integer> entry : ordersPerUser.entrySet()) {
                    System.out.printf("  %s -> %d%n", entry.getKey(), entry.getValue());
                }
                System.out.println("=================\n");
            }
        }
    }
}
