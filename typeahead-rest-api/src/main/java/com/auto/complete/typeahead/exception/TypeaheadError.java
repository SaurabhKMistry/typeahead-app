package com.auto.complete.typeahead.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class TypeaheadError {
	@Getter
	@NonNull
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timestamp;

	private int errorCode;

	@Getter
	@NonNull
	private HttpStatus httpCode;

	@Getter
	@NonNull
	private String errorMessage;

	public int getErrorCode() {
		return httpCode.value();
	}
}
