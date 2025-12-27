import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class RestOrderServer {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final KafkaProducer<String, String> producer;

    static {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer<>(props);
    }

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/orders", new OrderHandler());
        server.setExecutor(null); // default executor
        server.start();
        System.out.println("REST Order server started on http://localhost:8080");
        System.out.println("Send POST /orders with JSON body like:");
        System.out.println("{\"orderId\": 101, \"user\": \"alice\", \"amount\": 500}");
    }

    static class OrderHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String resp = "Method not allowed";
                exchange.sendResponseHeaders(405, resp.getBytes(StandardCharsets.UTF_8).length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(resp.getBytes(StandardCharsets.UTF_8));
                }
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            System.out.println("Incoming HTTP body: " + body);

            try {
                // парсим JSON в Order
                Order order = mapper.readValue(body, Order.class);

                // сериализуем обратно (на случай, если в теле были лишние поля)
                String value = mapper.writeValueAsString(order);
                String key = String.valueOf(order.orderId);

                ProducerRecord<String, String> record =
                        new ProducerRecord<>("orders", key, value);

                producer.send(record); // асинхронно, нам не обязательно ждать get()

                String resp = "Order accepted: " + value;
                byte[] bytes = resp.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }

                System.out.println("Sent to Kafka from REST: " + value);

            } catch (Exception e) {
                e.printStackTrace();
                String resp = "Bad request: " + e.getMessage();
                byte[] bytes = resp.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            }
        }
    }
}
