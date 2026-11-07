package com.withademm.stores.repository;

// 1. Add this missing import
import com.withademm.stores.entity.Product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

}