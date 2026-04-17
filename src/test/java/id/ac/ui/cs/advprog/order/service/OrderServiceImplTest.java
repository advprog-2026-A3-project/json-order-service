package id.ac.ui.cs.advprog.order.service;

import id.ac.ui.cs.advprog.order.dto.OrderCreateRequest;
import id.ac.ui.cs.advprog.order.exception.InvalidOrderStatusTransitionException;
import id.ac.ui.cs.advprog.order.exception.OrderNotFoundException;
import id.ac.ui.cs.advprog.order.exception.SelfPurchaseNotAllowedException;
import id.ac.ui.cs.advprog.order.model.Order;
import id.ac.ui.cs.advprog.order.model.OrderStatus;
import id.ac.ui.cs.advprog.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private OrderCreateRequest request;
    private Order order;

    @BeforeEach
    void setUp() {
        request = new OrderCreateRequest();
        request.setProductId("PROD-001");
        request.setProductName("MacBook Sleeve");
        request.setTitiperUserId("titiper-1");
        request.setJastiperUserId("jastiper-1");
        request.setQuantity(2);
        request.setTotalPrice(new BigDecimal("120000"));
        request.setShippingAddress("Depok");

        order = new Order();
        order.setId(1L);
        order.setProductId("PROD-001");
        order.setStatus(OrderStatus.PENDING);
    }

    @Test
    void createOrder_setsPendingAndSaves() {
        when(orderMapper.toEntity(request)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);

        Order result = orderService.createOrder(request);

        assertNotNull(result);
        assertEquals(OrderStatus.PENDING, result.getStatus());
        verify(orderMapper).toEntity(request);
        verify(orderRepository).save(order);
    }

    @Test
    void getAllOrders_returnsRepositoryResult() {
        when(orderRepository.findAllByOrderByIdDesc()).thenReturn(List.of(order));

        List<Order> result = orderService.getAllOrders();

        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().getId());
    }

    @Test
    void getOrderById_throwsWhenNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(99L));
    }

    @Test
    void updateOrder_mapsAndSavesExistingEntity() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        Order result = orderService.updateOrder(1L, request);

        assertEquals(1L, result.getId());
        verify(orderMapper).copyToExisting(request, order);
        verify(orderRepository).save(order);
    }

    @Test
    void updateOrder_throwsWhenStatusIsCompleted() {
        order.setStatus(OrderStatus.COMPLETED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderStatusTransitionException.class, () -> orderService.updateOrder(1L, request));
        verify(orderRepository, never()).save(order);
    }

    @Test
    void updateStatus_allowsValidTransition() {
        order.setStatus(OrderStatus.PAID);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        Order result = orderService.updateStatus(1L, OrderStatus.PURCHASED);

        assertEquals(OrderStatus.PURCHASED, result.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void updateStatus_allowsPendingToPaid() {
        order.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        Order result = orderService.updateStatus(1L, OrderStatus.PAID);

        assertEquals(OrderStatus.PAID, result.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void updateStatus_allowsPendingToCancelled() {
        order.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        Order result = orderService.updateStatus(1L, OrderStatus.CANCELLED);

        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void updateStatus_allowsPurchasedToShipped() {
        order.setStatus(OrderStatus.PURCHASED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        Order result = orderService.updateStatus(1L, OrderStatus.SHIPPED);

        assertEquals(OrderStatus.SHIPPED, result.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void updateStatus_allowsShippedToCompleted() {
        order.setStatus(OrderStatus.SHIPPED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        Order result = orderService.updateStatus(1L, OrderStatus.COMPLETED);

        assertEquals(OrderStatus.COMPLETED, result.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void updateStatus_allowsSameStatusTransition() {
        order.setStatus(OrderStatus.PAID);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        Order result = orderService.updateStatus(1L, OrderStatus.PAID);

        assertEquals(OrderStatus.PAID, result.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void updateStatus_rejectsInvalidTransition() {
        order.setStatus(OrderStatus.PAID);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderStatusTransitionException.class,
                () -> orderService.updateStatus(1L, OrderStatus.COMPLETED));
        verify(orderRepository, never()).save(order);
    }

    @Test
    void updateStatus_rejectsWhenCurrentStatusCompleted() {
        order.setStatus(OrderStatus.COMPLETED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderStatusTransitionException.class,
                () -> orderService.updateStatus(1L, OrderStatus.CANCELLED));
        verify(orderRepository, never()).save(order);
    }

    @Test
    void updateStatus_rejectsWhenCurrentStatusCancelled() {
        order.setStatus(OrderStatus.CANCELLED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderStatusTransitionException.class,
                () -> orderService.updateStatus(1L, OrderStatus.PAID));
        verify(orderRepository, never()).save(order);
    }

    @Test
    void cancelOrderById_setsCancelledWhenAllowed() {
        order.setStatus(OrderStatus.PAID);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        Order result = orderService.cancelOrderById(1L);

        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrderById_rejectsInvalidCancellation() {
        order.setStatus(OrderStatus.SHIPPED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderStatusTransitionException.class, () -> orderService.cancelOrderById(1L));
        verify(orderRepository, never()).save(order);
    }

    @Test
    void cancelOrderById_throwsWhenNotFound() {
        when(orderRepository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.cancelOrderById(42L));
    }

    @Test
    void createOrder_throwsWhenTitiperEqualsJastiper() {
        request.setTitiperUserId("same-user");
        request.setJastiperUserId("same-user");

        assertThrows(SelfPurchaseNotAllowedException.class, () -> orderService.createOrder(request));
        verify(orderRepository, never()).save(order);
    }

    @Test
    void updateOrder_throwsWhenTitiperEqualsJastiper() {
        request.setTitiperUserId("same-user");
        request.setJastiperUserId("same-user");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(SelfPurchaseNotAllowedException.class, () -> orderService.updateOrder(1L, request));
        verify(orderRepository, never()).save(order);
    }
}
