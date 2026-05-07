package id.ac.ui.cs.advprog.order.client;

import id.ac.ui.cs.advprog.order.dto.inventory.InventoryProductResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Component
public class InventoryClient {

    @Value("${inventory.service.base-url}")
    private String inventoryServiceBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public InventoryProductResponse getProductById(String productId) {
        try {
            InventoryProductResponse product = restTemplate.getForObject(
                    inventoryServiceBaseUrl + "/api/products/" + productId,
                    InventoryProductResponse.class
            );

            if (product == null || product.getId() == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found in inventory"
                );
            }

            return product;
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Product not found in inventory"
            );
        } catch (HttpClientErrorException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Failed to fetch product from inventory"
            );
        }
    }
}