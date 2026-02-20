package id.ac.ui.cs.advprog.order.service;

import id.ac.ui.cs.advprog.order.model.Order;
import java.util.List;

public interface OrderService {
    Order createOrder(Order order);
    List<Order> findAllOrders();
    Order updateStatus(Long id, String status);
}