package id.ac.ui.cs.advprog.order.dto.wallet;

import java.math.BigDecimal;

public record WalletWithdrawRequest(
        String userId,
        BigDecimal amount
) {}