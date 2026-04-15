package id.ac.ui.cs.advprog.order.controller;

import id.ac.ui.cs.advprog.order.dto.OrderCreateRequest;
import id.ac.ui.cs.advprog.order.exception.OrderNotFoundException;
import id.ac.ui.cs.advprog.order.model.Order;
import id.ac.ui.cs.advprog.order.service.OrderMapper;
import id.ac.ui.cs.advprog.order.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    public OrderController(OrderService orderService, OrderMapper orderMapper) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
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
            Model model
    ) {
        List<Order> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        model.addAttribute("successMessage", successMessage);
        model.addAttribute("errorMessage", errorMessage);
        return "order-list";
    }

    @GetMapping("/edit/{id}")
    public String editOrderPage(@PathVariable Long id, Model model) {
        try {
            Order order = orderService.getOrderById(id);
            model.addAttribute("orderId", id);
            model.addAttribute("checkoutRequest", orderMapper.toRequest(order));
            model.addAttribute("status", order.getStatus());
            return "edit-order";
        } catch (OrderNotFoundException ex) {
            return "redirect:/order/list?error=Order+tidak+ditemukan";
        }
    }

    @PostMapping("/edit/{id}")
    public String editOrder(@PathVariable Long id, @ModelAttribute("checkoutRequest") OrderCreateRequest request) {
        try {
            orderService.updateOrder(id, request);
            return "redirect:/order/list?success=Order+berhasil+diupdate";
        } catch (OrderNotFoundException ex) {
            return "redirect:/order/list?error=Order+tidak+ditemukan";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteOrder(@PathVariable Long id) {
        try {
            orderService.deleteOrderById(id);
            return "redirect:/order/list?success=Order+berhasil+dihapus";
        } catch (OrderNotFoundException ex) {
            return "redirect:/order/list?error=Order+tidak+ditemukan";
        }
    }
}
