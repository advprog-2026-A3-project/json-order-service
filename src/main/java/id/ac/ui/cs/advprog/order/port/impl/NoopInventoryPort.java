package id.ac.ui.cs.advprog.order.port.impl;

import id.ac.ui.cs.advprog.order.port.InventoryPort;
import org.springframework.stereotype.Component;

@Component
public class NoopInventoryPort implements InventoryPort {
    @Override
    public boolean hasSufficientStock(String productId, int quantity) {
        return true;
    }

    @Override
    public void reserveStock(String productId, int quantity) {
        // Placeholder adapter. Replace with real inventory integration.
    }

    @Override
    public void releaseReservedStock(String productId, int quantity) {
        // Placeholder adapter. Replace with real inventory integration.
    }
}

