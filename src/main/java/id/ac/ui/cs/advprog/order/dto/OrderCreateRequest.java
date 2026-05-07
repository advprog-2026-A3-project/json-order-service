package id.ac.ui.cs.advprog.order.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class OrderCreateRequest {
    @NotBlank
    private String productId;

    @NotBlank
    private String productName;

    @NotBlank
    private String titiperUserId;

    @NotBlank
    private String jastiperUserId;

    @NotNull
    @Min(1)
    private Integer quantity;

    @NotNull
    @Min(0)
    @Digits(integer = 17, fraction = 0)
    private BigDecimal totalPrice;

    @NotBlank
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
