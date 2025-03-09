package com.project.shopapp.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    @Min(value= 1, message = "User's ID mush be > 0")
    @JsonProperty("user_id")
    private long userId;

    @JsonProperty("fullname")
    private String fullName; // không yêu cầu validate

    private String email; // không yêu cầu validate

    @JsonProperty("phone_number")
    @NotBlank
    @Size(min = 5, message = "Phone number is required")
    private String phoneNumber;

    @JsonProperty("address")
    private String address; // không yêu cầu validate

    private String note; // không yêu cầu validate

    @JsonProperty("total_money")
    @Min(value = 0, message = "Total money must be >= 0")
    private Float totalMoney;

    @JsonProperty("shipping_method")
    private String shippingMethod;

    @JsonProperty("shipping_address")
    private String shippingAddress;

    @JsonProperty("payment_method")
    private String paymentMethod;
}
