package id.ac.ui.cs.advprog.order.port.impl;

import id.ac.ui.cs.advprog.order.port.WalletPort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class NoopWalletPort implements WalletPort {
    @Override
    public boolean hasSufficientBalance(String userId, BigDecimal amount) {
        return true;
    }

    @Override
    public void deductBalance(String userId, BigDecimal amount) {
        // Placeholder adapter. Replace with real wallet integration.
    }

    @Override
    public void refundBalance(String userId, BigDecimal amount) {
        // Placeholder adapter. Replace with real wallet integration.
    }
}

