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
import com.profinch.dto.CustomerCatModRequest;
import com.profinch.exception.TotalRecordsMismatchException;
import com.profinch.service.CustomerCatModService;
import com.profinch.util.UploadResponseBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/customerCatMod")
public class CustomerCatModController {

	@Autowired
	private CustomerCatModService customerCatModService;

	@PostMapping("/upload")
	public ResponseEntity<BulkApiResponse<Map<String, Object>>> bulkInsert(@Valid @RequestBody CustomerCatModRequest request) {
		if (request.getTotalRecords() != request.getCustomerCatMods().size()) {
			throw new TotalRecordsMismatchException(request.getTotalRecords(), request.getCustomerCatMods().size());
		}

		String uploadStatus = (String) customerCatModService.processblocks(request.getCustomerCatMods(),
				request.getReqRefNo());

		log.info("UploadStatus for ReqRefNo {}: {}", request.getReqRefNo(), uploadStatus);

		BulkApiResponse<Map<String, Object>> result = UploadResponseBuilder.buildUploadResponse(request.getReqRefNo(),
				request.getTotalRecords(), uploadStatus);

		log.debug("Response for ReqRefNo {}: {}", request.getReqRefNo(), result);

		return ResponseEntity.ok(result);
	}

	@GetMapping("/getStatus/{reqRefNo}")
	public ResponseEntity<BulkApiResponse<Map<String, Object>>> getStatus(@PathVariable String reqRefNo) {
		Map<String, Object> results = customerCatModService.getStatusByReqRefNo(reqRefNo);
		
		BulkApiResponse<Map<String, Object>> response = new BulkApiResponse<>(
                "SUCCESS", "Status fetched successfully", results);

        return ResponseEntity.ok(response);

	}

}
