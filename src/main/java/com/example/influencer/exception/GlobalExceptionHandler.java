package com.example.influencer.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import com.example.influencer.dto.ApiResponse;
import com.example.influencer.dto.ChatApiResponse;
import com.example.influencer.dto.ImageUploadResponse;
import com.example.influencer.dto.MessageResponse;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(BusinessEmailExistsException.class)
	public ResponseEntity<MessageResponse> handleBusinessEmailExists(BusinessEmailExistsException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponse("EMAIL EXISTS"));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<MessageResponse> handleValidation(MethodArgumentNotValidException ex) {
		String message = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.collect(Collectors.joining("; "));
		if (message.isBlank()) {
			message = "Invalid request";
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(message));
	}

	@ExceptionHandler(ChatException.class)
	public ResponseEntity<ChatApiResponse<Void>> handleChat(ChatException ex) {
		return ResponseEntity.status(ex.getStatus()).body(ChatApiResponse.failure(ex.getMessage()));
	}

	@ExceptionHandler(ImageUploadException.class)
	public ResponseEntity<ImageUploadResponse> handleImageUpload(ImageUploadException ex) {
		return ResponseEntity.status(ex.getStatus()).body(ImageUploadResponse.failure(ex.getMessage()));
	}

	@ExceptionHandler({ MaxUploadSizeExceededException.class })
	public ResponseEntity<ImageUploadResponse> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(ImageUploadResponse.failure("Image file exceeds the maximum allowed size"));
	}

	@ExceptionHandler({ MissingServletRequestPartException.class, MultipartException.class })
	public ResponseEntity<ImageUploadResponse> handleMissingMultipart(Exception ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(ImageUploadResponse.failure("Image is required"));
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<?> handleMissingParameter(MissingServletRequestParameterException ex) {
		String parameterName = ex.getParameterName();
		if ("image".equalsIgnoreCase(parameterName) || "email".equalsIgnoreCase(parameterName)) {
			String message = "email".equalsIgnoreCase(parameterName) ? "Email is required" : "Image is required";
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ImageUploadResponse.failure(message));
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(parameterName + " is required"));
	}

	@ExceptionHandler(DuplicateEmailException.class)
	public ResponseEntity<ApiResponse> handleDuplicateEmail(DuplicateEmailException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error("Email duplicate"));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<?> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
		if (isBusinessEmailConstraint(ex)) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponse("EMAIL EXISTS"));
		}
		log.warn("Rejected save due to a data integrity constraint");
		return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error("Email duplicate"));
	}

	@ExceptionHandler({ DataAccessException.class, RuntimeException.class })
	public ResponseEntity<ApiResponse> handlePersistenceFailure(RuntimeException ex) {
		log.error("Unable to save influencer data", ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponse.error("Unable to save influencer data"));
	}

	private boolean isBusinessEmailConstraint(DataIntegrityViolationException ex) {
		String details = String.valueOf(ex.getMostSpecificCause().getMessage());
		return details.toLowerCase().contains("uk_business_login_email")
				|| details.toLowerCase().contains("business_login");
	}
}
