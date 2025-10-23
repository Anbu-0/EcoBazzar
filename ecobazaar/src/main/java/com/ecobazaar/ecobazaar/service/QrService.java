package com.ecobazaar.ecobazaar.service;

import java.io.File;
import java.io.IOException;

import org.springframework.stereotype.Service;

import com.ecobazaar.ecobazaar.model.Product;
import com.ecobazaar.ecobazaar.repository.ProductRepository;
import com.ecobazaar.ecobazaar.util.QrCodeGenerator;
import com.google.zxing.WriterException;

@Service
public class QrService {

    private final ProductRepository productRepository;

    public QrService(ProductRepository productRepository) {
        this.productRepository = productRepository;
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
}
