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

    @PreAuthorize("hasAnyRole('DISTRIBUTOR','RETAILER')")
    @PostMapping("/update-chain")
    public ResponseEntity<?> updateChain(@RequestBody Map<String, Object> payload, Principal principal) {
        try {
            Long productId = Long.valueOf(payload.get("productId").toString());
            String location = payload.get("location").toString();
            String notes = payload.get("notes") != null ? payload.get("notes").toString() : "";

            String email = principal.getName();
            User fromUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found: " + email));

            SupplyChainLog log;

            // ✅ Distributor Logic
            if (fromUser.hasRole("ROLE_DISTRIBUTOR")) {
                if (payload.get("toUserId") == null)
                    throw new RuntimeException("toUserId is required for distributor updates");
                Long toUserId = Long.valueOf(payload.get("toUserId").toString());
                log = supplyChainService.addLog(productId, fromUser.getId(), toUserId, location, notes);
            }
            // ✅ Retailer Logic
            else if (fromUser.hasRole("ROLE_RETAILER")) {
                log = supplyChainService.confirmReceipt(productId, fromUser.getId(), location, notes);
            }
            // 🚫 Unauthorized role
            else {
                throw new RuntimeException("Unauthorized role for this action");
            }

            return ResponseEntity.ok(log);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','DISTRIBUTOR','RETAILER')")
    @GetMapping("/{productId}")
    public ResponseEntity<List<SupplyChainLog>> getProductChain(@PathVariable Long productId) {
        return ResponseEntity.ok(supplyChainService.getLogsByProduct(productId));
    }
    
    @PreAuthorize("hasRole('RETAILER')")
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingForRetailer(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<SupplyChainLog> pending = supplyChainService.getPendingConfirmations(user.getId());
        return ResponseEntity.ok(pending);
    }

}