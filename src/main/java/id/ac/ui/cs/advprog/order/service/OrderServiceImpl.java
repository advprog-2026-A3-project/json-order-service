package id.ac.ui.cs.advprog.order.service;

import id.ac.ui.cs.advprog.order.dto.OrderCreateRequest;
import id.ac.ui.cs.advprog.order.exception.OrderNotFoundException;
import id.ac.ui.cs.advprog.order.model.Order;
import id.ac.ui.cs.advprog.order.model.OrderStatus;
import id.ac.ui.cs.advprog.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    public OrderServiceImpl(OrderRepository orderRepository, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
    }

    @Override
    @Transactional
    public Order createOrder(OrderCreateRequest request) {
        Order order = orderMapper.toEntity(request);
        order.setStatus(OrderStatus.PENDING);
        return orderRepository.save(order);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByIdDesc();
    }

    @Override
    public Order getOrderById(Long id) {
        return findExistingOrder(id);
    }

    @Override
    @Transactional
    public Order updateOrder(Long id, OrderCreateRequest request) {
        Order existingOrder = findExistingOrder(id);
        orderMapper.copyToExisting(request, existingOrder);
        return orderRepository.save(existingOrder);
    }

    @Override
    @Transactional
    public void deleteOrderById(Long id) {
        Order existingOrder = findExistingOrder(id);
        orderRepository.delete(existingOrder);
    }

    private Order findExistingOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }
}
