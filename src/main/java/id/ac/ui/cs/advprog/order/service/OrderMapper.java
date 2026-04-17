package id.ac.ui.cs.advprog.order.service;

import id.ac.ui.cs.advprog.order.dto.OrderCreateRequest;
import id.ac.ui.cs.advprog.order.model.Order;

public interface OrderMapper {
    Order toEntity(OrderCreateRequest request);

    void copyToExisting(OrderCreateRequest request, Order target);

    OrderCreateRequest toRequest(Order order);
}

