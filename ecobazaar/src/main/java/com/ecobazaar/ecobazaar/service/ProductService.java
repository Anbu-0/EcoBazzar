package com.ecobazaar.ecobazaar.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // ✅ use Spring’s version
import com.ecobazaar.ecobazaar.model.Product;
import com.ecobazaar.ecobazaar.repository.ProductRepository;
import com.ecobazaar.ecobazaar.repository.UserRepository;
import com.ecobazaar.ecobazaar.model.User;
import java.time.LocalDate;
import com.google.zxing.WriterException;
import com.ecobazaar.ecobazaar.util.QrCodeGenerator;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductService(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Product saveProduct(Product product) {
        System.out.println("✅ [Service] Saving product: " + product.getCropName());
        Product saved = productRepository.save(product);
        System.out.println("✅ [Service] After save, ID = " + saved.getId());
        return saved;
    }

    public List<Product> getProductsByFarmerId(Long farmerId) {
    	
    	User farmer = userRepository.findById(farmerId)
    			.orElseThrow(()->new RuntimeException("Farmer not found"));
        return productRepository.findByFarmerId(farmerId);
    }

    public Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }
    
    public List<Product> filterProduct(String cropName, LocalDate endDate){
    	return productRepository.filterProducts(cropName, endDate);
    }
    
    public String generateProductQr(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product Not Found"));

        String qrText = "https://farmchainx.com/products/" + product.getId();

        
        String qrPath = "uploads/qrcodes/product_" + productId + ".png";

        try {
            
            File qrFile = new File(qrPath);
            File parentDir = qrFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                boolean created = parentDir.mkdirs();
                if (!created) {
                    throw new RuntimeException("Failed to create directory: " + parentDir.getAbsolutePath());
                }
            }

            
            QrCodeGenerator.generateQR(qrText, qrPath);

           
            product.setQrCodePath(qrPath);
            productRepository.save(product);

            return qrPath;
        } catch (WriterException | IOException e) {
            throw new RuntimeException("Error generating QR: " + e.getMessage());
        }
    }
    
    public byte[] getProductQRImage(Long productId) {
    	Product product = productRepository.findById(productId)
    			.orElseThrow(()-> new RuntimeException("Product not found"));
    	
    	String qrPath = product.getQrCodePath();
    	
    	if(qrPath == null||qrPath.isEmpty()) {
    		throw new RuntimeException("QR code is not generated yet for this product");
    	}
    	
    	try {
    		Path path = Path.of(qrPath);
    		return Files.readAllBytes(path);
    	}catch(IOException e) {
    		throw new RuntimeException("Unable to read the QR code image :"+e.getMessage());
    	}
    	
    }

}