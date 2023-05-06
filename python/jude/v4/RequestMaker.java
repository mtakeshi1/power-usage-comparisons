import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
This class is based on RequestMaker in benchmark.
Do not modify. The goal is only to get an idea on whether or not
the server can be reached with HTTP 2 protocol, as V3 failed miserably
there. */

public class RequestMaker {

    public static final int NUM_SELECT = 5;

    private final URI productsURI;
    private final URI newOrderURI;

    private static final int[] PRODUCT_IDS = IntStream.range(1, 32).toArray();
    private final String name;

    private final String host;
    private final int port;

    public RequestMaker(String name, String host, int port) {
        this.name = name;
        this.host = host;
        this.port = port;
        System.out.printf("http://%s:%d/products", host, port);
        this.productsURI = URI.create(String.format("http://%s:%d/products", host, port));
        this.newOrderURI = URI.create(String.format("http://%s:%d/orders/new", host, port));
    }

    public void loop(int numberOfRequests, Duration pauseBetweenRequests) throws InterruptedException, IOException {
        long pauseNanos = pauseBetweenRequests.toNanos();
        for (int i = 0; i < numberOfRequests; i++) {
            makeRequest();
            TimeUnit.NANOSECONDS.sleep(pauseNanos);
        }
    }

    private static void shuffle(int[] array) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < array.length; i++) {
            int randomIndexToSwap = random.nextInt(array.length);
            int temp = array[randomIndexToSwap];
            array[randomIndexToSwap] = array[i];
            array[i] = temp;
        }
    }

    private int[] select() {
        int[] s = PRODUCT_IDS.clone();
        shuffle(s);
        return Arrays.copyOf(s, NUM_SELECT);
    }

    private String formatJSON(int[] select, int amount) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < select.length; i++) {
            sb.append("{\"productId\":%d, \"amount\":%d}".formatted(select[i], amount));
            if (i < select.length - 1) {
                sb.append(',');
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private URI productURI(int productId) {
        return URI.create(String.format("http://%s:%d/products/%d", host, port, productId));
    }

    private URI orderURI(int orderId) {
        return URI.create(String.format("http://%s:%d/orders/%d", host, port, orderId));
    }

    public void makeRequest() throws IOException, InterruptedException {
        HttpRequest build = HttpRequest.newBuilder()
                .GET()
                .uri(productsURI)
                .build();
        int[] select = select();
        String json = formatJSON(select, 5);
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> send = client.send(build, HttpResponse.BodyHandlers.ofString());
        if (send.statusCode() != 200) {
            throw new RuntimeException("%s - Http response code: %d - %s".formatted(name, send.statusCode(), send.body()));
        } else {
            System.out.println("All is fine");
        }
        var response = client.send(HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .uri(newOrderURI)
                .header("Content-Type", "application/json")
                .build(), HttpResponse.BodyHandlers.ofString());
        if (!validateResponse(response.statusCode())) {
            throw new RuntimeException("%s Http response code: %d for POST - %s".formatted(name, response.statusCode(), send.body()));
        }
        int orderId = Integer.parseInt(response.body());
        if (!validateResponse(client.send(HttpRequest.newBuilder().uri(orderURI(orderId)).build(), HttpResponse.BodyHandlers.discarding()).statusCode())) {
            throw new RuntimeException("%s Http response code: %d  for order id: %d - %s".formatted(name, send.statusCode(), orderId, send.body()));
        }
        System.out.println("All good");
    }

    public static boolean validateResponse(int response) {
        return response >= 200 && response < 300;
    }

    public static void main(String[] args) throws Exception {
        RequestMaker maker = new RequestMaker("sample", "localhost", 8000);
        maker.loop(1000, Duration.ofMillis(10));
    }
}