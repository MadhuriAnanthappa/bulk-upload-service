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
import com.profinch.entity.AccountReclassification;
import com.profinch.repository.AccountReclassificationRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AccountReclassificationService {
	@Value("${app.userid}")
	private String userId;

	@Autowired
	private AccountReclassificationRepository accountReclassificationRepository;

	@Autowired
	private ThreadPoolTaskExecutor taskExecutor;

	private static final int BATCH_SIZE = 10000;

	public String processReclassification(List<AccountReclassification> accountReclassifications, String reqRefNo) {
		Instant start = Instant.now();

		if (accountReclassificationRepository.existsByReqRefNo(reqRefNo)) {
			log.warn("Request reference number {} already exists. Skipping processing.", reqRefNo);
			return UploadStatus.DUPLICATE.name();
		}
		List<CompletableFuture<UploadStatus>> futures = new ArrayList<>();

		for (int i = 0; i < accountReclassifications.size(); i += BATCH_SIZE) {
			int end = Math.min(i + BATCH_SIZE, accountReclassifications.size());
			List<AccountReclassification> batch = new ArrayList<>(accountReclassifications.subList(i, end));

			CompletableFuture<UploadStatus> future = CompletableFuture.supplyAsync(
					() -> accountReclassificationRepository.saveAccountReclassifications(batch, reqRefNo),
					taskExecutor);

			futures.add(future);
		}

		CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

		try {
			allFutures.get(); // Wait for all inserts to complete
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return "F";
		}

		boolean allSuccessful = futures.stream().allMatch(f -> {
			try {
				return UploadStatus.SUCCESS.equals(f.get());
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		});

		CompletableFuture.runAsync(() -> {
			try {
				accountReclassificationRepository.executeValidationJob(reqRefNo, userId);
			} catch (Exception e) {
				log.error("Procedure call executeReclassificationJob failed for reqRefNo {}: {}", reqRefNo,
						e.getMessage(), e);
			}
		}, taskExecutor);

		Instant end = Instant.now();
		log.debug("All batches done in " + Duration.between(start, end).toMillis() + " ms");

		return allSuccessful ? UploadStatus.SUCCESS.name() : UploadStatus.FAILED.name();
	}

	public Map<String, Object> getStatusByReqRefNo(String fileRefNo) {
		return accountReclassificationRepository.getStatusByReqRefNo(fileRefNo);
	}
}
