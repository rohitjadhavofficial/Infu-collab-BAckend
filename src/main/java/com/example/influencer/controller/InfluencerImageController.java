package com.example.influencer.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.influencer.dto.ImageUploadResponse;
import com.example.influencer.service.InfluencerImageService;

@RestController
@RequestMapping("/api/influencers")
public class InfluencerImageController {

	private final InfluencerImageService influencerImageService;

	public InfluencerImageController(InfluencerImageService influencerImageService) {
		this.influencerImageService = influencerImageService;
	}

	@PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ImageUploadResponse> uploadImage(
			@RequestParam("email") String email,
			@RequestParam("image") MultipartFile image) {
		ImageUploadResponse response = influencerImageService.storeImage(email, image);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/images")
	public ResponseEntity<List<String>> getImages(@RequestParam("email") String email) {
		System.out.println(influencerImageService.listImageUrls(email));
		return ResponseEntity.ok(influencerImageService.listImageUrls(email));
	}
}
