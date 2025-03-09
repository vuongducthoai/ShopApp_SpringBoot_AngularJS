package com.project.shopapp.controllers;

import com.project.shopapp.dtos.OrderDetailDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("${api.prefix}/order_details")
public class OrderDetailController {
    @PostMapping
    public ResponseEntity<?> createOrderDetail(@Valid @RequestBody OrderDetailDTO newOrderDetail) {
        return ResponseEntity.ok().body("createOrderDetail here");
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderDetail(@PathVariable Long id) {
        return ResponseEntity.ok().body("getOrderDetail with id " + id);
    }

    //Lay ra danh sach cac order_Details cua 1 order nào đó
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getListOrderDetailByOrderId(@Min(1) @PathVariable Long orderId ) {
        return ResponseEntity.ok("getListOrderDetail with OrderId= " + orderId);

    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrderDetail(
            @Min(1) @PathVariable Long id, @Valid @RequestBody
            OrderDetailDTO newOrderDetailData
    ) {
        return ResponseEntity.ok().
                body("updateOrderDetail here =" + id + " " +
                        ",newOrderDetailData: " + newOrderDetailData);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteOrderDetail(@Valid @PathVariable Long id) {
        return ResponseEntity.ok().body("deleteOrderDetail with id " + id);
    }


}
