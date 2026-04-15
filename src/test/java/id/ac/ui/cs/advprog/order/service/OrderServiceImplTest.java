package id.ac.ui.cs.advprog.order.service;

import id.ac.ui.cs.advprog.order.dto.OrderCreateRequest;
import id.ac.ui.cs.advprog.order.exception.OrderNotFoundException;
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
        assertEquals(1L, result.get(0).getId());
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
    void deleteOrderById_deletesExistingEntity() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.deleteOrderById(1L);

        verify(orderRepository).delete(order);
    }

    @Test
    void deleteOrderById_throwsWhenNotFound() {
        when(orderRepository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.deleteOrderById(42L));
    }
}
