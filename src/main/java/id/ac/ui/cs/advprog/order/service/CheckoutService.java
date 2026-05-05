package id.ac.ui.cs.advprog.order.service;

import id.ac.ui.cs.advprog.order.client.VoucherClient;
import id.ac.ui.cs.advprog.order.dto.CheckoutRequest;
import id.ac.ui.cs.advprog.order.dto.CheckoutResponse;
import id.ac.ui.cs.advprog.order.dto.voucher.VoucherValidateResponse;
import id.ac.ui.cs.advprog.order.exception.SelfPurchaseNotAllowedException;
import id.ac.ui.cs.advprog.order.model.Order;
import id.ac.ui.cs.advprog.order.model.OrderStatus;
import id.ac.ui.cs.advprog.order.repository.OrderRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CheckoutService {
    private final VoucherClient voucherClient;
    private final OrderRepository orderRepository;

    public CheckoutService(VoucherClient voucherClient, OrderRepository orderRepository) {
        this.voucherClient = voucherClient;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public CheckoutResponse checkout(CheckoutRequest request) {
        validateNotSelfPurchase(request);

        BigDecimal subtotal = request.getSubtotal();
        BigDecimal discountAmount = BigDecimal.ZERO;
        String voucherCode = normalizeVoucherCode(request.getVoucherCode());

        if (voucherCode != null) {
            VoucherValidateResponse voucherResponse = voucherClient.validateVoucher(voucherCode, subtotal);
            discountAmount = voucherResponse.discountAmount();
        }

        BigDecimal totalPaid = subtotal.subtract(discountAmount);
        Order savedOrder = orderRepository.save(buildPaidOrder(
            request,
            voucherCode,
            subtotal,
            discountAmount,
            totalPaid
        ));

        if (voucherCode != null) {
            voucherClient.redeemVoucher(voucherCode, subtotal);
        }

        return new CheckoutResponse(
            savedOrder.getId(),
            OrderStatus.PAID,
            voucherCode,
            subtotal,
            discountAmount,
            totalPaid
        );
    }

    private Order buildPaidOrder(
        CheckoutRequest request,
        String voucherCode,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal totalPaid
    ) {
        Order order = new Order();
        order.setProductId(request.getProductId());
        order.setProductName(request.getProductName());
        order.setTitiperUserId(request.getTitiperUserId());
        order.setJastiperUserId(request.getJastiperUserId());
        order.setQuantity(request.getQuantity());
        order.setShippingAddress(request.getShippingAddress());
        order.setVoucherCode(voucherCode);
        order.setSubtotal(subtotal);
        order.setDiscountAmount(discountAmount);
        order.setTotalPrice(totalPaid);
        order.setStatus(OrderStatus.PAID);
        return order;
    }

    private void validateNotSelfPurchase(CheckoutRequest request) {
        String titiperId = request.getTitiperUserId();
        String jastiperId = request.getJastiperUserId();
        if (titiperId != null && !titiperId.isBlank() && titiperId.equals(jastiperId)) {
            throw new SelfPurchaseNotAllowedException();
        }
    }

    private String normalizeVoucherCode(String voucherCode) {
        if (voucherCode == null || voucherCode.isBlank()) {
            return null;
        }
        return voucherCode.trim();
    }
}
