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

import com.profinch.dto.AmountBlockRequest;
import com.profinch.dto.BulkApiResponse;
import com.profinch.exception.TotalRecordsMismatchException;
import com.profinch.service.AmountBlockService;
import com.profinch.util.UploadResponseBuilder;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/amountBlocks")
@Slf4j
public class AmountBlockController {

	@Autowired
	private AmountBlockService amountBlockService;

	@PostMapping("/upload")
	public ResponseEntity<BulkApiResponse<Map<String, Object>>> bulkInsert(@Valid @RequestBody AmountBlockRequest request) {
		 log.info("Bulk insert request initiated. ReqRefNo: {}, Total Records Claimed: {}", 
	                request.getReqRefNo(), request.getTotalRecords());
		if (request.getTotalRecords() != request.getAmountBlocks().size()) {
			log.error("Record count mismatch for ReqRefNo: {}. Claimed: {}, Actual: {}", 
                    request.getReqRefNo(), request.getTotalRecords(), request.getAmountBlocks().size());
			throw new TotalRecordsMismatchException(request.getTotalRecords(),
					request.getAmountBlocks().size());
		}
		   log.debug("Processing {} amount blocks for ReqRefNo: {}", 
                   request.getAmountBlocks().size(), request.getReqRefNo());
		String uploadStatus = (String) amountBlockService.processBlocks(request.getAmountBlocks(),
				request.getReqRefNo());

		log.info("UploadStatus for ReqRefNo {}: {}", request.getReqRefNo(), uploadStatus);

		BulkApiResponse<Map<String, Object>> result = UploadResponseBuilder.buildUploadResponse(request.getReqRefNo(),
				request.getTotalRecords(), uploadStatus);

		log.debug("Response for ReqRefNo {}: {}", request.getReqRefNo(), result);

		return ResponseEntity.ok(result);
	}

	@GetMapping("/getStatus/{reqRefNo}")
	public ResponseEntity<BulkApiResponse<Map<String, Object>>> getStatus(@PathVariable String reqRefNo) {
		log.info("Fetching upload status for reqRefNo={}", reqRefNo);
 		Map<String, Object> results = amountBlockService.getStatusByReqRefNo(reqRefNo);
 		BulkApiResponse<Map<String, Object>> response = new BulkApiResponse<>(
                "SUCCESS", "Status fetched successfully", results);

        return ResponseEntity.ok(response);
	}
}