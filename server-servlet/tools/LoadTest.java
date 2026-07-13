import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoadTest {
    private record Result(int statusCode, long elapsedMillis, String error) {
    }

    public static void main(String[] args) {
        String url = args.length > 0
                ? args[0]
                : "http://localhost:8080/SEC_Servlet/DishServlet";
        int totalRequests = args.length > 1 ? Integer.parseInt(args[1]) : 100;
        int concurrency = args.length > 2 ? Integer.parseInt(args[2]) : 20;

        ExecutorService executor = Executors.newFixedThreadPool(concurrency);
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .executor(executor)
                .build();

        long suiteStart = System.nanoTime();
        List<CompletableFuture<Result>> futures = new ArrayList<>();
        for (int i = 0; i < totalRequests; i++) {
            futures.add(CompletableFuture.supplyAsync(() -> send(client, url), executor));
        }

        List<Result> results = futures.stream().map(CompletableFuture::join).toList();
        executor.shutdown();

        double totalSeconds = (System.nanoTime() - suiteStart) / 1_000_000_000.0;
        List<Long> latencies = new ArrayList<>();
        long success = 0;
        for (Result result : results) {
            latencies.add(result.elapsedMillis());
            if (result.statusCode() >= 200 && result.statusCode() < 300 && result.error() == null) {
                success++;
            }
        }
        Collections.sort(latencies);

        double average = latencies.stream().mapToLong(Long::longValue).average().orElse(0);
        long p95 = latencies.get(Math.max(0, (int) Math.ceil(latencies.size() * 0.95) - 1));
        double throughput = totalRequests / totalSeconds;

        System.out.printf("URL: %s%n", url);
        System.out.printf("Requests: %d, concurrency: %d%n", totalRequests, concurrency);
        System.out.printf("Success: %d, failed: %d, success rate: %.2f%%%n",
                success, totalRequests - success, success * 100.0 / totalRequests);
        System.out.printf("Total: %.3f s, throughput: %.2f req/s%n", totalSeconds, throughput);
        System.out.printf("Average latency: %.2f ms, P95 latency: %d ms%n", average, p95);
    }

    private static Result send(HttpClient client, String url) {
        long start = System.nanoTime();
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return new Result(response.statusCode(), elapsedMillis(start), null);
        } catch (Exception e) {
            return new Result(0, elapsedMillis(start), e.getClass().getSimpleName());
        }
    }

    private static long elapsedMillis(long start) {
        return (System.nanoTime() - start) / 1_000_000;
    }
}
