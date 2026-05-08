package id.ac.ui.cs.advprog.order.client;

import id.ac.ui.cs.advprog.order.dto.inventory.InventoryProductResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Component
public class InventoryClient {

    @Value("${inventory.service.base-url}")
    private String inventoryServiceBaseUrl;

    private final RestTemplate restTemplate;

    // BARU(Order - Inventory): 1. Inject RestTemplate untuk call Inventory Service
    public InventoryClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public InventoryProductResponse getProductById(String productId) {
        try {
            // BARU(Order - Inventory): 2. Fetch Product dari Inventory Service berdasarkan productId
            InventoryProductResponse product = restTemplate.getForObject(
                    inventoryServiceBaseUrl + "/api/products/" + productId,
                    InventoryProductResponse.class
            );

            // BARU(Order - Inventory): 3. Validasi response Inventory tidak kosong
            if (product == null || product.getId() == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found in inventory"
                );
            }

            return product;
        } catch (HttpClientErrorException.NotFound e) {
            // BARU(Order - Inventory): 4. Mapping error kalau product tidak ditemukan di Inventory
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Product not found in inventory"
            );
        } catch (HttpClientErrorException e) {
            // BARU(Order - Inventory): 5. Mapping error kalau Inventory Service gagal diakses
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Failed to fetch product from inventory"
            );
        }
    }

    // BARU(Order - Inventory): 6. Mengurangi stok product di Inventory setelah checkout berhasil
    public void reduceProductStock(String productId, Integer quantity) {
        try {
            restTemplate.exchange(
                    inventoryServiceBaseUrl + "/api/products/" + productId + "/stock/reduce?quantity=" + quantity,
                    HttpMethod.PATCH,
                    null,
                    Void.class
            );
        } catch (HttpClientErrorException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Failed to reduce product stock in inventory"
            );
        }
    }
}