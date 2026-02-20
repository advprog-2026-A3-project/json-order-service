package id.ac.ui.cs.advprog.order.controller;

import id.ac.ui.cs.advprog.order.model.Order;
import id.ac.ui.cs.advprog.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @GetMapping("/create")
    public String createOrderForm(Model model) {
        model.addAttribute("order", new Order());
        return "create-order"; // Ke file create-order.html
    }

    @PostMapping("/create")
    public String submitOrder(@ModelAttribute Order order) {
        order.setStatus("PAID"); // Status awal setelah checkout sukses [cite: 126]
        orderRepository.save(order);
        return "redirect:/order/list";
    }

    @GetMapping("/list")
    public String orderHistory(Model model) {
        model.addAttribute("orders", orderRepository.findAll());
        return "order-list"; // Ke file order-list.html
    }
}