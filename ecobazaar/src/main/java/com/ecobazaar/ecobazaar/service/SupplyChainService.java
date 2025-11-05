package com.ecobazaar.ecobazaar.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import com.ecobazaar.ecobazaar.model.SupplyChainLog;
import com.ecobazaar.ecobazaar.repository.SupplyChainLogRepository;
import com.ecobazaar.ecobazaar.util.HashUtil;

@Service
public class SupplyChainService {

    private final SupplyChainLogRepository supplyChainLogRepository;

    public SupplyChainService(SupplyChainLogRepository supplyChainLogRepository) {
        this.supplyChainLogRepository = supplyChainLogRepository;
    }

    // Distributor or any authorized sender adds new tracking log
    public SupplyChainLog addLog(Long productId, Long fromUserId, Long toUserId,
                                 String location, String notes) {
        Optional<SupplyChainLog> lastLogOpt = supplyChainLogRepository.findTopByProductIdOrderByTimestampDesc(productId);
        String prevHash = lastLogOpt.map(SupplyChainLog::getHash).orElse("");

        SupplyChainLog newLog = new SupplyChainLog();
        newLog.setProductId(productId);
        newLog.setFromUserId(fromUserId);
        newLog.setToUserId(toUserId);
        newLog.setLocation(location);
        newLog.setNotes(notes);
        newLog.setTimestamp(LocalDateTime.now());
        newLog.setPrevHash(prevHash);
        newLog.setHash(HashUtil.computeHash(newLog, prevHash));

        return supplyChainLogRepository.save(newLog);
    }

    // Retailer confirms receipt ONLY if product was sent to them
    public SupplyChainLog confirmReceipt(Long productId, Long retailerId,
                                         String location, String notes) {
        Optional<SupplyChainLog> lastLogOpt = supplyChainLogRepository.findTopByProductIdOrderByTimestampDesc(productId);

        if (lastLogOpt.isEmpty()) {
            throw new RuntimeException("No previous tracking record found for this product.");
        }

        SupplyChainLog lastLog = lastLogOpt.get();

        // ✅ Ensure this product was sent to this retailer
        if (!lastLog.getToUserId().equals(retailerId)) {
            throw new RuntimeException("This product is not assigned to you. Only assigned retailer can confirm receipt.");
        }

        String prevHash = lastLog.getHash();

        SupplyChainLog receiptLog = new SupplyChainLog();
        receiptLog.setProductId(productId);
        receiptLog.setFromUserId(lastLog.getFromUserId());
        receiptLog.setToUserId(retailerId);
        receiptLog.setLocation(location);
        receiptLog.setNotes(notes.isEmpty() ? "Product received by retailer" : notes);
        receiptLog.setTimestamp(LocalDateTime.now());
        receiptLog.setPrevHash(prevHash);
        receiptLog.setHash(HashUtil.computeHash(receiptLog, prevHash));

        return supplyChainLogRepository.save(receiptLog);
    }

    public List<SupplyChainLog> getLogsByProduct(Long productId) {
        return supplyChainLogRepository.findByProductIdOrderByTimestampAsc(productId);
    }

    public boolean verifyChain(Long productId) {
        List<SupplyChainLog> logs = getLogsByProduct(productId);
        String prevHash = "";
        for (SupplyChainLog log : logs) {
            String recomputed = HashUtil.computeHash(log, prevHash);
            if (!recomputed.equals(log.getHash())) return false;
            prevHash = log.getHash();
        }
        return true;
    }
    
    public List<SupplyChainLog> getPendingConfirmations(Long retailerId) {
        return supplyChainLogRepository.findPendingForRetailer(retailerId);
    }

}