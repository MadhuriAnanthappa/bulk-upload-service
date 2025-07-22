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

import com.profinch.dto.AmountUnblockRequest;
import com.profinch.dto.BulkApiResponse;
import com.profinch.exception.TotalRecordsMismatchException;
import com.profinch.service.AmountUnblockService;
import com.profinch.util.UploadResponseBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/amountUnblocks")
public class AmountUnblockController {
	@Autowired
	private AmountUnblockService amountUnblockService;

	@PostMapping("/upload")
	public ResponseEntity<?> bulkInsert(@Valid @RequestBody AmountUnblockRequest request) {
		if (request.getTotalRecords() != request.getAmountUnblocks().size()) {
			throw new TotalRecordsMismatchException(request.getTotalRecords(),
					request.getAmountUnblocks().size());
		}

		String uploadStatus =  (String) amountUnblockService.processUnblocks(request.getAmountUnblocks(),
				request.getReqRefNo());

		log.info("UploadStatus for ReqRefNo {}: {}", request.getReqRefNo(), uploadStatus);

		BulkApiResponse<Map<String, Object>> result = UploadResponseBuilder.buildUploadResponse(request.getReqRefNo(),
				request.getTotalRecords(), uploadStatus);

		log.debug("Response for ReqRefNo {}: {}", request.getReqRefNo(), result);
		
		return ResponseEntity.ok(result);
	}
	@GetMapping("/getStatus/{reqRefNo}")
	public ResponseEntity<BulkApiResponse<Map<String, Object>>> getStatus(@PathVariable String reqRefNo) {
		Map<String, Object> results = amountUnblockService.getStatusByReqRefNo(reqRefNo);
		BulkApiResponse<Map<String, Object>> response = new BulkApiResponse<>(
                "SUCCESS", "Status fetched successfully", results);

        return ResponseEntity.ok(response);

	}
}
