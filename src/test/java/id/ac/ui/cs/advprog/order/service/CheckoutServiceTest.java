package id.ac.ui.cs.advprog.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import id.ac.ui.cs.advprog.order.client.VoucherClient;
import id.ac.ui.cs.advprog.order.dto.CheckoutRequest;
import id.ac.ui.cs.advprog.order.dto.CheckoutResponse;
import id.ac.ui.cs.advprog.order.dto.voucher.VoucherRedeemResponse;
import id.ac.ui.cs.advprog.order.dto.voucher.VoucherValidateResponse;
import id.ac.ui.cs.advprog.order.exception.SelfPurchaseNotAllowedException;
import id.ac.ui.cs.advprog.order.model.Order;
import id.ac.ui.cs.advprog.order.model.OrderStatus;
import id.ac.ui.cs.advprog.order.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.RestClientResponseException;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {
    @Mock
    private VoucherClient voucherClient;

    @Mock
    private OrderRepository orderRepository;

    private CheckoutService checkoutService;
    private CheckoutRequest request;

    @BeforeEach
    void setUp() {
        checkoutService = new CheckoutService(voucherClient, orderRepository);
        request = new CheckoutRequest();
        request.setProductId("PROD-001");
        request.setProductName("KitKat Matcha");
        request.setTitiperUserId("titiper-1");
        request.setJastiperUserId("jastiper-1");
        request.setQuantity(2);
        request.setSubtotal(new BigDecimal("200000"));
        request.setShippingAddress("Depok");
    }

    @Test
    void checkoutWithoutVoucher_returnsPaidTotalWithoutCallingVoucher() {
        stubOrderSave();

        CheckoutResponse response = checkoutService.checkout(request);

        assertEquals(1L, response.orderId());
        assertEquals(OrderStatus.PAID, response.status());
        assertEquals(null, response.voucherCode());
        assertEquals(0, new BigDecimal("200000").compareTo(response.subtotal()));
        assertEquals(0, BigDecimal.ZERO.compareTo(response.discountAmount()));
        assertEquals(0, new BigDecimal("200000").compareTo(response.totalPaid()));
        verify(voucherClient, never()).validateVoucher("WELCOME20", new BigDecimal("200000"));
        verify(voucherClient, never()).redeemVoucher("WELCOME20", new BigDecimal("200000"));
        verify(orderRepository).save(argThat(order ->
            order.getVoucherCode() == null
                && new BigDecimal("200000").compareTo(order.getSubtotal()) == 0
                && BigDecimal.ZERO.compareTo(order.getDiscountAmount()) == 0
                && new BigDecimal("200000").compareTo(order.getTotalPrice()) == 0
                && OrderStatus.PAID == order.getStatus()
        ));
    }

    @Test
    void checkoutWithVoucher_callsValidateAndReturnsDiscountedTotal() {
        stubOrderSave();
        request.setVoucherCode("WELCOME20");
        when(voucherClient.validateVoucher("WELCOME20", new BigDecimal("200000")))
            .thenReturn(new VoucherValidateResponse(
                "WELCOME20",
                new BigDecimal("200000"),
                new BigDecimal("40000")
            ));
        when(voucherClient.redeemVoucher("WELCOME20", new BigDecimal("200000")))
            .thenReturn(new VoucherRedeemResponse(
                "WELCOME20",
                new BigDecimal("200000"),
                9
            ));

        TransactionSynchronizationManager.initSynchronization();
        try {
            CheckoutResponse response = checkoutService.checkout(request);

            assertEquals(1L, response.orderId());
            assertEquals(OrderStatus.CHECKOUT_PENDING, response.status());
            assertEquals("WELCOME20", response.voucherCode());
            assertEquals(0, new BigDecimal("40000").compareTo(response.discountAmount()));
            assertEquals(0, new BigDecimal("160000").compareTo(response.totalPaid()));

            triggerAfterCommit();

            InOrder inOrder = inOrder(voucherClient, orderRepository);
            inOrder.verify(voucherClient).validateVoucher("WELCOME20", new BigDecimal("200000"));
            inOrder.verify(orderRepository).save(any(Order.class));
            inOrder.verify(voucherClient).redeemVoucher("WELCOME20", new BigDecimal("200000"));
            inOrder.verify(orderRepository).save(any(Order.class));
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void checkoutWithVoucher_trimsVoucherCodeAndSavesVoucherSnapshot() {
        stubOrderSave();
        request.setVoucherCode("  WELCOME20  ");
        when(voucherClient.validateVoucher("WELCOME20", new BigDecimal("200000")))
            .thenReturn(new VoucherValidateResponse(
                "WELCOME20",
                new BigDecimal("200000"),
                new BigDecimal("40000")
            ));
        when(voucherClient.redeemVoucher("WELCOME20", new BigDecimal("200000")))
            .thenReturn(new VoucherRedeemResponse(
                "WELCOME20",
                new BigDecimal("200000"),
                9
            ));

        TransactionSynchronizationManager.initSynchronization();
        try {
            CheckoutResponse response = checkoutService.checkout(request);

            assertEquals("WELCOME20", response.voucherCode());
            assertEquals(OrderStatus.CHECKOUT_PENDING, response.status());
            verify(voucherClient).validateVoucher("WELCOME20", new BigDecimal("200000"));
            verify(orderRepository).save(argThat(order ->
                "WELCOME20".equals(order.getVoucherCode())
                    && new BigDecimal("200000").compareTo(order.getSubtotal()) == 0
                    && new BigDecimal("40000").compareTo(order.getDiscountAmount()) == 0
                    && new BigDecimal("160000").compareTo(order.getTotalPrice()) == 0
                    && OrderStatus.CHECKOUT_PENDING == order.getStatus()
            ));

            triggerAfterCommit();
            verify(voucherClient).redeemVoucher("WELCOME20", new BigDecimal("200000"));
            verify(orderRepository, org.mockito.Mockito.atLeast(2)).save(any(Order.class));
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void checkoutWithBlankVoucher_doesNotCallVoucher() {
        stubOrderSave();
        request.setVoucherCode(" ");

        CheckoutResponse response = checkoutService.checkout(request);

        assertEquals(null, response.voucherCode());
        assertEquals(0, new BigDecimal("200000").compareTo(response.totalPaid()));
        verify(voucherClient, never()).validateVoucher("WELCOME20", new BigDecimal("200000"));
        verify(voucherClient, never()).redeemVoucher("WELCOME20", new BigDecimal("200000"));
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void checkoutRejectsSelfPurchase() {
        request.setTitiperUserId("same-user");
        request.setJastiperUserId("same-user");

        assertThrows(SelfPurchaseNotAllowedException.class, () -> checkoutService.checkout(request));
        verify(voucherClient, never()).validateVoucher("WELCOME20", new BigDecimal("200000"));
        verify(voucherClient, never()).redeemVoucher("WELCOME20", new BigDecimal("200000"));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void checkoutWhenVoucherInvalid_propagatesVoucherError() {
        request.setVoucherCode("INVALID");
        when(voucherClient.validateVoucher("INVALID", new BigDecimal("200000")))
            .thenThrow(RestClientResponseException.class);

        assertThrows(RestClientResponseException.class, () -> checkoutService.checkout(request));
        verify(voucherClient).validateVoucher("INVALID", new BigDecimal("200000"));
        verify(voucherClient, never()).redeemVoucher("INVALID", new BigDecimal("200000"));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void checkoutWhenRedeemFails_savesOrderThenPropagatesVoucherError() {
        stubOrderSave();
        request.setVoucherCode("WELCOME20");
        when(voucherClient.validateVoucher("WELCOME20", new BigDecimal("200000")))
            .thenReturn(new VoucherValidateResponse(
                "WELCOME20",
                new BigDecimal("200000"),
                new BigDecimal("40000")
            ));
        when(voucherClient.redeemVoucher("WELCOME20", new BigDecimal("200000")))
            .thenThrow(RestClientResponseException.class);

        TransactionSynchronizationManager.initSynchronization();
        try {
            CheckoutResponse response = checkoutService.checkout(request);
            assertEquals(1L, response.orderId());
            assertEquals(OrderStatus.CHECKOUT_PENDING, response.status());

            assertThrows(RestClientResponseException.class, this::triggerAfterCommit);

            InOrder inOrder = inOrder(voucherClient, orderRepository);
            inOrder.verify(voucherClient).validateVoucher("WELCOME20", new BigDecimal("200000"));
            inOrder.verify(orderRepository).save(any(Order.class));
            inOrder.verify(voucherClient).redeemVoucher("WELCOME20", new BigDecimal("200000"));
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private void triggerAfterCommit() {
        List<TransactionSynchronization> synchronizations =
            new ArrayList<>(TransactionSynchronizationManager.getSynchronizations());
        for (TransactionSynchronization synchronization : synchronizations) {
            synchronization.afterCommit();
        }
    }

    private void stubOrderSave() {
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });
    }
}

