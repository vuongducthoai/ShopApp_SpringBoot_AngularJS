package com.project.shopapp.controllers;

import com.project.shopapp.dtos.OrderDTO;
import com.project.shopapp.responses.OrderResponse;
import com.project.shopapp.services.IVNPayService;
import com.project.shopapp.services.Impl.OrderServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderServiceImpl orderService;
    private final IVNPayService paymentService;
    @PostMapping("")
    //http://localhost:8088/api/v1/orders
    public ResponseEntity<?> createOrder (HttpServletRequest request,
            @Valid @RequestBody OrderDTO orderDTO, String vnpTxtRef,
                                          BindingResult result) {
        try {
            if(result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors().stream().map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errorMessages.toString());
            }
            //1. Tao don hang
            OrderResponse newOrderResponse = orderService.createOrder(orderDTO, vnpTxtRef);
            return ResponseEntity.ok(newOrderResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/{user_id}") // Thêm biến đường dẫn "user_id"
    //http://localhost:8088/api/v1/orders/user/4
    public ResponseEntity<?> getOrderUserById(@PathVariable("user_id") @Min(1) Long user_id) {
      try {
          List<OrderResponse> orders = orderService.findAllOrderByUserId(user_id);
          return ResponseEntity.ok().body(orders);
      } catch (Exception e) {
          return ResponseEntity.badRequest().body(e.getMessage());
      }
    }

    @GetMapping("/{id}") // Thêm biến đường dẫn "user_id"
    //http://localhost:8088/api/v1/orders/4
    public ResponseEntity<?> getOrdersById(@PathVariable("id") @Min(1) Long orderId) {
        try {
            OrderResponse existingOrder = orderService.getOrderById(orderId);
            return ResponseEntity.ok().body(existingOrder);
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
            OrderResponse order = orderService.updateOrder(id, orderDTO);
            return ResponseEntity.ok().body("updateOrder successfully: " + order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable @Min(1) Long id) {
        //xoa mem => cap truong truong active = false
        orderService.deleteOrder(id);
        return ResponseEntity.ok().body("deleteOrder successfully with: " + id);
    }
}
