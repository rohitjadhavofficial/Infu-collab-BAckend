package com.example.influencer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import com.example.influencer.dto.InfluencerLoginRequest;
import com.example.influencer.dto.InfluencerOnboardingRequest;
import com.example.influencer.dto.InfluencerProfileResponse;
import com.example.influencer.dto.InfluencerResponseDTO;
import com.example.influencer.entity.InfluencerLogin;
import com.example.influencer.exception.DuplicateEmailException;
import com.example.influencer.repository.InfluencerLoginRepository;

@SpringBootTest
@Transactional
class InfluencerOnboardingServiceTest {

	@Autowired
	private InfluencerOnboardingService influencerOnboardingService;

	@Autowired
	private InfluencerLoginRepository influencerLoginRepository;

	@Autowired
	private InfluencerImageService influencerImageService;

	@Test
	void savePersistsInfluencerWhenEmailIsNew() {
		InfluencerOnboardingRequest request = sampleRequest("Abc@Gmail.com");

		influencerOnboardingService.save(request);

		InfluencerLogin saved = influencerLoginRepository.findById("abc@gmail.com").orElseThrow();
		assertThat(saved.getName()).isEqualTo("ss");
		assertThat(saved.getFollowers()).isEqualTo(10003L);
		assertThat(saved.getCategories()).containsExactly("Fashion & Lifestyle");
		assertThat(saved.getAverageCollaborationPriceInr()).isEqualByComparingTo("400");
	}

	@Test
	void saveRejectsDuplicateEmailIgnoringCase() {
		influencerOnboardingService.save(sampleRequest("abc@gmail.com"));

		assertThatThrownBy(() -> influencerOnboardingService.save(sampleRequest("ABC@gmail.com")))
				.isInstanceOf(DuplicateEmailException.class);
		assertThat(influencerLoginRepository.count()).isEqualTo(1);
	}

	@Test
	void loginReturnsProfileWhenEmailAndPasswordMatch() {
		influencerOnboardingService.save(sampleRequest("abc@gmail.com"));

		InfluencerLoginRequest loginRequest = new InfluencerLoginRequest();
		loginRequest.setEmail("ABC@gmail.com");
		loginRequest.setPassword("secret123");

		InfluencerProfileResponse profile = influencerOnboardingService.login(loginRequest).orElseThrow();
		assertThat(profile.getEmail()).isEqualTo("abc@gmail.com");
		assertThat(profile.getName()).isEqualTo("ss");
		assertThat(profile.getMobile()).isEqualTo("8208967904");
	}

	@Test
	void loginRejectsWrongPassword() {
		influencerOnboardingService.save(sampleRequest("abc@gmail.com"));

		InfluencerLoginRequest loginRequest = new InfluencerLoginRequest();
		loginRequest.setEmail("abc@gmail.com");
		loginRequest.setPassword("wrong");

		assertThat(influencerOnboardingService.login(loginRequest)).isEmpty();
	}

	@Test
	void listAllWithProfileImagesSelectsImageContainingUnderscoreOne() throws Exception {
		String email = "profile.user." + System.nanoTime() + "@example.com";
		influencerOnboardingService.save(sampleRequest(email));
		MockMultipartFile image = new MockMultipartFile("image", "profile.png", "image/png", pngBytes());
		influencerImageService.storeImage(email, image);
		influencerImageService.storeImage(email, image);

		InfluencerResponseDTO result = influencerOnboardingService.listAllWithProfileImages().stream()
				.filter(influencer -> email.equals(influencer.getEmail()))
				.findFirst()
				.orElseThrow();

		assertThat(result.getName()).isEqualTo("ss");
		assertThat(result.getLocation()).isEqualTo("pune");
		assertThat(result.getProfileImageUrl())
				.isEqualTo("http://localhost:8080/uploads/users/" + email + "/image_1.png");
		assertThat(result.getProfileImageUrl()).contains("_1.");
	}

	@Test
	void listAllWithProfileImagesReturnsNullWhenNoProfileImageExists() {
		influencerOnboardingService.save(sampleRequest("no.image@example.com"));

		InfluencerResponseDTO result = influencerOnboardingService.listAllWithProfileImages().stream()
				.filter(influencer -> "no.image@example.com".equals(influencer.getEmail()))
				.findFirst()
				.orElseThrow();

		assertThat(result.getEmail()).isEqualTo("no.image@example.com");
		assertThat(result.getProfileImageUrl()).isNull();
	}

	private byte[] pngBytes() throws IOException {
		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ImageIO.write(image, "png", output);
		return output.toByteArray();
	}

	private InfluencerOnboardingRequest sampleRequest(String email) {
		InfluencerOnboardingRequest request = new InfluencerOnboardingRequest();
		request.setName("ss");
		request.setFollowers(10003L);
		request.setCategories(List.of("Fashion & Lifestyle"));
		request.setAverageCollaborationPriceInr(new BigDecimal("400"));
		request.setLocation("pune");
		request.setEmail(email);
		request.setCountryCode("+91");
		request.setMobile("8208967904");
		request.setPassword("secret123");
		request.setSubmittedAt(Instant.parse("2026-10-10T14:01:18.650Z"));
		return request;
	}
}
