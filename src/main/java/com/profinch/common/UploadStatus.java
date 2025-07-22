package com.profinch.common;

import java.util.Optional;

public enum UploadStatus {
	SUCCESS("S", "Upload Successful, Processing started.."), FAILED("F", "Upload Failed."), DUPLICATE("D","Duplicate upload");

	private final String code;
	private final String message;

	UploadStatus(String code, String message) {
		this.code = code;
		this.message = message;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public static Optional<UploadStatus> fromName(String name) {
		try {
			return Optional.of(UploadStatus.valueOf(name));
		} catch (Exception e) {
			return Optional.empty();
		}
	}
}
