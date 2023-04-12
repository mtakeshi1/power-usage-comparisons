package benchmark;

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

public class RequestMaker {

    public static final int NUM_SELECT = 5;

    private final URI productsURI;
    private final URI newOrderURI;

    private static final int[] PRODUCT_IDS = IntStream.range(1, 32).toArray();
    private final URI scaphandreURI;
    private final String name;

    private final String host;
    private final int port;

    public RequestMaker(String name, String host, int port, int scaphandrePort) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.productsURI = URI.create(String.format("http://%s:%d/products", host, port));
        this.newOrderURI = URI.create(String.format("http://%s:%d/orders/new", host, port));
        if (scaphandrePort > 0) {
            this.scaphandreURI = URI.create(String.format("http://%s:%d/metrics", host, scaphandrePort));
        } else {
            this.scaphandreURI = null;
        }
    }

    public Results loop(int numberOfRequests, Duration pauseBetweenRequests) throws InterruptedException, IOException {
        List<Duration> list = new ArrayList<>(numberOfRequests);
        long pauseNanos = pauseBetweenRequests.toNanos();
        long before = energyMeasureMicroJoules();
        long t0 = System.nanoTime();
        for (int i = 0; i < numberOfRequests; i++) {
            list.add(makeRequest());
            TimeUnit.NANOSECONDS.sleep(pauseNanos);
        }
        long t1 = System.nanoTime() - t0;
        long powerDiff = energyMeasureMicroJoules() - before;
        return new Results(name, list, powerDiff, Duration.ofNanos(t1), new CPUUsage(0, 0, 1, 0));
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

    public long energyMeasureMicroJoules() throws IOException, InterruptedException {
        if (scaphandreURI != null) {
            HttpClient client = HttpClient.newHttpClient();
            var response = client.send(HttpRequest.newBuilder().uri(scaphandreURI).build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                for (String line : response.body().split("\n")) {
                    if (line.startsWith("scaph_host_energy_microjoules")) {
                        return Long.parseLong(line.split(" ")[1]);
                    }
                }
            }
        }
        return 0;
    }

    public Duration makeRequest() throws IOException, InterruptedException {
        HttpRequest build = HttpRequest.newBuilder()
                .GET()
                .uri(productsURI)
                .build();
        int[] select = select();
        String json = formatJSON(select, 5);
        HttpClient client = HttpClient.newHttpClient();
        long t0 = System.nanoTime();
        HttpResponse<String> send = client.send(build, HttpResponse.BodyHandlers.ofString());
        if (send.statusCode() != 200) {
            throw new RuntimeException("%s - Http response code: %d - %s".formatted(name, send.statusCode(), send.body()));
        }
        for (int productId : select) {
            int code = client.send(HttpRequest.newBuilder().uri(productURI(productId)).build(), HttpResponse.BodyHandlers.discarding()).statusCode();
            if (!validateResponse(code)) {
                throw new RuntimeException("%s Http response code: %d for product id: %d - %s".formatted(name, send.statusCode(), productId, send.body()));
            }
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
        return Duration.ofNanos(System.nanoTime() - t0);
    }

    public static boolean validateResponse(int response) {
        return response >= 200 && response < 300;
    }

    public static void main(String[] args) throws Exception {
        RequestMaker maker = new RequestMaker("sample", "localhost", 8080, 8081);
        System.out.println(maker.loop(1000, Duration.ofMillis(10)));
    }
}
