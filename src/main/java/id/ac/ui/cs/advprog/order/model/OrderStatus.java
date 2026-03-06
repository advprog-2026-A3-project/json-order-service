package id.ac.ui.cs.advprog.order.model;

/**
 * Enum representing the different statuses of an order throughout its lifecycle.
 *
 * Status Flow:
 * PENDING → PAID → PURCHASED → SHIPPED → COMPLETED
 * Any status → CANCELLED (with conditions)
 */
public enum OrderStatus {
    /**
     * Order has been created but payment is not yet confirmed
     */
    PENDING,

    /**
     * Payment has been confirmed and funds have been deducted from Titiper's wallet
     */
    PAID,

    /**
     * Jastiper has purchased/obtained the item from the source
     */
    PURCHASED,

    /**
     * Item has been shipped to the Titiper's address
     */
    SHIPPED,

    /**
     * Item has been received by Titiper. Order is now complete and eligible for rating.
     */
    COMPLETED,

    /**
     * Order has been cancelled. Refund may be issued to Titiper.
     */
    CANCELLED
}

