package com.project.shopapp.repositories;

import com.project.shopapp.models.Product;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByName(String name);
    Page<Product> findAll(Pageable pageable);  //Phân trang
    @Query("SELECT p FROM Product p WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
//            "LOWER(p.color) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.category.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchPhones(@Param("keyword") String keyword);

    @Query(value = "SELECT * FROM products LIMIT 3;", nativeQuery = true)
    List<Product> findAllProducts();
}
