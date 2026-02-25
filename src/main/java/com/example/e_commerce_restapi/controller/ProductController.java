package com.example.e_commerce_restapi.controller;

import com.example.e_commerce_restapi.dto.request.ProductRequest;
import com.example.e_commerce_restapi.dto.response.ProductResponse;
import com.example.e_commerce_restapi.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping(path = "/admin/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public ResponseEntity<List<ProductResponse>> getAllProducts(){
        return ResponseEntity.ok(productService.getAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> create(@RequestBody ProductRequest request, @RequestParam Long categoryId) {

        return ResponseEntity.ok(productService.create(request,categoryId));
    }

    @PutMapping(path = "/{Id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> update(@PathVariable Long Id,@RequestBody ProductRequest request){
        return ResponseEntity.ok(productService.update(Id, request));
    }

    @DeleteMapping(path = "/{Id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long Id){
        productService.delete(Id);
    }
}
