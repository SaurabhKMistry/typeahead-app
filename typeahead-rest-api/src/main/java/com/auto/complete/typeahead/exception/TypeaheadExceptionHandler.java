package com.auto.complete.typeahead.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import static com.auto.complete.typeahead.TypeaheadPropertyKeys.*;
import static java.time.LocalDateTime.now;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;

@Slf4j
@ControllerAdvice
public class TypeaheadExceptionHandler {
	private Environment env;

	@Autowired
	public TypeaheadExceptionHandler(Environment env) {
		this.env = env;
	}

	@ExceptionHandler(value = {ConstraintViolationException.class})
	@ResponseBody
	public ResponseEntity<TypeaheadError> handleException(ConstraintViolationException e) {
		String errMsg = e.getConstraintViolations()
						 .stream()
						 .findFirst()
						 .map(ConstraintViolation::getMessage)
						 .orElseGet(() -> env.getProperty(INVALID_SUGGESTION_COUNT));
		return new ResponseEntity<>(new TypeaheadError(now(), BAD_REQUEST, errMsg), BAD_REQUEST);
	}

	@ExceptionHandler(value = {HttpMessageNotReadableException.class})
	@ResponseBody
	public ResponseEntity<TypeaheadError> handleException(HttpMessageNotReadableException e) {
		return new ResponseEntity<>(new TypeaheadError(now(),
													   BAD_REQUEST,
													   env.getProperty(MISSING_REQUIRED_REQ_BODY)),
									BAD_REQUEST);
	}

	@ExceptionHandler(value = {MethodArgumentNotValidException.class})
	@ResponseBody
	public ResponseEntity<TypeaheadError> handleException(MethodArgumentNotValidException e) {
		return new ResponseEntity<>(new TypeaheadError(now(),
													   BAD_REQUEST,
													   env.getProperty(MISSING_KEY_IN_REQUEST_BODY)),
									BAD_REQUEST);
	}

	@ExceptionHandler(value = {MissingServletRequestParameterException.class})
	@ResponseBody
	public ResponseEntity<TypeaheadError> handleException(MissingServletRequestParameterException e) {
		return new ResponseEntity<>(new TypeaheadError(now(),
													   BAD_REQUEST,
													   env.getProperty(PREFIX_QRY_PARAM_MISSING)),
									BAD_REQUEST);
	}

	@ExceptionHandler(value = {HttpMediaTypeNotSupportedException.class})
	@ResponseBody
	public ResponseEntity<TypeaheadError> handleException(HttpMediaTypeNotSupportedException e) {
		return new ResponseEntity<>(new TypeaheadError(now(),
													   UNSUPPORTED_MEDIA_TYPE,
													   e.getMessage()),
									UNSUPPORTED_MEDIA_TYPE);
	}
}
