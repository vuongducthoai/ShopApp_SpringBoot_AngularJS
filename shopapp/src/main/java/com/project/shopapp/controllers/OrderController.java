package com.project.shopapp.controllers;

import com.project.shopapp.dtos.OrderDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/orders")
public class OrderController {
    @PostMapping("")
    //http://localhost:8088/api/v1/orders
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderDTO orderDTO, BindingResult result) {
        try {
            if(result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors().stream().map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errorMessages.toString());
            }
            return ResponseEntity.ok().body("createOrder");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @GetMapping("/{user_id}") // Thêm biến đường dẫn "user_id"
    //http://localhost:8088/api/v1/orders/4
    public ResponseEntity<?> getOrders(@PathVariable("user_id") @Min(1) Long user_id) {
      try {
          return ResponseEntity.ok().body("Lấy ra danh sách order từ user_id: " + user_id);
      } catch (Exception e) {
          return ResponseEntity.badRequest().body(e.getMessage());
      }
    }

    @PutMapping("/{id}")//cong viec cua admin
    //PUT: http://localhost:8088/api/v1/orders/2
    public ResponseEntity<?> updateOrder(
            @Valid @RequestBody OrderDTO orderDTO,
             @PathVariable  @Min(1) Long id, BindingResult result) {
        try{
            if(result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors().stream().map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errorMessages.toString());
            }
            return ResponseEntity.ok().body("updateOrder successfully with: " + id);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable @Min(1) Long id) {
        //xoa mem => cap truong truong active = false
        return ResponseEntity.ok().body("deleteOrder successfully with: " + id);
    }
}
