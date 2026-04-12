package id.ac.ui.cs.advprog.order.port;

import java.math.BigDecimal;

public interface WalletPort {
    boolean hasSufficientBalance(String userId, BigDecimal amount);

    void deductBalance(String userId, BigDecimal amount);

    void refundBalance(String userId, BigDecimal amount);
}

