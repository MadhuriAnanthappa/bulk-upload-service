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
import com.profinch.dto.BulkUdfDTORequest;
import com.profinch.exception.TotalRecordsMismatchException;
import com.profinch.service.BulkUdfService;
import com.profinch.util.UploadResponseBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/bulk-udf")
public class BulkUdfController {

	@Autowired
	private BulkUdfService bulkUdfService;

	@PostMapping("/upload")
	public ResponseEntity<BulkApiResponse<Map<String, Object>>> saveRecords(
			@Valid @RequestBody BulkUdfDTORequest bulkDto) {

		if (bulkDto.getTotalRecords() != bulkDto.getBulkUdfs().size()) {
			throw new TotalRecordsMismatchException(bulkDto.getTotalRecords(), bulkDto.getBulkUdfs().size());
		}

		log.info("Calling service to process bulkUdfService records for ReqRefNo: {}", bulkDto.getReqRefNo());
		String uploadStatus = (String) bulkUdfService.processUdf(bulkDto.getBulkUdfs(), bulkDto.getReqRefNo());

		log.info("UploadStatus for ReqRefNo {}: {}", bulkDto.getReqRefNo(), uploadStatus);

		BulkApiResponse<Map<String, Object>> result = UploadResponseBuilder.buildUploadResponse(bulkDto.getReqRefNo(),
				bulkDto.getTotalRecords(), uploadStatus);

		log.debug("Response for ReqRefNo {}: {}", bulkDto.getReqRefNo(), result);

		return ResponseEntity.ok(result);

	}

	@GetMapping("/getStatus/{reqRefNo}")
	public ResponseEntity<BulkApiResponse<Map<String, Object>>> getStatusByReqRefNo(@PathVariable String reqRefNo) {
		Map<String, Object> result = bulkUdfService.getStatusByReqRefNo(reqRefNo);
		BulkApiResponse<Map<String, Object>> response = new BulkApiResponse<>("SUCCESS", "Status fetched successfully",
				result);

		return ResponseEntity.ok(response);
	}
}
