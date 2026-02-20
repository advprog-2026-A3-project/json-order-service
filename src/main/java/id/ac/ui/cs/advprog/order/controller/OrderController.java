package id.ac.ui.cs.advprog.order.controller;

import id.ac.ui.cs.advprog.order.model.Order;
import id.ac.ui.cs.advprog.order.service.OrderService; // Pastikan import service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService; // Perubahan utama: Gunakan Service

    @GetMapping("/create")
    public String createOrderForm(Model model) {
        model.addAttribute("order", new Order());
        return "create-order";
    }

    @PostMapping("/create")
    public String submitOrder(@ModelAttribute Order order) {
        // Logika status "PAID" dipindahkan ke Service
        orderService.createOrder(order);
        return "redirect:/order/list";
    }

    @GetMapping("/list")
    public String orderHistory(Model model) {
        model.addAttribute("orders", orderService.findAllOrders());
        return "order-list";
    }
}