package com.project.shopapp.responses;

import com.project.shopapp.models.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductListResponse {
    private List<ProductResponse>  products;
    private int totalPages;
}
