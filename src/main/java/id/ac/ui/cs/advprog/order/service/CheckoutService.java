package id.ac.ui.cs.advprog.order.service;

import id.ac.ui.cs.advprog.order.client.InventoryClient;
import id.ac.ui.cs.advprog.order.client.VoucherClient;
import id.ac.ui.cs.advprog.order.dto.CheckoutRequest;
import id.ac.ui.cs.advprog.order.dto.CheckoutResponse;
import id.ac.ui.cs.advprog.order.dto.inventory.InventoryProductResponse;
import id.ac.ui.cs.advprog.order.dto.voucher.VoucherValidateResponse;
import id.ac.ui.cs.advprog.order.exception.SelfPurchaseNotAllowedException;
import id.ac.ui.cs.advprog.order.model.Order;
import id.ac.ui.cs.advprog.order.model.OrderStatus;
import id.ac.ui.cs.advprog.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
public class CheckoutService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckoutService.class);

    private final VoucherClient voucherClient;
    private final OrderRepository orderRepository;

    // BARU(Order - Inventory): 1. Menambahkan InventoryClient untuk komunikasi ke Inventory Service
    private final InventoryClient inventoryClient;

    public CheckoutService(
            VoucherClient voucherClient,
            OrderRepository orderRepository,
            InventoryClient inventoryClient
    ) {
        this.voucherClient = voucherClient;
        this.orderRepository = orderRepository;

        // BARU(Order - Inventory): 2. Inject InventoryClient melalui constructor
        this.inventoryClient = inventoryClient;
    }

    @Transactional
    public CheckoutResponse checkout(CheckoutRequest request) {
        // BARU(Order - Inventory): 3. Fetch Product dari Inventory Service berdasarkan productId
        InventoryProductResponse product =
                inventoryClient.getProductById(request.getProductId());

        // BARU(Order - Inventory): 4. Validasi stok product dari Inventory sebelum checkout
        validateStock(product, request.getQuantity());

        // BARU(Order - Inventory): 5. Hitung subtotal dari harga Inventory dikali quantity
        BigDecimal subtotal = product.getHarga()
                .multiply(BigDecimal.valueOf(request.getQuantity()));

        // BARU(Order - Inventory): 6. Override data product dari request FE dengan data resmi dari Inventory
        request.setProductName(product.getNama());
        request.setJastiperUserId(product.getJastiperId());
        request.setSubtotal(subtotal);

        validateNotSelfPurchase(request);

        BigDecimal discountAmount = BigDecimal.ZERO;
        String voucherCode = normalizeVoucherCode(request.getVoucherCode());

        if (voucherCode != null) {
            VoucherValidateResponse voucherResponse =
                    voucherClient.validateVoucher(voucherCode, subtotal);

            discountAmount = voucherResponse.discountAmount();
        }

        BigDecimal totalPaid = subtotal.subtract(discountAmount);

        OrderStatus initialStatus = voucherCode == null
                ? OrderStatus.PAID
                : OrderStatus.CHECKOUT_PENDING;

        Order savedOrder = orderRepository.save(buildOrder(
                request,
                voucherCode,
                subtotal,
                discountAmount,
                totalPaid,
                initialStatus
        ));

        if (voucherCode == null) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            // BARU(Order - Inventory): 7. Mengurangi stok Inventory setelah checkout tanpa voucher berhasil
                            inventoryClient.reduceProductStock(
                                    request.getProductId(),
                                    request.getQuantity()
                            );
                        }
                    }
            );
        }

        if (voucherCode != null) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            try {
                                voucherClient.redeemVoucher(voucherCode, subtotal);

                                // BARU(Order - Inventory): 8. Mengurangi stok Inventory setelah voucher berhasil diredeem
                                inventoryClient.reduceProductStock(
                                        request.getProductId(),
                                        request.getQuantity()
                                );

                                savedOrder.setStatus(OrderStatus.PAID);
                                orderRepository.save(savedOrder);
                            } catch (RuntimeException ex) {
                                LOGGER.error(
                                        "Voucher redemption failed for order {} and voucher {}",
                                        savedOrder.getId(),
                                        voucherCode,
                                        ex
                                );
                                throw ex;
                            }
                        }
                    }
            );
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

    // BARU(Order - Inventory): 9. Method validasi quantity dan stok berdasarkan data Inventory
    private void validateStock(InventoryProductResponse product, Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity must be at least 1"
            );
        }

        if (product.getStok() == null || product.getStok() < quantity) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Insufficient product stock"
            );
        }
    }

    private void validateNotSelfPurchase(CheckoutRequest request) {
        String titiperId = request.getTitiperUserId();
        String jastiperId = request.getJastiperUserId();

        if (titiperId != null
                && !titiperId.isBlank()
                && titiperId.equals(jastiperId)) {
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