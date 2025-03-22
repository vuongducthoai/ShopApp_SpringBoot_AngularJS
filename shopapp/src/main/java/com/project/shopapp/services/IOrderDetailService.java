package com.project.shopapp.services;

import com.project.shopapp.dtos.OrderDetailDTO;
import com.project.shopapp.models.OrderDetail;
import java.util.List;

public interface IOrderDetailService {
    OrderDetail createOrderDetail(OrderDetailDTO newOrderDetail);
    OrderDetail getOrderDetail(Long id);
    OrderDetail updateOrderDetail(Long id, OrderDetailDTO newOrderDetail);
    void deleteOrderDetail(Long id);
    List<OrderDetail> findByOrderId(Long orderId);
}
