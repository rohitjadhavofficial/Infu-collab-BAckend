package com.example.influencer.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.influencer.dto.BusinessLoginRequest;
import com.example.influencer.dto.BusinessOnboardingRequest;
import com.example.influencer.dto.BusinessProfileImageResponse;
import com.example.influencer.dto.MessageResponse;
import com.example.influencer.service.BusinessOnboardingService;
import com.example.influencer.service.BusinessProfileImageService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/business")
public class BusinessOnboardingController {

	private final BusinessOnboardingService businessOnboardingService;
	private final BusinessProfileImageService businessProfileImageService;

	public BusinessOnboardingController(BusinessOnboardingService businessOnboardingService,
			BusinessProfileImageService businessProfileImageService) {
		this.businessOnboardingService = businessOnboardingService;
		this.businessProfileImageService = businessProfileImageService;
	}

	@PostMapping(value = "/onboarding", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<MessageResponse> onboard(@Valid @RequestBody BusinessOnboardingRequest request) {
		businessOnboardingService.register(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Onboarding success"));
	}

	@PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> login(@RequestBody BusinessLoginRequest request) {
		return businessOnboardingService.login(request)
				.<ResponseEntity<?>>map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.ok("Login Failed"));
	}

	@PostMapping(value = "/upload-profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<BusinessProfileImageResponse> uploadProfileImage(
			@RequestParam("email") String email,
			@RequestParam("image") MultipartFile image) {
		return ResponseEntity.ok(businessProfileImageService.storeProfileImage(email, image));
	}

	@GetMapping(value = "/profile-image", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getProfileImage(@RequestParam("email") String email) {
		return ResponseEntity.ok(businessProfileImageService.getProfileImageUrl(email));
	}
}
