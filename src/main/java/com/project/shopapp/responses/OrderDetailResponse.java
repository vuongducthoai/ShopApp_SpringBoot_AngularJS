package com.project.shopapp.responses;

import com.project.shopapp.models.OrderDetail;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDetailResponse {
    private Long id;
    private Long orderId;
    private Long productId;
    private Float price;
    private int numberOfProduct;
    private Float totalMoney;
    private String color;

    public static OrderDetailResponse fromOrderDetail(OrderDetail orderDetail) {
        OrderDetailResponse orderDetailResponse = OrderDetailResponse.builder()
                .id(orderDetail.getId())
                .orderId(orderDetail.getOrder().getId())
                .productId(orderDetail.getProduct().getId())
                .price(orderDetail.getPrice())
                .totalMoney(orderDetail.getTotalMoney())
                .color(orderDetail.getColor())
                .build();
        return orderDetailResponse;
    }
}
