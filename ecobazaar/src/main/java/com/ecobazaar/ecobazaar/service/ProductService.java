package com.ecobazaar.ecobazaar.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // ✅ use Spring’s version
import com.ecobazaar.ecobazaar.model.Product;
import com.ecobazaar.ecobazaar.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public Product saveProduct(Product product) {
        System.out.println("✅ [Service] Saving product: " + product.getCropName());
        Product saved = productRepository.save(product);
        System.out.println("✅ [Service] After save, ID = " + saved.getId());
        return saved;
    }

    public List<Product> getProductsByFarmerId(Long farmerId) {
        return productRepository.findByFarmerId(farmerId);
    }

    public Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }
}