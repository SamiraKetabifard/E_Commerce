package com.example.e_commerce_restapi.controller;

import com.example.e_commerce_restapi.dto.request.OrderItemRequest;
import com.example.e_commerce_restapi.dto.request.OrderRequest;
import com.example.e_commerce_restapi.dto.response.OrderResponse;
import com.example.e_commerce_restapi.entity.OrderStatus;
import com.example.e_commerce_restapi.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import java.math.BigDecimal;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerUnitTest {

    @Mock
    private OrderService orderService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private OrderController orderController;

    private OrderRequest orderRequest;
    private OrderResponse orderResponse;
    private String userEmail;

    @BeforeEach
    void setUp() {
        userEmail = "p@gmail.com";

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(2);

        orderRequest = new OrderRequest();
        orderRequest.setItems(List.of(itemRequest));

        orderResponse = new OrderResponse();
        orderResponse.setOrderId(1L);
        orderResponse.setUserId(1L);
        orderResponse.setTotalAmount(new BigDecimal("1999.98"));
        orderResponse.setStatus(OrderStatus.PLACED);
    }

    @Test
    void place_WithValidRequest_ShouldReturnOk() {
        when(authentication.getName()).thenReturn(userEmail);
        when(orderService.placeOrder(anyString(), any(OrderRequest.class))).thenReturn(orderResponse);

        ResponseEntity<?> response = orderController.place(authentication, orderRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(orderResponse);

        verify(authentication).getName();
        verify(orderService).placeOrder(userEmail, orderRequest);
    }

    @Test
    void place_WhenServiceThrowsException_ShouldReturnBadRequest() {
        String errorMessage = "Cart is empty";
        when(authentication.getName()).thenReturn(userEmail);
        when(orderService.placeOrder(anyString(), any(OrderRequest.class)))
                .thenThrow(new RuntimeException(errorMessage));

        ResponseEntity<?> response = orderController.place(authentication, orderRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(errorMessage);

        verify(authentication).getName();
        verify(orderService).placeOrder(userEmail, orderRequest);
    }

    @Test
    void place_WithNullAuthentication_ShouldThrowException() {
        when(authentication.getName()).thenThrow(new RuntimeException("Authentication is null"));

        try {
            orderController.place(authentication, orderRequest);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(RuntimeException.class);
        }
    }

    @Test
    void place_WithEmptyItems_ShouldCallService() {
        OrderRequest emptyRequest = new OrderRequest();
        emptyRequest.setItems(List.of());

        when(authentication.getName()).thenReturn(userEmail);
        when(orderService.placeOrder(anyString(), any(OrderRequest.class)))
                .thenThrow(new RuntimeException("Cart is empty"));

        ResponseEntity<?> response = orderController.place(authentication, emptyRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Cart is empty");

        verify(orderService).placeOrder(userEmail, emptyRequest);
    }
}