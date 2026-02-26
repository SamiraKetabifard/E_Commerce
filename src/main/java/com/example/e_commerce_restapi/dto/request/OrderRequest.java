package com.example.e_commerce_restapi.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class OrderRequest {

    @NotEmpty
    private List<OrderItemRequest> items;
}
