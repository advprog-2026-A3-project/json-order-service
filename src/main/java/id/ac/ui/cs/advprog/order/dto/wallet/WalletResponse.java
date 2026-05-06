package id.ac.ui.cs.advprog.order.dto.wallet;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WalletResponse(
        UUID id,
        String userId,
        BigDecimal balance,
        Instant createdAt,
        Instant updatedAt
) {}