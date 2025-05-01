package com.project.shopapp.services.Impl;

import com.project.shopapp.dtos.OrderDTO;
import com.project.shopapp.exception.DataNotFoundException;
import com.project.shopapp.models.Order;
import com.project.shopapp.models.OrderStatus;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.OrderDetailRepository;
import com.project.shopapp.repositories.OrderRepository;
import com.project.shopapp.repositories.ProductRepository;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.responses.OrderResponse;
import com.project.shopapp.services.IOrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;

    @Override
    public OrderResponse createOrder(OrderDTO orderDTO, String vnpTxnRef) throws Exception {
        //Tim xem user 'id' co ton tai hay khong
        Optional<User> userOptional =  userRepository.findById(orderDTO.getUserId());
        User user = userOptional
                .orElseThrow(() -> new DataNotFoundException("Cannot find user with id: " + orderDTO.getUserId()));
        //Convert orderDTO => Order DB
        //Dung thu vien Model Mapper
        //Tao mot luong bang anh xa rieng de kiem soat viet anh xa
        modelMapper.typeMap(OrderDTO.class, Order.class)
                .addMappings(mapper -> mapper.skip(Order::setId));

        //Cap nhat cac truong cua don hang tu orderDTO sang order
        Order order = new Order();
        modelMapper.map(orderDTO, order);
        order.setUser(user);
        order.setOrderDate(LocalDate.now());
        order.setStatus(OrderStatus.PENDING);

        //Kiem tra shipping date >= ngay hom nay
        LocalDate shippingDate = orderDTO.getShippingDate() == null ? LocalDate.now() : orderDTO.getShippingDate();
        if(shippingDate.isBefore(LocalDate.now())) {
            throw new DataNotFoundException("Date must be at least today !");
        }
        order.setShippingDate(shippingDate);
        order.setActive(true);
        order.setTotalMoney(orderDTO.getTotalMoney());
        //Luu vnpTxtRef neu co
        if(orderDTO.getVnpTxtRef() != null){
            order.setVnpTxnRef(orderDTO.getVnpTxtRef());
        }
        if(orderDTO.getShippingAddress() == null) {
            order.setShippingAddress(orderDTO.getAddress());
        }
        order.setVnpTxnRef(vnpTxnRef);


        orderRepository.save(order);
        modelMapper.typeMap(Order.class, OrderResponse.class);
        return modelMapper.map(order, OrderResponse.class);
   }

    @Override
    public OrderResponse getOrderById(Long id) {
        Optional<Order> order = orderRepository.findById(id);
        if(order.isPresent()) {
            return modelMapper.map(order.get(), OrderResponse.class);
        } else {
            return null;
        }
    }

    @Override
    public OrderResponse updateOrder(Long id, OrderDTO orderDTO) {
        try {
            Order exisingOrder = orderRepository
                    .findById(id).orElseThrow(()
                            -> new DataNotFoundException("Cannot find order with id: " + id));
            User existingUser = userRepository
                    .findById(orderDTO.getUserId()).orElseThrow(()
                            -> new DataNotFoundException("Cannot find user with id: " + id));

            //Tao mot luong anh xa rieng de kiem soat viet anh xa
            modelMapper.typeMap(OrderDTO.class, Order.class)
                    .addMappings(
                            mapper -> mapper.skip(Order::setId));
            modelMapper.map(orderDTO, exisingOrder);
            exisingOrder.setUser(existingUser);
            orderRepository.save(exisingOrder);
            modelMapper.typeMap(Order.class, OrderResponse.class);
            return modelMapper.map(exisingOrder, OrderResponse.class);
        } catch (DataNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id).orElse(null);
        //no hard-delete => please soft-delete
        if(order != null) {
            order.setActive(false);
            orderRepository.save(order);
        }
    }

    @Override
    public List<OrderResponse> findAllOrderByUserId(Long userId) {
        return orderRepository.findByUserId(userId).stream().map(order -> modelMapper.map(order, OrderResponse.class)).collect(Collectors.toList());
    }


    @Override
    public OrderResponse updateOrderStatus(Long id, String status) {
        // Tìm đơn hàng theo ID
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + id));

        // Kiểm tra trạng thái hợp lệ
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }

        // Cập nhật và lưu
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);

        // Trả về DTO
        return modelMapper.map(updatedOrder, OrderResponse.class);
    }
}
