package com.project.shopapp.services;

import com.project.shopapp.dtos.OrderDTO;

import com.project.shopapp.models.Order;
import com.project.shopapp.responses.OrderResponse;
import java.util.List;

public interface IOrderService {
    OrderResponse createOrder(OrderDTO orderDTO, String vnpTxnRef) throws Exception;
    OrderResponse getOrderById(Long id);
    OrderResponse updateOrder(Long id, OrderDTO orderDTO);
    void deleteOrder(Long id);
    List<OrderResponse> findAllOrderByUserId(Long userId);
    OrderResponse updateOrderStatus(Long id, String status);
}
