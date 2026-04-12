package id.ac.ui.cs.advprog.order.port;

public interface InventoryPort {
    boolean hasSufficientStock(String productId, int quantity);

    void reserveStock(String productId, int quantity);

    void releaseReservedStock(String productId, int quantity);
}

