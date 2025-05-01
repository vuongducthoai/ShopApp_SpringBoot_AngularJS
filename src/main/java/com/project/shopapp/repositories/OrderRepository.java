package com.project.shopapp.repositories;

import com.project.shopapp.models.Order;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    //Tim cac don hang cua 1 user nao do
    List<Order> findByUserId(Long userId);

    Optional<Order> findByVnpTxnRef(String vnpTxnRef);

    @Modifying
    @Transactional
    @Query("UPDATE Order o SET o.status = :status WHERE o.vnpTxnRef = :vnpTxnRef")
    int updateStatusByVnpTxnRef(String status, String vnpTxnRef);
}
