package id.ac.ui.cs.advprog.order;

import id.ac.ui.cs.advprog.order.client.WalletClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class OrderApplicationTests {

    @MockitoBean
    WalletClient walletClient;

    @Test
    void contextLoads() {
    }
}