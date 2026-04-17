package id.ac.ui.cs.advprog.order.service;

import id.ac.ui.cs.advprog.order.dto.OrderCreateRequest;
import id.ac.ui.cs.advprog.order.exception.InvalidOrderStatusTransitionException;
import id.ac.ui.cs.advprog.order.exception.OrderNotFoundException;
import id.ac.ui.cs.advprog.order.exception.SelfPurchaseNotAllowedException;
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
        validateNotSelfPurchase(request);
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
        if (existingOrder.getStatus() == OrderStatus.CANCELLED || existingOrder.getStatus() == OrderStatus.COMPLETED) {
            throw new InvalidOrderStatusTransitionException(existingOrder.getStatus(), existingOrder.getStatus());
        }
        validateNotSelfPurchase(request);
        orderMapper.copyToExisting(request, existingOrder);
        return orderRepository.save(existingOrder);
    }

    @Override
    @Transactional
    public Order updateStatus(Long id, OrderStatus newStatus) {
        Order existingOrder = findExistingOrder(id);
        OrderStatus currentStatus = existingOrder.getStatus();

        if (!canTransition(currentStatus, newStatus)) {
            throw new InvalidOrderStatusTransitionException(currentStatus, newStatus);
        }

        existingOrder.setStatus(newStatus);
        return orderRepository.save(existingOrder);
    }

    @Override
    @Transactional
    public Order cancelOrderById(Long id) {
        Order existingOrder = findExistingOrder(id);
        OrderStatus currentStatus = existingOrder.getStatus();

        if (!canCancel(currentStatus)) {
            throw new InvalidOrderStatusTransitionException(currentStatus, OrderStatus.CANCELLED);
        }

        existingOrder.setStatus(OrderStatus.CANCELLED);
        return orderRepository.save(existingOrder);
    }

    private boolean canTransition(OrderStatus current, OrderStatus next) {
        if (current == next) {
            return true;
        }
        return switch (current) {
            case PENDING -> next == OrderStatus.PAID || next == OrderStatus.CANCELLED;
            case PAID -> next == OrderStatus.PURCHASED || next == OrderStatus.CANCELLED;
            case PURCHASED -> next == OrderStatus.SHIPPED || next == OrderStatus.CANCELLED;
            case SHIPPED -> next == OrderStatus.COMPLETED;
            case COMPLETED, CANCELLED -> false;
        };
    }

    private boolean canCancel(OrderStatus currentStatus) {
        return currentStatus == OrderStatus.PENDING
                || currentStatus == OrderStatus.PAID
                || currentStatus == OrderStatus.PURCHASED;
    }

    private void validateNotSelfPurchase(OrderCreateRequest request) {
        String titiperId = request.getTitiperUserId();
        String jastiperId = request.getJastiperUserId();
        if (titiperId != null && !titiperId.isBlank() && titiperId.equals(jastiperId)) {
            throw new SelfPurchaseNotAllowedException();
        }
    }

    private Order findExistingOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }
}
