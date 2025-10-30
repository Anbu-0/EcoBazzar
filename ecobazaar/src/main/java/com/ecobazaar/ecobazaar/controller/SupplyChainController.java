package com.ecobazaar.ecobazaar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.ecobazaar.ecobazaar.model.SupplyChainLog;
import com.ecobazaar.ecobazaar.service.SupplyChainService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/track")
public class SupplyChainController {

    private final SupplyChainService supplyChainService;

    @Autowired
    public SupplyChainController(SupplyChainService supplyChainService) {
        this.supplyChainService = supplyChainService;
    }

    // Create new handover log (Distributor, Retailer, Admin only)
    @PreAuthorize("hasAnyRole('DISTRIBUTOR','RETAILER','ADMIN')")
    @PostMapping("/update")
    public ResponseEntity<SupplyChainLog> updateChain(@RequestBody Map<String, Object> payload) {
        Long productId = Long.valueOf(payload.get("productId").toString());
        Long fromUserId = Long.valueOf(payload.get("fromUserId").toString());
        Long toUserId = Long.valueOf(payload.get("toUserId").toString());
        String location = payload.get("location").toString();
        String notes = payload.get("notes") != null ? payload.get("notes").toString() : "";

        SupplyChainLog savedLog = supplyChainService.addLog(productId, fromUserId, toUserId, location, notes);
        return ResponseEntity.ok(savedLog);
    }

    // Fetch full tracking history for one product
    @GetMapping("/{productId}")
    public ResponseEntity<List<SupplyChainLog>> getProductChain(@PathVariable Long productId) {
        List<SupplyChainLog> logs = supplyChainService.getLogsByProduct(productId);
        return ResponseEntity.ok(logs);
    }

    // Verify if chain intact (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/verify/{productId}")
    public ResponseEntity<Map<String, Object>> verifyChain(@PathVariable Long productId) {
        boolean valid = supplyChainService.verifyChain(productId);
        return ResponseEntity.ok(Map.of(
                "productId", productId,
                "isValid", valid
        ));
    }
}
