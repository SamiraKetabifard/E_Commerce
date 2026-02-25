package com.example.e_commerce_restapi.service;

import com.example.e_commerce_restapi.entity.Cart;
import com.example.e_commerce_restapi.entity.CartItem;
import com.example.e_commerce_restapi.entity.Product;
import com.example.e_commerce_restapi.entity.User;
import com.example.e_commerce_restapi.repository.CartRepository;
import com.example.e_commerce_restapi.repository.ProductRepository;
import com.example.e_commerce_restapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public Cart getCart(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->{
                    return new RuntimeException("User not found");
                });

        Cart cart= cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
        return cart;
    }
    public Cart addItem(Long productId, Integer quantity, String username){

        Cart cart= getCart(username);
        Product product= productRepository.findById(productId)
                .orElseThrow(()->{
                    return new RuntimeException("product not found");
                });
        CartItem item= new CartItem();
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setCart(cart);
        cart.getItems().add(item);
        return  cartRepository.save(cart);
    }
}