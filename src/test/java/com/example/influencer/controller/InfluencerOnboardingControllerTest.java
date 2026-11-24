package com.example.influencer.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.influencer.dto.InfluencerLoginRequest;
import com.example.influencer.dto.InfluencerOnboardingRequest;
import com.example.influencer.dto.InfluencerProfileResponse;
import com.example.influencer.exception.DuplicateEmailException;
import com.example.influencer.exception.GlobalExceptionHandler;
import com.example.influencer.service.InfluencerOnboardingService;

@WebMvcTest({ InfluencerOnboardingController.class, GlobalExceptionHandler.class })
class InfluencerOnboardingControllerTest {

	private static final String PAYLOAD = """
			{
			  "name": "ss",
			  "followers": 10003,
			  "categories": ["Fashion & Lifestyle"],
			  "averageCollaborationPriceInr": 400,
			  "location": "pune",
			  "email": "abc@gmail.com",
			  "countryCode": "+91",
			  "mobile": "8208967904",
			  "submittedAt": "2026-10-10T14:01:18.650Z"
			}
			""";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private InfluencerOnboardingService influencerOnboardingService;

	@Test
	void onboardReturnsOkWhenSaveSucceeds() throws Exception {
		mockMvc.perform(post("/api/influencers/onboarding")
				.contentType(MediaType.APPLICATION_JSON)
				.content(PAYLOAD))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("OK"))
				.andExpect(jsonPath("$.message").value("Influencer data saved successfully"));
	}

	@Test
	void onboardReturnsConflictWhenEmailExists() throws Exception {
		doThrow(new DuplicateEmailException()).when(influencerOnboardingService)
				.save(any(InfluencerOnboardingRequest.class));

		mockMvc.perform(post("/api/influencers/onboarding")
				.contentType(MediaType.APPLICATION_JSON)
				.content(PAYLOAD))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value("OK"))
				.andExpect(jsonPath("$.message").value("Duplicate email detected"));
	}

	@Test
	void onboardReturnsServerErrorWhenSaveFails() throws Exception {
		doThrow(new RuntimeException("database down")).when(influencerOnboardingService)
				.save(any(InfluencerOnboardingRequest.class));

		mockMvc.perform(post("/api/influencers/onboarding")
				.contentType(MediaType.APPLICATION_JSON)
				.content(PAYLOAD))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.status").value("ERROR"))
				.andExpect(jsonPath("$.message").value("Unable to save influencer data"));
	}

	@Test
	void loginReturnsUserDetailsWhenCredentialsAreValid() throws Exception {
		InfluencerProfileResponse profile = new InfluencerProfileResponse();
		profile.setName("ss");
		profile.setEmail("abc@gmail.com");
		profile.setFollowers(10003L);
		profile.setCategories(List.of("Fashion & Lifestyle"));
		profile.setLocation("pune");
		when(influencerOnboardingService.login(any(InfluencerLoginRequest.class)))
				.thenReturn(Optional.of(profile));

		mockMvc.perform(post("/api/influencers/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "email": "abc@gmail.com",
						  "password": "secret123"
						}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("abc@gmail.com"))
				.andExpect(jsonPath("$.name").value("ss"))
				.andExpect(jsonPath("$.followers").value(10003))
				.andExpect(jsonPath("$.location").value("pune"));
	}

	@Test
	void loginReturnsWrongCredentialsWhenLoginFails() throws Exception {
		when(influencerOnboardingService.login(any(InfluencerLoginRequest.class)))
				.thenReturn(Optional.empty());

		mockMvc.perform(post("/api/influencers/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "email": "abc@gmail.com",
						  "password": "wrong"
						}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").value("WRONG CREDENCTIOLS"));
	}
}
