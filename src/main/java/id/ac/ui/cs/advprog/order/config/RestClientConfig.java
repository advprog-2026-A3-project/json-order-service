package id.ac.ui.cs.advprog.order.config;

import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfig {

    private static final int DEFAULT_CONNECT_TIMEOUT_SECONDS = 5;
    private static final int DEFAULT_READ_TIMEOUT_SECONDS = 10;

    @Bean
    public RestClient.Builder restClientBuilder(
            @Value("${voucher.client.connect-timeout-seconds:" + DEFAULT_CONNECT_TIMEOUT_SECONDS + "}") int connectTimeoutSeconds,
            @Value("${voucher.client.read-timeout-seconds:" + DEFAULT_READ_TIMEOUT_SECONDS + "}") int readTimeoutSeconds) {
        if (connectTimeoutSeconds <= 0 || readTimeoutSeconds <= 0) {
            throw new IllegalArgumentException(
                    "Timeout values must be positive: connectTimeoutSeconds=" + connectTimeoutSeconds
                    + ", readTimeoutSeconds=" + readTimeoutSeconds);
        }
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(connectTimeoutSeconds))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(readTimeoutSeconds));
        return RestClient.builder().requestFactory(factory);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}