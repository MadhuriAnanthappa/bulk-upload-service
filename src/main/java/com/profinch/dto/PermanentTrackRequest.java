package com.profinch.dto;

import java.util.List;

import javax.validation.constraints.*;

import com.profinch.entity.PermanentTrack;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PermanentTrackRequest {
	@NotBlank(message = "reqRefNo is mandatory")
	private String reqRefNo;
	
	@NotNull(message = "totalRecords is required")
	private int totalRecords;
	
	@NotNull(message = "permanentTrack list is required")
	@Size(min = 1, message = "permanentTrack list must contain at least one record")
	private List<PermanentTrack> permanentTrack;
}
