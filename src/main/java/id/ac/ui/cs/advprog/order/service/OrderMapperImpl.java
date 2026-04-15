package id.ac.ui.cs.advprog.order.service;

import id.ac.ui.cs.advprog.order.dto.OrderCreateRequest;
import id.ac.ui.cs.advprog.order.model.Order;
import org.springframework.stereotype.Component;

@Component
public class OrderMapperImpl implements OrderMapper {

    @Override
    public Order toEntity(OrderCreateRequest request) {
        Order order = new Order();
        copyToExisting(request, order);
        return order;
    }

    @Override
    public void copyToExisting(OrderCreateRequest request, Order target) {
        target.setProductId(request.getProductId());
        target.setProductName(request.getProductName());
        target.setTitiperUserId(request.getTitiperUserId());
        target.setJastiperUserId(request.getJastiperUserId());
        target.setQuantity(request.getQuantity());
        target.setTotalPrice(request.getTotalPrice());
        target.setShippingAddress(request.getShippingAddress());
    }

    @Override
    public OrderCreateRequest toRequest(Order order) {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setProductId(order.getProductId());
        request.setProductName(order.getProductName());
        request.setTitiperUserId(order.getTitiperUserId());
        request.setJastiperUserId(order.getJastiperUserId());
        request.setQuantity(order.getQuantity());
        request.setTotalPrice(order.getTotalPrice());
        request.setShippingAddress(order.getShippingAddress());
        return request;
    }
}

