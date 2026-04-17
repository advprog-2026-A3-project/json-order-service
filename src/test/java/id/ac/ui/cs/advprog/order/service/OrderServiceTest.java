package id.ac.ui.cs.advprog.order.service;

import id.ac.ui.cs.advprog.order.dto.OrderCreateRequest;
import id.ac.ui.cs.advprog.order.model.Order;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderServiceTest {

    private final OrderMapper mapper = new OrderMapperImpl();

    @Test
    void toEntity_mapsAllFields() {
        OrderCreateRequest request = createRequest();

        Order order = mapper.toEntity(request);

        assertEquals("PROD-001", order.getProductId());
        assertEquals("AirTag", order.getProductName());
        assertEquals("titiper-1", order.getTitiperUserId());
        assertEquals("jastiper-1", order.getJastiperUserId());
        assertEquals(2, order.getQuantity());
        assertEquals(new BigDecimal("50000"), order.getTotalPrice());
        assertEquals("Depok", order.getShippingAddress());
    }

    @Test
    void toRequest_mapsOrderForEditForm() {
        Order order = new Order();
        order.setProductId("PROD-777");
        order.setProductName("Power Adapter");
        order.setTitiperUserId("titiper-x");
        order.setJastiperUserId("jastiper-x");
        order.setQuantity(1);
        order.setTotalPrice(new BigDecimal("399000"));
        order.setShippingAddress("Jakarta");

        OrderCreateRequest request = mapper.toRequest(order);

        assertEquals("PROD-777", request.getProductId());
        assertEquals("Power Adapter", request.getProductName());
        assertEquals("titiper-x", request.getTitiperUserId());
        assertEquals("jastiper-x", request.getJastiperUserId());
        assertEquals(1, request.getQuantity());
        assertEquals(new BigDecimal("399000"), request.getTotalPrice());
        assertEquals("Jakarta", request.getShippingAddress());
    }

    private OrderCreateRequest createRequest() {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setProductId("PROD-001");
        request.setProductName("AirTag");
        request.setTitiperUserId("titiper-1");
        request.setJastiperUserId("jastiper-1");
        request.setQuantity(2);
        request.setTotalPrice(new BigDecimal("50000"));
        request.setShippingAddress("Depok");
        return request;
    }
}