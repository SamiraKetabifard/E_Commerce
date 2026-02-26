package com.example.e_commerce_restapi.service;

import com.example.e_commerce_restapi.dto.EmailDetails;
import com.example.e_commerce_restapi.dto.request.OrderRequest;
import com.example.e_commerce_restapi.dto.response.OrderItemResponse;
import com.example.e_commerce_restapi.dto.response.OrderResponse;
import com.example.e_commerce_restapi.entity.*;
import com.example.e_commerce_restapi.repository.CartRepository;
import com.example.e_commerce_restapi.repository.OrderRepository;
import com.example.e_commerce_restapi.repository.ProductRepository;
import com.example.e_commerce_restapi.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final EmailService emailService;

    @Transactional
    public OrderResponse placeOrder(String email, OrderRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart empty"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PLACED);
        order.setOrderItems(new ArrayList<>());

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem ci : cart.getItems()) {

            Product product = productRepository.findById(ci.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            if (product.getStock() < ci.getQuantity()){
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            product.setStock(product.getStock() - ci.getQuantity());
            productRepository.save(product);

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(product);
            oi.setQuantity(ci.getQuantity());
            oi.setPriceAtPurchase(product.getPrice());
            order.getOrderItems().add(oi);

            total = total.add(
                    product.getPrice().multiply(
                            BigDecimal.valueOf(ci.getQuantity())));
        }

        order.setTotalAmount(total);

        // Clear the cart after saving the order
        cart.getItems().clear();
        cartRepository.save(cart);

        Order savedOrder = orderRepository.save(order);
        OrderResponse response = mapToOrderResponse(savedOrder);

        // Send confirmation email
        try {
            EmailDetails orderEmail = new EmailDetails();
            orderEmail.setRecipient(email);
            orderEmail.setSubject("Order Confirmation");
            orderEmail.setMessageBody(
                    "Your order #" + savedOrder.getId() + " has been placed successfully.\n" +
                            "Total Amount: $" + total + "\n\n" +
                            "Thank you for shopping with us!"
            );
            emailService.sendEmail(orderEmail);
        } catch (Exception e) {
            log.error("Failed to send email but order saved: {}", e.getMessage());
        }
        return response;
    }

    private OrderItemResponse mapToOrderItemResponse(OrderItem item) {
        OrderItemResponse dto = new OrderItemResponse();
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setPriceAtPurchase(item.getPriceAtPurchase());
        dto.setQuantity(item.getQuantity());
        dto.setSubTotal(
                item.getPriceAtPurchase()
                        .multiply(BigDecimal.valueOf(item.getQuantity())));
        return dto;
    }

    private OrderResponse mapToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getId());
        response.setUserId(order.getUser().getId());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setItems(
                order.getOrderItems()
                        .stream()
                        .map(this::mapToOrderItemResponse)
                        .toList());
        return response;
    }
}