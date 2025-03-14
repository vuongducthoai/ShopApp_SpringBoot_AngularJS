package com.project.shopapp.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProductResponse extends BaseResponse {
    private String name;
    private float price;
    private String thumbnail;
    private String description;
    @JsonProperty("category_id")
    private int categoryId;
}
