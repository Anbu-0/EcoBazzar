package com.ecobazaar.ecobazaar.controller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.security.Principal;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;

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
    
    // 🧑‍🌾 Only Farmers can upload products
    @PreAuthorize("hasRole('FARMER')")
    @PostMapping("/upload")
    public Product uploadProduct(
            @RequestParam String cropName,
            @RequestParam String soilType,
            @RequestParam String pesticides,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate harvestDate,
            @RequestParam String gpsLocation,
            @RequestParam("image") MultipartFile imageFile,
            Principal principal
    ) throws IOException {

        System.out.println("🔥 [Controller] Entered /upload endpoint");

        if (imageFile.isEmpty()) {
            throw new RuntimeException("Image is required");
        }
        
        //Save image to upload directory
        String uploadDir = System.getProperty("user.dir") + File.separator + "uploads";

        File folder = new File(uploadDir);
        if (!folder.exists()) folder.mkdirs();

        String imagePath = uploadDir + System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
        imageFile.transferTo(new File(imagePath));
        

     // 🧩 Get currently logged-in user from JWT token
        String email = principal.getName(); // Extracted from token
        User farmer = userRepository.findByEmail(email)
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
 
    @PreAuthorize("hasAnyRole('FARMER','ADMIN')")
    @PostMapping("/{id}/qrcode")
    public String generateProductQrCode(@PathVariable Long id, Principal principal) {
        String email = principal.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productService.getProductById(id);

        boolean isFarmer = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase("ROLE_FARMER"));

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase("ROLE_ADMIN"));

        // 👨‍🌾 FARMER: can only generate QR for their own products
        if (isFarmer && product.getFarmer().getId() != currentUser.getId()) {
            throw new RuntimeException("Access Denied: You can only generate QR for your own products");
        }

        // 🧑‍💼 ADMIN: can generate QR for any product
        if (isAdmin || isFarmer) {
            return productService.generateProductQr(id);
        }

        throw new RuntimeException("Access Denied: Unauthorized role");
    }



    // 🌍 Public — anyone can download product QR
    @GetMapping("/{id}/qrcode/download")
    public ResponseEntity<byte[]> downloadProductQR(@PathVariable Long id) {
        byte[] imageBytes = productService.getProductQRImage(id);
        return ResponseEntity.ok()
                .header("Content-Type", "image/png")
                .header("Content-Disposition", "attachment; filename=product_" + id + ".png")
                .body(imageBytes);
    }

}