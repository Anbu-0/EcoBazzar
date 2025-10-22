package com.ecobazaar.ecobazaar.controller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.ecobazaar.ecobazaar.model.Product;
import com.ecobazaar.ecobazaar.model.User;
import com.ecobazaar.ecobazaar.repository.UserRepository;
import com.ecobazaar.ecobazaar.service.ProductService;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final UserRepository userRepository;

    public ProductController(ProductService productService, UserRepository userRepository) {
        this.productService = productService;
        this.userRepository = userRepository;
    }

  
    @PostMapping("/upload")
    public Product uploadProduct(
            @RequestParam String cropName,
            @RequestParam String soilType,
            @RequestParam String pesticides,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate harvestDate,
            @RequestParam String gpsLocation,
            @RequestParam Long farmerId,
            @RequestParam("image") MultipartFile imageFile
    ) throws IOException {

        System.out.println("🔥 [Controller] Entered /upload endpoint");

        if (imageFile.isEmpty()) {
            throw new RuntimeException("Image is required");
        }

        String uploadDir = System.getProperty("user.dir") + File.separator + "uploads";

        File folder = new File(uploadDir);
        if (!folder.exists()) folder.mkdirs();

        String imagePath = uploadDir + System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
        imageFile.transferTo(new File(imagePath));

        User farmer = userRepository.findById(farmerId)
                .orElseThrow(() -> new RuntimeException("Farmer not found"));
        System.out.println("🔥 [Controller] Farmer found: " + farmer.getEmail());

        Product product = new Product();
        product.setCropName(cropName);
        product.setSoilType(soilType);
        product.setPesticides(pesticides);
        product.setHarvestDate(harvestDate);
        product.setGpsLocation(gpsLocation);
        product.setImagePath(imagePath);
        product.setQualityGrade("Pending");
        product.setConfidenceScore(0.0);
        product.setFarmer(farmer);

        Product saved = productService.saveProduct(product);

        System.out.println("🔥 [Controller] Product saved with ID: " + saved.getId());
        return saved;
    }
 
    @GetMapping("/farmer/{farmerId}")
    public List<Product> getProductsByFarmer(@PathVariable Long farmerId) {
        return productService.getProductsByFarmerId(farmerId);
    }


    @GetMapping("/{productId}")
    public Product getProductById(@PathVariable Long productId) {
        return productService.getProductById(productId); 
    }
    
    @GetMapping("/filter")
    public List<Product> filterProducts(
            @RequestParam(required = false) String cropName,
            @RequestParam(required = false) LocalDate endDate
    ) {
        return productService.filterProduct(cropName, endDate);
    }

}