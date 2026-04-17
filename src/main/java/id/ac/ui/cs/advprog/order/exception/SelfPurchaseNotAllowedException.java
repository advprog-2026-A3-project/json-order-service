package id.ac.ui.cs.advprog.order.exception;

public class SelfPurchaseNotAllowedException extends RuntimeException {
    public SelfPurchaseNotAllowedException() {
        super("Jastiper tidak boleh membeli barang miliknya sendiri");
    }
}

