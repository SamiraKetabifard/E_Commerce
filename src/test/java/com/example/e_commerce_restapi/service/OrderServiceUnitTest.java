package com.example.e_commerce_restapi.service;

import com.example.e_commerce_restapi.dto.request.OrderItemRequest;
import com.example.e_commerce_restapi.dto.request.OrderRequest;
import com.example.e_commerce_restapi.dto.response.OrderResponse;
import com.example.e_commerce_restapi.entity.*;
import com.example.e_commerce_restapi.repository.CartRepository;
import com.example.e_commerce_restapi.repository.OrderRepository;
import com.example.e_commerce_restapi.repository.ProductRepository;
import com.example.e_commerce_restapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class OrderServiceUnitTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    private User testUser;
    private Product testProduct1;
    private Product testProduct2;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("p@gmail.com");
        testUser.setName("pari");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.CUSTOMER);
        testUser = userRepository.save(testUser);

        testProduct1 = new Product();
        testProduct1.setName("iPhone 14");
        testProduct1.setDescription("Apple iPhone");
        testProduct1.setPrice(new BigDecimal("999.99"));
        testProduct1.setStock(10);
        testProduct1 = productRepository.save(testProduct1);

        testProduct2 = new Product();
        testProduct2.setName("AirPods");
        testProduct2.setDescription("Apple AirPods");
        testProduct2.setPrice(new BigDecimal("199.99"));
        testProduct2.setStock(20);
        testProduct2 = productRepository.save(testProduct2);

        testCart = new Cart();
        testCart.setUser(testUser);
        testCart.setItems(new java.util.ArrayList<>());
        testCart = cartRepository.save(testCart);
    }

    private void addItemToCart(Long productId, int quantity) {
        Cart cart = cartRepository.findByUser(testUser).orElseThrow();
        Product product = productRepository.findById(productId).orElseThrow();

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        cartItem.setCart(cart);
        cart.getItems().add(cartItem);

        cartRepository.save(cart);
    }

    private void clearCart() {
        Cart cart = cartRepository.findByUser(testUser).orElseThrow();
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    @Test
    void placeOrder_WithValidCart_ShouldCreateOrder() {
        addItemToCart(testProduct1.getId(), 2);
        addItemToCart(testProduct2.getId(), 1);

        OrderResponse response = orderService.placeOrder("p@gmail.com", new OrderRequest());

        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isNotNull();
        assertThat(response.getTotalAmount()).isEqualTo(new BigDecimal("2199.97"));
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PLACED);
        assertThat(response.getItems()).hasSize(2);

        Product updatedProduct1 = productRepository.findById(testProduct1.getId()).get();
        Product updatedProduct2 = productRepository.findById(testProduct2.getId()).get();
        assertThat(updatedProduct1.getStock()).isEqualTo(8);
        assertThat(updatedProduct2.getStock()).isEqualTo(19);

        Cart updatedCart = cartRepository.findByUser(testUser).orElse(null);
        assertThat(updatedCart).isNotNull();
        assertThat(updatedCart.getItems()).isEmpty();
    }
    @Test
    void placeOrder_WithEmptyCart_ShouldThrowException() {
        clearCart();

        assertThatThrownBy(() -> orderService.placeOrder("p@gmail.com", new OrderRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cart is empty");
    }
    @Test
    void placeOrder_WithInsufficientStock_ShouldThrowException() {
        addItemToCart(testProduct1.getId(), 2);

        testProduct1.setStock(1);
        productRepository.save(testProduct1);

        assertThatThrownBy(() -> orderService.placeOrder("p@gmail.com", new OrderRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Insufficient stock");
    }
    @Test
    void placeOrder_WithInvalidUser_ShouldThrowException() {
        addItemToCart(testProduct1.getId(), 1);

        assertThatThrownBy(() -> orderService.placeOrder("invalid@gmail.com", new OrderRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }
    @Test
    void placeOrder_WithProductNotInCart_ShouldNotAffectOrder() {
        addItemToCart(testProduct1.getId(), 1);

        OrderResponse response = orderService.placeOrder("p@gmail.com", new OrderRequest());

        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getTotalAmount()).isEqualTo(new BigDecimal("999.99"));
    }
}