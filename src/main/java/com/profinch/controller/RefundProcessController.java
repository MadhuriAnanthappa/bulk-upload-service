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

import com.profinch.dto.BulkApiResponse;
import com.profinch.dto.RefundProcessRequest;
import com.profinch.exception.TotalRecordsMismatchException;
import com.profinch.service.RefundProcessService;
import com.profinch.util.UploadResponseBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/refund")
public class RefundProcessController {

	@Autowired
	private RefundProcessService services;

	@PostMapping("/upload")
	public ResponseEntity<BulkApiResponse<Map<String, Object>>> saveRecords(
			@Valid @RequestBody RefundProcessRequest refund) {
		if (refund.getTotalRecords() != refund.getRefundProcesses().size()) {
			throw new TotalRecordsMismatchException(refund.getTotalRecords(), refund.getRefundProcesses().size());
		}
		String uploadStatus = services.processRefund(refund.getRefundProcesses(), refund.getReqRefNo());
		System.out.println(uploadStatus);
		log.info("UploadStatus for ReqRefNo {}: {}", refund.getReqRefNo(), uploadStatus);

		BulkApiResponse<Map<String, Object>> result = UploadResponseBuilder.buildUploadResponse(refund.getReqRefNo(),
				refund.getTotalRecords(), uploadStatus);

		log.debug("Response for ReqRefNo {}: {}", refund.getReqRefNo(), result);
		return ResponseEntity.ok(result);
	}

	@GetMapping("/getStatus/{reqRefNo}")
	public ResponseEntity<BulkApiResponse<Map<String, Object>>> getStatus(@PathVariable String reqRefNo) {
		log.info("Fetching upload status for reqRefNo={}", reqRefNo);
		Map<String, Object> results = services.getStatusByReqRefNo(reqRefNo);
		BulkApiResponse<Map<String, Object>> response = new BulkApiResponse<>("SUCCESS", "Status fetched successfully",
				results);

		return ResponseEntity.ok(response);

	}
}