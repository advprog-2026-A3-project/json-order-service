package id.ac.ui.cs.advprog.order.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "order_table") // Menghindari reserved word SQL
@Getter @Setter
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String productName;
    private Integer quantity;
    private String shippingAddress;
    private String status;
}