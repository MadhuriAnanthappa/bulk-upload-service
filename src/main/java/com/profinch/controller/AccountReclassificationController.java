package com.profinch.controller;

import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.profinch.dto.AccountReclassificationRequest;
import com.profinch.dto.BulkApiResponse;
import com.profinch.exception.TotalRecordsMismatchException;
import com.profinch.service.AccountReclassificationService;
import com.profinch.util.UploadResponseBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/accreclassification")
public class AccountReclassificationController {

	@Autowired
	private AccountReclassificationService accountReclassificationService;

	@PostMapping("/upload")
	public ResponseEntity<BulkApiResponse<Map<String, Object>>> bulkInsert(
			@Valid @RequestBody AccountReclassificationRequest requestDTO) {

		System.out.println("File Ref: " + requestDTO.getReqRefNo());
		System.out.println("Total Records: " + requestDTO.getTotalRecords());

		if (requestDTO.getTotalRecords() != requestDTO.getAccountReclassifications().size()) {
			throw new TotalRecordsMismatchException(requestDTO.getTotalRecords(),
					requestDTO.getAccountReclassifications().size());
		}

		String uploadStatus = (String) accountReclassificationService
				.processReclassification(requestDTO.getAccountReclassifications(), requestDTO.getReqRefNo());

		log.info("UploadStatus for ReqRefNo {}: {}", requestDTO.getReqRefNo(), uploadStatus);

		BulkApiResponse<Map<String, Object>> result = UploadResponseBuilder
				.buildUploadResponse(requestDTO.getReqRefNo(), requestDTO.getTotalRecords(), uploadStatus);

		log.debug("Response for ReqRefNo {}: {}", requestDTO.getReqRefNo(), result);

		return ResponseEntity.ok(result);

	}

	@GetMapping("/getStatus/{reqRefNo}")
	public ResponseEntity<BulkApiResponse<Map<String, Object>>> getStatus(@PathVariable String reqRefNo) {
		Map<String, Object> results = accountReclassificationService.getStatusByReqRefNo(reqRefNo);
		BulkApiResponse<Map<String, Object>> response = new BulkApiResponse<>("SUCCESS", "Status fetched successfully",
				results);

		return ResponseEntity.ok(response);

	}
}
