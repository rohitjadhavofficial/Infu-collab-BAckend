package com.example.influencer.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.Optional;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import com.example.influencer.dto.BusinessLoginRequest;
import com.example.influencer.dto.BusinessOnboardingRequest;
import com.example.influencer.dto.BusinessProfileImageResponse;
import com.example.influencer.dto.BusinessProfileResponse;
import com.example.influencer.exception.BusinessEmailExistsException;
import com.example.influencer.exception.GlobalExceptionHandler;
import com.example.influencer.exception.ImageUploadException;
import com.example.influencer.service.BusinessOnboardingService;
import com.example.influencer.service.BusinessProfileImageService;

@WebMvcTest({ BusinessOnboardingController.class, GlobalExceptionHandler.class })
class BusinessOnboardingControllerTest {

	private static final String VALID_PAYLOAD = """
			{
			  "businessName": "ABC Restaurant",
			  "category": "Food",
			  "city": "Solapur",
			  "email": "abc@example.com",
			  "phone": "9876543210",
			  "password": "StrongPassword123"
			}
			""";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private BusinessOnboardingService businessOnboardingService;

	@MockitoBean
	private BusinessProfileImageService businessProfileImageService;

	@Test
	void onboardReturnsCreatedForNewBusiness() throws Exception {
		mockMvc.perform(post("/api/business/onboarding")
				.contentType(MediaType.APPLICATION_JSON)
				.content(VALID_PAYLOAD))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.message").value("Onboarding success"));
	}

	@Test
	void onboardReturnsConflictWhenEmailExists() throws Exception {
		doThrow(new BusinessEmailExistsException()).when(businessOnboardingService)
				.register(any(BusinessOnboardingRequest.class));

		mockMvc.perform(post("/api/business/onboarding")
				.contentType(MediaType.APPLICATION_JSON)
				.content(VALID_PAYLOAD))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("EMAIL EXISTS"));
	}

	@Test
	void onboardReturnsBadRequestWhenFieldsAreInvalid() throws Exception {
		mockMvc.perform(post("/api/business/onboarding")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "businessName": "",
						  "category": "Food",
						  "city": "Solapur",
						  "email": "not-an-email",
						  "phone": "123",
						  "password": "short"
						}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").isString());
	}

	@Test
	void loginReturnsBusinessDetailsWhenCredentialsMatch() throws Exception {
		BusinessProfileResponse profile = new BusinessProfileResponse();
		profile.setId(1L);
		profile.setBusinessName("ABC Restaurant");
		profile.setCategory("Food");
		profile.setCity("Solapur");
		profile.setEmail("abc@example.com");
		profile.setPhone("9876543210");
		when(businessOnboardingService.login(any(BusinessLoginRequest.class))).thenReturn(Optional.of(profile));

		mockMvc.perform(post("/api/business/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "email": "abc@example.com",
						  "password": "StrongPassword123"
						}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.businessName").value("ABC Restaurant"))
				.andExpect(jsonPath("$.email").value("abc@example.com"))
				.andExpect(jsonPath("$.city").value("Solapur"))
				.andExpect(jsonPath("$.password").doesNotExist());
	}

	@Test
	void loginReturnsLoginFailedWhenCredentialsDoNotMatch() throws Exception {
		when(businessOnboardingService.login(any(BusinessLoginRequest.class))).thenReturn(Optional.empty());

		mockMvc.perform(post("/api/business/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "email": "abc@example.com",
						  "password": "wrong"
						}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").value("Login Failed"));
	}

	@Test
	void uploadProfileImageReturnsStoredFileName() throws Exception {
		when(businessProfileImageService.storeProfileImage(eq("business1@gmail.com"), any(MultipartFile.class)))
				.thenReturn(BusinessProfileImageResponse.stored("business1@gmail.com", "profile.jpg"));

		MockMultipartFile image = new MockMultipartFile("image", "photo.jpg", MediaType.IMAGE_JPEG_VALUE,
				new byte[] { 1, 2, 3 });

		mockMvc.perform(multipart("/api/business/upload-profile-image")
				.file(image)
				.param("email", "business1@gmail.com"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Business profile image uploaded successfully"))
				.andExpect(jsonPath("$.email").value("business1@gmail.com"))
				.andExpect(jsonPath("$.fileName").value("profile.jpg"));
	}

	@Test
	void uploadProfileImageReturnsBadRequestWhenEmailIsMissing() throws Exception {
		MockMultipartFile image = new MockMultipartFile("image", "photo.jpg", MediaType.IMAGE_JPEG_VALUE,
				new byte[] { 1, 2, 3 });

		mockMvc.perform(multipart("/api/business/upload-profile-image").file(image))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Email is required"));
	}

	@Test
	void getProfileImageReturnsUrlForEmail() throws Exception {
		when(businessProfileImageService.getProfileImageUrl("business1@gmail.com"))
				.thenReturn("http://localhost:8080/Bussness/business1@gmail.com/profile.jpg");

		mockMvc.perform(get("/api/business/profile-image").param("email", "business1@gmail.com"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").value("http://localhost:8080/Bussness/business1@gmail.com/profile.jpg"));
	}

	@Test
	void uploadProfileImageReturnsBadRequestForEmptyImage() throws Exception {
		when(businessProfileImageService.storeProfileImage(eq("business1@gmail.com"), any(MultipartFile.class)))
				.thenThrow(new ImageUploadException(HttpStatus.BAD_REQUEST, "Image is required"));

		MockMultipartFile image = new MockMultipartFile("image", "photo.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[0]);

		mockMvc.perform(multipart("/api/business/upload-profile-image")
				.file(image)
				.param("email", "business1@gmail.com"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false));
	}
}
