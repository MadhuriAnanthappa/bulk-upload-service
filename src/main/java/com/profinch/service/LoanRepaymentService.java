package com.profinch.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.profinch.common.UploadStatus;
import com.profinch.entity.LoanRepayment;
import com.profinch.repository.LoanRepaymentRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LoanRepaymentService {

    @Value("${app.userid}")
    private String userId;

    @Autowired
    private LoanRepaymentRepository loanRepaymentRepository;
    
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    private static final int BATCH_SIZE = 10000;

    public String processRepayments(List<LoanRepayment> loanRepayments, String reqRefNo, String fileName) {
        Instant start = Instant.now();

        if (loanRepaymentRepository.existsByReqRefNo(reqRefNo)) {
            log.warn("Request reference number {} already exists. Skipping processing.", reqRefNo);
            return UploadStatus.DUPLICATE.name();
        }

        List<CompletableFuture<UploadStatus>> futures = new ArrayList<>();

        for (int i = 0; i < loanRepayments.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, loanRepayments.size());
            List<LoanRepayment> batch = new ArrayList<>(loanRepayments.subList(i, end));

            CompletableFuture<UploadStatus> future = CompletableFuture
                .supplyAsync(() -> loanRepaymentRepository.saveRepayments(batch, reqRefNo,fileName), taskExecutor);

            futures.add(future);
        }

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        try {
            allFutures.get(); 
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("Error processing batches for reqRefNo {}: {}", reqRefNo, e.getMessage(), e);
            return UploadStatus.FAILED.name();
        }

        boolean allSuccessful = futures.stream().allMatch(f -> {
            try {
                return UploadStatus.SUCCESS.equals(f.get());
            } catch (Exception e) {
                log.error("Batch processing failed for reqRefNo {}: {}", reqRefNo, e.getMessage(), e);
                return false;
            }
        });

        CompletableFuture.runAsync(() -> {
            try {
                loanRepaymentRepository.executeLoanRepayment(reqRefNo, userId);
            } catch (Exception e) {
                log.error("Procedure call executeLoanRepayment failed for reqRefNo {}: {}", reqRefNo, e.getMessage(), e);
            }
        }, taskExecutor);

        Instant end = Instant.now();
        log.debug("All batches processed for reqRefNo {} in {} ms", 
                 reqRefNo, Duration.between(start, end).toMillis());

        return allSuccessful ? UploadStatus.SUCCESS.name() : UploadStatus.FAILED.name();
    }

    public Map<String, Object> getStatusByReqRefNo(String reqRefNo) {
        return loanRepaymentRepository.getStatusByReqRefNo(reqRefNo);
    }
}