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

    // Add a new supply chain log entry
    public SupplyChainLog addLog(Long productId, Long fromUserId, Long toUserId,
                                 String location, String notes) {

        // 1️ Find last log to get previous hash
        Optional<SupplyChainLog> lastLogOpt = supplyChainLogRepository.findTopByProductIdOrderByTimestampDesc(productId);
        String prevHash = lastLogOpt.map(SupplyChainLog::getHash).orElse("");

        // 2️ Create new log
        SupplyChainLog newLog = new SupplyChainLog();
        newLog.setProductId(productId);
        newLog.setFromUserId(fromUserId);
        newLog.setToUserId(toUserId);
        newLog.setLocation(location);
        newLog.setNotes(notes);
        newLog.setTimestamp(LocalDateTime.now());
        newLog.setPrevHash(prevHash);

        // 3️ Compute new hash
        String hash = HashUtil.computeHash(newLog, prevHash);
        newLog.setHash(hash);

        // 4️ Save to DB
        return supplyChainLogRepository.save(newLog);
    }

    // Get all logs for a product
    public List<SupplyChainLog> getLogsByProduct(Long productId) {
        return supplyChainLogRepository.findByProductIdOrderByTimestampAsc(productId);
    }

    // Verify integrity of product chain
    public boolean verifyChain(Long productId) {
        List<SupplyChainLog> logs = getLogsByProduct(productId);

        String prevHash = "";
        for (SupplyChainLog log : logs) {
            String recomputed = HashUtil.computeHash(log, prevHash);
            if (!recomputed.equals(log.getHash())) {
                return false; // tampered
            }
            prevHash = log.getHash();
        }
        return true;
    }
}