package com.withademm.stores.repository;

import com.withademm.stores.entity.Product;
import com.withademm.stores.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    // 1. Used by Public Portal & Manager Queue: Fetch products by approval status
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    // 2. Used by Sellers: Fetch products created by a specific seller
    Page<Product> findByCreatedByEmail(String email, Pageable pageable);
}