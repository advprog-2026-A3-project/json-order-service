package id.ac.ui.cs.advprog.order.service;

import id.ac.ui.cs.advprog.order.dto.OrderCreateRequest;
import id.ac.ui.cs.advprog.order.model.Order;
import id.ac.ui.cs.advprog.order.model.OrderStatus;

import java.util.List;

public interface OrderService {
    Order createOrder(OrderCreateRequest request);

    List<Order> getAllOrders();

    Order getOrderById(Long id);

    Order updateOrder(Long id, OrderCreateRequest request);

    Order updateStatus(Long id, OrderStatus newStatus);

    Order cancelOrderById(Long id);
}
