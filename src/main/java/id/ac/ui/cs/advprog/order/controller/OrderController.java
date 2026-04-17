package id.ac.ui.cs.advprog.order.controller;

import id.ac.ui.cs.advprog.order.dto.OrderCreateRequest;
import id.ac.ui.cs.advprog.order.model.Order;
import id.ac.ui.cs.advprog.order.model.OrderStatus;
import id.ac.ui.cs.advprog.order.model.Rating;
import id.ac.ui.cs.advprog.order.service.OrderMapper;
import id.ac.ui.cs.advprog.order.service.OrderService;
import id.ac.ui.cs.advprog.order.service.RatingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;
    private final RatingService ratingService;

    public OrderController(OrderService orderService, OrderMapper orderMapper, RatingService ratingService) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
        this.ratingService = ratingService;
    }

    @GetMapping("/create")
    public String createOrderPage(Model model) {
        model.addAttribute("checkoutRequest", new OrderCreateRequest());
        return "create-order";
    }

    @PostMapping("/create")
    public String createOrder(@ModelAttribute("checkoutRequest") OrderCreateRequest request) {
        orderService.createOrder(request);
        return "redirect:/order/list?success=Order+berhasil+disimpan";
    }

    @GetMapping("/list")
    public String orderList(
            @RequestParam(value = "success", required = false) String successMessage,
            @RequestParam(value = "error", required = false) String errorMessage,
            @RequestParam(value = "viewer", defaultValue = "titiper") String viewer,
            Model model
    ) {
        List<Order> orders = orderService.getAllOrders();
        List<Long> orderIds = orders.stream().map(Order::getId).toList();
        Map<Long, Rating> ratingsByOrderId = ratingService.getRatingsByOrderIds(orderIds);

        model.addAttribute("orders", orders);
        model.addAttribute("ratingsByOrderId", ratingsByOrderId);
        model.addAttribute("viewer", viewer);
        model.addAttribute("isTitiperView", "titiper".equalsIgnoreCase(viewer));
        model.addAttribute("isJastiperView", "jastiper".equalsIgnoreCase(viewer));
        model.addAttribute("successMessage", successMessage);
        model.addAttribute("errorMessage", errorMessage);
        return "order-list";
    }

    @GetMapping("/edit/{id}")
    public String editOrderPage(@PathVariable Long id, Model model) {
        Order order = orderService.getOrderById(id);
        model.addAttribute("orderId", id);
        model.addAttribute("checkoutRequest", orderMapper.toRequest(order));
        model.addAttribute("status", order.getStatus());
        return "edit-order";
    }

    @PostMapping("/edit/{id}")
    public String editOrder(@PathVariable Long id, @ModelAttribute("checkoutRequest") OrderCreateRequest request) {
        orderService.updateOrder(id, request);
        return "redirect:/order/list?success=Order+berhasil+diupdate";
    }

    @PostMapping("/status/{id}")
    public String updateOrderStatus(
            @PathVariable Long id,
            @RequestParam("status") OrderStatus status,
            @RequestParam(value = "viewer", defaultValue = "jastiper") String viewer
    ) {
        orderService.updateStatus(id, status);
        return "redirect:/order/list?viewer=" + viewer + "&success=Status+order+berhasil+diupdate";
    }

    @PostMapping("/cancel/{id}")
    public String cancelOrder(
            @PathVariable Long id,
            @RequestParam(value = "viewer", defaultValue = "jastiper") String viewer
    ) {
        orderService.cancelOrderById(id);
        return "redirect:/order/list?viewer=" + viewer + "&success=Order+berhasil+dibatalkan";
    }
}
