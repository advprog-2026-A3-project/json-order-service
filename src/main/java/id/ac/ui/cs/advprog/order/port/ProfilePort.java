package id.ac.ui.cs.advprog.order.port;

public interface ProfilePort {
    boolean isSelfPurchase(String buyerUserId, String jastiperUserId);
}
