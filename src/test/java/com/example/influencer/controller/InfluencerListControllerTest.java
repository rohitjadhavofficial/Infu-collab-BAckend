package com.example.influencer.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.influencer.dto.InfluencerResponseDTO;
import com.example.influencer.exception.GlobalExceptionHandler;
import com.example.influencer.service.InfluencerOnboardingService;

@WebMvcTest({ InfluencerListController.class, GlobalExceptionHandler.class })
class InfluencerListControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private InfluencerOnboardingService influencerOnboardingService;

	@Test
	void getAllInfluencersReturnsProfileImageUrl() throws Exception {
		InfluencerResponseDTO influencer = new InfluencerResponseDTO();
		influencer.setName("ss");
		influencer.setEmail("abc@gmail.com");
		influencer.setLocation("pune");
		influencer.setCategories(List.of("Fashion & Lifestyle"));
		influencer.setProfileImageUrl("http://localhost:8080/uploads/users/abc@gmail.com/image_1.png");
		when(influencerOnboardingService.listAllWithProfileImages()).thenReturn(List.of(influencer));

		mockMvc.perform(get("/influencers"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].email").value("abc@gmail.com"))
				.andExpect(jsonPath("$[0].name").value("ss"))
				.andExpect(jsonPath("$[0].location").value("pune"))
				.andExpect(jsonPath("$[0].profileImageUrl")
						.value("http://localhost:8080/uploads/users/abc@gmail.com/image_1.png"))
				.andExpect(jsonPath("$[0].password").doesNotExist());
	}

	@Test
	void getAllInfluencersIsAlsoAvailableUnderApiPrefix() throws Exception {
		when(influencerOnboardingService.listAllWithProfileImages()).thenReturn(List.of());

		mockMvc.perform(get("/api/influencers"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray());
	}
}
