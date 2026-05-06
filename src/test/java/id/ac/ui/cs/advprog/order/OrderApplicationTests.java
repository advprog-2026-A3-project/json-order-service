package id.ac.ui.cs.advprog.order;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import id.ac.ui.cs.advprog.order.client.WalletClient;

@SpringBootTest
class OrderApplicationTests {

    @MockitoBean
    WalletClient walletClient;

    @Test
    void contextLoads() {
    }

}