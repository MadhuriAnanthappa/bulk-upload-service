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
import com.profinch.dto.PermanentTrackRequest;
import com.profinch.exception.TotalRecordsMismatchException;
import com.profinch.service.PermanentTrackService;
import com.profinch.util.UploadResponseBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/permanentTrack")
public class PermanentTrackController {

	@Autowired
	private PermanentTrackService permanentTrackService;

	@PostMapping("/upload")
	public ResponseEntity<BulkApiResponse<Map<String, Object>>> permanentTrackInsert(@Valid @RequestBody PermanentTrackRequest request) {
		log.info("Received permanentTrack upload request. ReqRefNo: {}, TotalRecords: {}", request.getReqRefNo(),
				request.getTotalRecords());

		if (request.getTotalRecords() != request.getPermanentTrack().size()) {
			log.warn("Validation failed - totalRecords ({}) does not match actual records ({})",
					request.getTotalRecords(), request.getPermanentTrack().size());

			throw new TotalRecordsMismatchException(request.getTotalRecords(), request.getPermanentTrack().size());
		}
		log.info("Calling service to process permanentTrack records for ReqRefNo: {}", request.getReqRefNo());
		String uploadStatus = (String) permanentTrackService.processPermanentTracks(request.getPermanentTrack(),
				request.getReqRefNo());

		log.info("UploadStatus for ReqRefNo {}: {}", request.getReqRefNo(), uploadStatus);

		BulkApiResponse<Map<String, Object>> response = UploadResponseBuilder.buildUploadResponse(request.getReqRefNo(),
				request.getTotalRecords(), uploadStatus);

		log.debug("Response for ReqRefNo {}: {}", request.getReqRefNo(), response);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/getStatus/{reqRefNo}")
	public ResponseEntity<BulkApiResponse<Map<String, Object>>> getStatus(@PathVariable String reqRefNo) {
		log.info("Fetching upload status for reqRefNo={}", reqRefNo);
		Map<String, Object> results = permanentTrackService.getStatusByReqRefNo(reqRefNo);

		BulkApiResponse<Map<String, Object>> response = new BulkApiResponse<>(
                "SUCCESS", "Status fetched successfully", results);

        return ResponseEntity.ok(response);

	}
}
