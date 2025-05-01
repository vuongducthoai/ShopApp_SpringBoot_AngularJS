package com.project.shopapp.services.Impl;

import com.project.shopapp.dtos.OrderDetailDTO;
import com.project.shopapp.exception.DataNotFoundException;
import com.project.shopapp.models.Order;
import com.project.shopapp.models.OrderDetail;
import com.project.shopapp.models.Product;
import com.project.shopapp.repositories.OrderDetailRepository;
import com.project.shopapp.repositories.OrderRepository;
import com.project.shopapp.repositories.ProductRepository;
import com.project.shopapp.services.IOrderDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@RequiredArgsConstructor
@Service
public class OrderDetailService implements IOrderDetailService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderDetailRepository orderDetailRepository;

    @Override
    public OrderDetail createOrderDetail(OrderDetailDTO orderDetailDTO) {
        try {
            Order order = orderRepository.findById(orderDetailDTO.getOrderId()).orElseThrow(() -> new DataNotFoundException("Cannot find Order with id : " + orderDetailDTO.getOrderId()));
            Product product =  productRepository.findById(orderDetailDTO.getProductId()).orElseThrow(() -> new DataNotFoundException("Cannot find Product with id : " + orderDetailDTO.getProductId()));
            OrderDetail orderDetail = OrderDetail.builder()
                    .order(order)
                    .product(product)
                    .price(orderDetailDTO.getPrice())
                    .numberOfProducts(orderDetailDTO.getNumberOfProducts())
                    .color(orderDetailDTO.getColor())
                    .totalMoney(orderDetailDTO.getTotalMoney())
                    .build();
          return orderDetailRepository.save(orderDetail);
        } catch (DataNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public OrderDetail getOrderDetail(Long id) {
        try {
            return orderDetailRepository.findById(id).orElseThrow(() -> new DataNotFoundException("Cannot find OrderDetail with id: " + id));
        } catch (DataNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public OrderDetail updateOrderDetail(Long id, OrderDetailDTO orderDetailDTO) {
        //Tim xem order detail co ton tai hay khong
        try {
            OrderDetail existingOrderDetail = orderDetailRepository
                    .findById(id).orElseThrow(() ->
                            new DataNotFoundException("Cannot find OrderDetail with id: " + id));
            Order existingOrder = orderRepository
                    .findById(orderDetailDTO.getOrderId())
                    .orElseThrow(() -> new DataNotFoundException("Cannot find Order with id : " + id));
            Product existingProduct = productRepository
                    .findById(orderDetailDTO
                            .getProductId())
                    .orElseThrow(() -> new DataNotFoundException("Cannot find Product with id : " + orderDetailDTO.getProductId()));

            existingOrderDetail.setPrice(orderDetailDTO.getPrice());
            existingOrderDetail.setNumberOfProducts(orderDetailDTO.getNumberOfProducts());
            existingOrderDetail.setColor(orderDetailDTO.getColor());
            existingOrderDetail.setTotalMoney(orderDetailDTO.getTotalMoney());
            existingOrderDetail.setOrder(existingOrder);
            existingOrderDetail.setProduct(existingProduct);
            return orderDetailRepository.save(existingOrderDetail);


        } catch (DataNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteOrderDetail(Long id) {
        orderDetailRepository.deleteById(id);
    }

    @Override
    public List<OrderDetail> findByOrderId(Long orderId) {
        return orderDetailRepository.findByOrderId(orderId);
    }
}
