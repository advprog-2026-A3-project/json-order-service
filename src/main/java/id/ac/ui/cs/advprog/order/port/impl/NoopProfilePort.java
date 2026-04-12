package id.ac.ui.cs.advprog.order.port.impl;

import id.ac.ui.cs.advprog.order.port.ProfilePort;
import org.springframework.stereotype.Component;

@Component
public class NoopProfilePort implements ProfilePort {
    @Override
    public boolean isSelfPurchase(String buyerUserId, String jastiperUserId) {
        if (buyerUserId == null || jastiperUserId == null) {
            return false;
        }
        return buyerUserId.equalsIgnoreCase(jastiperUserId);
    }
}

