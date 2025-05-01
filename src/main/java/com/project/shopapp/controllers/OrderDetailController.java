package com.project.shopapp.controllers;

import com.project.shopapp.dtos.OrderDetailDTO;
import com.project.shopapp.models.OrderDetail;
import com.project.shopapp.responses.OrderDetailResponse;
import com.project.shopapp.services.Impl.OrderDetailService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("${api.prefix}/order_details")
@RequiredArgsConstructor
public class OrderDetailController {
    private final OrderDetailService orderDetailService;

    //Thêm mới 1 order detail
    @PostMapping
    public ResponseEntity<?> createOrderDetail
    (@Valid @RequestBody OrderDetailDTO orderDetailDTO) {
        OrderDetail newOrderDetail = orderDetailService.createOrderDetail(orderDetailDTO);
        return ResponseEntity.ok().body(OrderDetailResponse.fromOrderDetail(newOrderDetail));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderDetail(@PathVariable Long id) {
            return ResponseEntity.ok().body(OrderDetailResponse.fromOrderDetail(orderDetailService.getOrderDetail(id)));
    }

    //Lay ra danh sach cac order_Details cua 1 order nào đó
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getListOrderDetails(
            @Min(1) @PathVariable Long orderId ) {
        List<OrderDetail> orderDetails = orderDetailService.findByOrderId(orderId);
        List<OrderDetailResponse> orderDetailResponses = orderDetails
                .stream()
                .map(OrderDetailResponse::fromOrderDetail).toList();
        return ResponseEntity.ok(orderDetailResponses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrderDetail(
            @Min(1) @PathVariable Long id, @Valid @RequestBody
            OrderDetailDTO orderDetailDTO
    ) {
        try {
           OrderDetail orderDetail =  orderDetailService.updateOrderDetail(id, orderDetailDTO);
            return ResponseEntity.ok().
                    body(orderDetail);
        } catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteOrderDetail(@Valid @PathVariable Long id) {
        orderDetailService.deleteOrderDetail(id);
        return ResponseEntity.ok().body("deleteOrderDetail with id " + id);
    }
}
