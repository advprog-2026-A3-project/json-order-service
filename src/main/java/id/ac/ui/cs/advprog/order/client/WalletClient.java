package id.ac.ui.cs.advprog.order.client;

import id.ac.ui.cs.advprog.order.dto.wallet.WalletResponse;
import id.ac.ui.cs.advprog.order.dto.wallet.WalletWithdrawRequest;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class WalletClient {
    private final RestClient restClient;

    public WalletClient(
        RestClient.Builder restClientBuilder,
        @Value("${wallet.service.base-url:https://wallet-jastip-1c28ffa49260.herokuapp.com}") String walletServiceBaseUrl
    ) {
        this.restClient = restClientBuilder
            .baseUrl(walletServiceBaseUrl)
            .build();
    }

    public WalletResponse getWallet(String userId) {
        return restClient.get()
            .uri("/wallet/{userId}", userId)
            .retrieve()
            .body(WalletResponse.class);
    }

    public WalletResponse withdraw(String userId, BigDecimal amount) {
        return restClient.post()
            .uri("/wallet/withdraw")
            .body(new WalletWithdrawRequest(userId, amount))
            .retrieve()
            .body(WalletResponse.class);
    }
}