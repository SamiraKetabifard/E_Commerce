package com.example.e_commerce_restapi.service;

import com.example.e_commerce_restapi.entity.*;
import com.example.e_commerce_restapi.repository.CartRepository;
import com.example.e_commerce_restapi.repository.ProductRepository;
import com.example.e_commerce_restapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceUnitTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartService cartService;

    @Captor
    private ArgumentCaptor<Cart> cartCaptor;

    private User testUser;
    private Product testProduct;
    private Cart testCart;
    private CartItem testCartItem;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("p@gmail.com");
        testUser.setName("pari");
        testUser.setRole(Role.CUSTOMER);

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("iPhone 14");
        testProduct.setPrice(new BigDecimal("999.99"));
        testProduct.setStock(10);

        testCart = new Cart();
        testCart.setId(1L);
        testCart.setUser(testUser);
        testCart.setItems(new ArrayList<>());

        testCartItem = new CartItem();
        testCartItem.setId(1L);
        testCartItem.setProduct(testProduct);
        testCartItem.setQuantity(2);
        testCartItem.setCart(testCart);
    }

    @Test
    void getCart_WithExistingCart_ShouldReturnCart() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(any(User.class))).thenReturn(Optional.of(testCart));

        Cart result = cartService.getCart("p@gmail.com");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUser()).isEqualTo(testUser);

        verify(userRepository).findByEmail("p@gmail.com");
        verify(cartRepository).findByUser(testUser);
        verify(cartRepository, never()).save(any());
    }

    @Test
    void getCart_WithNoExistingCart_ShouldCreateNewCart() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(any(User.class))).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        Cart result = cartService.getCart("p@gmail.com");

        assertThat(result).isNotNull();

        verify(cartRepository).save(cartCaptor.capture());
        Cart newCart = cartCaptor.getValue();
        assertThat(newCart.getUser()).isEqualTo(testUser);
        assertThat(newCart.getItems()).isEmpty();
    }

    @Test
    void getCart_WithInvalidUser_ShouldThrowException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.getCart("invalid@gmail.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");

        verify(cartRepository, never()).findByUser(any());
    }

    @Test
    void addItem_WithValidData_ShouldAddItemToCart() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(any(User.class))).thenReturn(Optional.of(testCart));
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        Cart result = cartService.addItem(1L, 2, "p@gmail.com");

        assertThat(result).isNotNull();

        verify(cartRepository).save(cartCaptor.capture());
        Cart savedCart = cartCaptor.getValue();
        assertThat(savedCart.getItems()).hasSize(1);

        CartItem addedItem = savedCart.getItems().get(0);
        assertThat(addedItem.getProduct()).isEqualTo(testProduct);
        assertThat(addedItem.getQuantity()).isEqualTo(2);
    }

    @Test
    void addItem_WithInvalidProduct_ShouldThrowException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(any(User.class))).thenReturn(Optional.of(testCart));
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItem(999L, 2, "p@gmail.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("product not found");

        verify(cartRepository, never()).save(any());
    }

    @Test
    void addItem_WhenCartHasExistingItem_ShouldAddNewItem() {
        testCart.getItems().add(testCartItem);

        Product newProduct = new Product();
        newProduct.setId(2L);
        newProduct.setName("MacBook Pro");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(any(User.class))).thenReturn(Optional.of(testCart));
        when(productRepository.findById(2L)).thenReturn(Optional.of(newProduct));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        Cart result = cartService.addItem(2L, 1, "p@gmail.com");

        assertThat(result.getItems()).hasSize(2);

        verify(cartRepository).save(any());
    }
}
