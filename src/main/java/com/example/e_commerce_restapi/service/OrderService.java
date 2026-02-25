package com.example.e_commerce_restapi.service;

import com.example.e_commerce_restapi.dto.request.OrderRequest;
import com.example.e_commerce_restapi.dto.response.OrderItemResponse;
import com.example.e_commerce_restapi.dto.response.OrderResponse;
import com.example.e_commerce_restapi.entity.*;
import com.example.e_commerce_restapi.repository.CartRepository;
import com.example.e_commerce_restapi.repository.OrderRepository;
import com.example.e_commerce_restapi.repository.ProductRepository;
import com.example.e_commerce_restapi.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public OrderResponse placeOrder(String email, OrderRequest request) {


        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->{
                    return   new RuntimeException("User not found");
                });
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() ->{
                    return new RuntimeException("Cart empty");
                });
        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PLACED);
        order.setOrderItems(new ArrayList<>());

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem ci : cart.getItems()) {

            Product product = ci.getProduct();
            if (product.getStock() < ci.getQuantity()){
                throw new RuntimeException("Insufficient stock");
            }
            product.setStock(product.getStock() - ci.getQuantity());
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
        cart.getItems().clear(); // Empty cart after order
        Order saveOrder=  orderRepository.save(order);
        return mapToOrderResponse(saveOrder);
    }
    private OrderItemResponse mapToOrderItemResponse(OrderItem item) {

        OrderItemResponse dto = new OrderItemResponse();
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setPriceAtPurchase(item.getPriceAtPurchase());
        dto.setQuantity(item.getQuantity());

        dto.setSubTotal(
                item.getPriceAtPurchase()
                        .multiply(BigDecimal.valueOf(item.getQuantity()))
        );
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
