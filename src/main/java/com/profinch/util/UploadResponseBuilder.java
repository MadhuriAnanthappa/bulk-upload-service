package com.profinch.util;

import java.util.HashMap;
import java.util.Map;

import com.profinch.common.UploadStatus;
import com.profinch.dto.BulkApiResponse;

public class UploadResponseBuilder {
	public static BulkApiResponse<Map<String, Object>> buildUploadResponse(String reqRefNo, int totalRecords, String statusName) {
	    Map<String, Object> result = new HashMap<>();
	    result.put("reqRefNo", reqRefNo);
	    result.put("totalRecords", totalRecords);

	    if (UploadStatus.fromName(statusName).isPresent()) {
	        UploadStatus status = UploadStatus.fromName(statusName).get();
	        result.put("uploadStatus", status.name());
	        result.put("uploadMessage", status.getMessage());
	        return new BulkApiResponse<>("OK", "Request processed", result);
	    } else {
	        result.put("errMsg", statusName);
	        return new BulkApiResponse<>("ERROR", "Unexpected status", result);
	    }
	}
}
