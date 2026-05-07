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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class CheckoutService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckoutService.class);

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
        OrderStatus initialStatus = voucherCode == null ? OrderStatus.PAID : OrderStatus.CHECKOUT_PENDING;
        Order savedOrder = orderRepository.save(buildOrder(
            request,
            voucherCode,
            subtotal,
            discountAmount,
            totalPaid,
            initialStatus
        ));

        if (voucherCode != null) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        voucherClient.redeemVoucher(voucherCode, subtotal);
                        savedOrder.setStatus(OrderStatus.PAID);
                        orderRepository.save(savedOrder);
                    } catch (Exception ex) {
                        LOGGER.error("Voucher redemption failed for order {} and voucher {}", savedOrder.getId(), voucherCode, ex);
                    }
                }
            });
        }

        return new CheckoutResponse(
            savedOrder.getId(),
            initialStatus,
            voucherCode,
            subtotal,
            discountAmount,
            totalPaid
        );
    }

    private Order buildOrder(
        CheckoutRequest request,
        String voucherCode,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal totalPaid,
        OrderStatus status
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
        order.setStatus(status);
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
