package com.ecobazaar.ecobazaar.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.ecobazaar.ecobazaar.model.SupplyChainLog;
import com.ecobazaar.ecobazaar.model.User;
import com.ecobazaar.ecobazaar.repository.UserRepository;
import com.ecobazaar.ecobazaar.service.SupplyChainService;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/track")
public class SupplyChainController {

    private final SupplyChainService supplyChainService;
    private final UserRepository userRepository;

    public SupplyChainController(SupplyChainService supplyChainService, UserRepository userRepository) {
        this.supplyChainService = supplyChainService;
        this.userRepository = userRepository;
    }

    // Create new handover log (Distributor, Retailer, Admin only)
    @PreAuthorize("hasAnyRole('DISTRIBUTER','RETAILER','ADMIN')")
    @PostMapping("/update-chain")
    public ResponseEntity<?> updateChain(@RequestBody Map<String, Object> payload, Principal principal) {
        System.out.println("🟢 [TRACK] update-chain called with payload: " + payload);

        try {
            Long productId = Long.valueOf(payload.get("productId").toString());
            Long toUserId = Long.valueOf(payload.get("toUserId").toString());
            String location = payload.get("location").toString();
            String notes = payload.get("notes") != null ? payload.get("notes").toString() : "";

            String email = principal.getName();
            User fromUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found: " + email));

            System.out.println("🟣 [TRACK] FromUser: " + fromUser.getId() + " → ToUser: " + toUserId);

            SupplyChainLog log = supplyChainService.addLog(
                    productId, fromUser.getId(), toUserId, location, notes
            );

            System.out.println("✅ [TRACK] Log saved: " + log.getId());
            return ResponseEntity.ok(log);

        } catch (Exception e) {
            System.out.println("❌ [TRACK] Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{productId}")
    public ResponseEntity<List<SupplyChainLog>> getProductChain(@PathVariable Long productId) {
        return ResponseEntity.ok(supplyChainService.getLogsByProduct(productId));
    }
}