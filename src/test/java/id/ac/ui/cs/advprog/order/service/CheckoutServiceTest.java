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
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock
    private VoucherClient voucherClient;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InventoryClient inventoryClient;

    @InjectMocks
    private CheckoutService checkoutService;

    private CheckoutRequest request;
    private InventoryProductResponse product;

    @BeforeEach
    void setUp() {
        request = new CheckoutRequest();
        request.setProductId("PROD-1");
        request.setProductName("Old Name");
        request.setTitiperUserId("titiper-1");
        request.setJastiperUserId("old-jastiper");
        request.setQuantity(2);
        request.setShippingAddress("Depok");
        request.setSubtotal(BigDecimal.ZERO);

        product = new InventoryProductResponse();
        product.setId("PROD-1");
        product.setNama("Inventory Name");
        product.setHarga(new BigDecimal("10000"));
        product.setStok(5);
        product.setJastiperId("jastiper-2");

        when(inventoryClient.getProductById("PROD-1")).thenReturn(product);
        lenient().when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(1L);
            }
            return saved;
        });
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void checkoutWithoutVoucher_marksPaidAndReducesStockAfterCommit() {
        request.setVoucherCode(null);
        initSynchronization();

        CheckoutResponse response = checkoutService.checkout(request);

        assertNotNull(response);
        assertEquals(OrderStatus.PAID, response.status());
        assertNull(response.voucherCode());
        assertEquals(new BigDecimal("20000"), response.subtotal());
        assertEquals(new BigDecimal("20000"), response.totalPaid());
        verify(voucherClient, never()).validateVoucher(any(), any());
        verify(orderRepository).save(any(Order.class));

        triggerAfterCommit();

        verify(inventoryClient).reduceProductStock("PROD-1", 2);
        verify(voucherClient, never()).redeemVoucher(any(), any());
    }

    @Test
    void checkoutWithVoucher_redeemsAfterCommitAndUpdatesStatus() {
        request.setVoucherCode("  VOUCHER10 ");
        when(voucherClient.validateVoucher(eq("VOUCHER10"), eq(new BigDecimal("20000"))))
                .thenReturn(new VoucherValidateResponse("VOUCHER10", new BigDecimal("20000"), new BigDecimal("2000")));
        initSynchronization();

        CheckoutResponse response = checkoutService.checkout(request);

        assertEquals(OrderStatus.CHECKOUT_PENDING, response.status());
        assertEquals("VOUCHER10", response.voucherCode());
        assertEquals(new BigDecimal("20000"), response.subtotal());
        assertEquals(new BigDecimal("18000"), response.totalPaid());

        ArgumentCaptor<Order> initialCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(initialCaptor.capture());
        assertEquals(OrderStatus.CHECKOUT_PENDING, initialCaptor.getValue().getStatus());

        triggerAfterCommit();

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, times(2)).save(orderCaptor.capture());
        List<Order> savedOrders = orderCaptor.getAllValues();
        assertEquals(OrderStatus.PAID, savedOrders.get(1).getStatus());
        verify(voucherClient).redeemVoucher("VOUCHER10", new BigDecimal("20000"));
        verify(inventoryClient).reduceProductStock("PROD-1", 2);
    }

    @Test
    void checkoutWithVoucher_whenRedeemFails_throwsOnAfterCommit() {
        request.setVoucherCode("VOUCHER10");
        when(voucherClient.validateVoucher(eq("VOUCHER10"), eq(new BigDecimal("20000"))))
                .thenReturn(new VoucherValidateResponse("VOUCHER10", new BigDecimal("20000"), new BigDecimal("1000")));
        when(voucherClient.redeemVoucher(eq("VOUCHER10"), eq(new BigDecimal("20000"))))
                .thenThrow(new RuntimeException("redeem failed"));
        initSynchronization();

        checkoutService.checkout(request);

        assertThrows(RuntimeException.class, this::triggerAfterCommit);
        verify(inventoryClient, never()).reduceProductStock(any(), any());
    }

    @Test
    void checkoutRejectsSelfPurchase() {
        product.setJastiperId("titiper-1");

        assertThrows(SelfPurchaseNotAllowedException.class, () -> checkoutService.checkout(request));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void checkoutRejectsInsufficientStock() {
        product.setStok(1);

        assertThrows(ResponseStatusException.class, () -> checkoutService.checkout(request));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void checkoutRejectsInvalidQuantity() {
        request.setQuantity(0);

        assertThrows(ResponseStatusException.class, () -> checkoutService.checkout(request));
        verify(orderRepository, never()).save(any(Order.class));
    }

    private void initSynchronization() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.initSynchronization();
        }
    }

    private void triggerAfterCommit() {
        for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
            synchronization.afterCommit();
        }
    }
}