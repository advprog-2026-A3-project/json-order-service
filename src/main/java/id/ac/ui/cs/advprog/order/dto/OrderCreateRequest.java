package id.ac.ui.cs.advprog.order.dto;

import java.math.BigDecimal;

public class OrderCreateRequest {
    private String productId;
    private String productName;
    private String titiperUserId;
    private String jastiperUserId;
    private Integer quantity;
    private BigDecimal totalPrice;
    private String shippingAddress;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getTitiperUserId() {
        return titiperUserId;
    }

    public void setTitiperUserId(String titiperUserId) {
        this.titiperUserId = titiperUserId;
    }

    public String getJastiperUserId() {
        return jastiperUserId;
    }

    public void setJastiperUserId(String jastiperUserId) {
        this.jastiperUserId = jastiperUserId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
}

