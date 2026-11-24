package com.example.influencer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.influencer.dto.BusinessLoginRequest;
import com.example.influencer.dto.BusinessOnboardingRequest;
import com.example.influencer.dto.BusinessProfileResponse;
import com.example.influencer.entity.BusinessLogin;
import com.example.influencer.exception.BusinessEmailExistsException;
import com.example.influencer.repository.BusinessLoginRepository;

@SpringBootTest
class BusinessOnboardingServiceTest {

	@Autowired
	private BusinessOnboardingService businessOnboardingService;

	@Autowired
	private BusinessLoginRepository businessLoginRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void registerSavesHashedPasswordForNewEmail() {
		BusinessOnboardingRequest request = sampleRequest("new.business@example.com");

		businessOnboardingService.register(request);

		BusinessLogin saved = businessLoginRepository.findByEmailIgnoreCase("new.business@example.com").orElseThrow();
		assertThat(saved.getBusinessName()).isEqualTo("ABC Restaurant");
		assertThat(saved.getEmail()).isEqualTo("new.business@example.com");
		assertThat(saved.getPassword()).isNotEqualTo("StrongPassword123");
		assertThat(passwordEncoder.matches("StrongPassword123", saved.getPassword())).isTrue();
	}

	@Test
	void registerRejectsDuplicateEmailIgnoringCase() {
		businessOnboardingService.register(sampleRequest("Dup@Example.com"));

		assertThatThrownBy(() -> businessOnboardingService.register(sampleRequest("dup@example.com")))
				.isInstanceOf(BusinessEmailExistsException.class)
				.hasMessage("EMAIL EXISTS");
		assertThat(countByEmail("dup@example.com")).isEqualTo(1);
	}

	@Test
	void concurrentRegisterWithSameEmailCreatesOnlyOneRecord() throws Exception {
		int threads = 8;
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		CountDownLatch start = new CountDownLatch(1);
		AtomicInteger successes = new AtomicInteger();
		AtomicInteger conflicts = new AtomicInteger();
		List<Future<?>> futures = new ArrayList<>();

		for (int i = 0; i < threads; i++) {
			futures.add(executor.submit(() -> {
				start.await();
				try {
					businessOnboardingService.register(sampleRequest("concurrent@example.com"));
					successes.incrementAndGet();
				} catch (BusinessEmailExistsException ex) {
					conflicts.incrementAndGet();
				}
				return null;
			}));
		}

		start.countDown();
		for (Future<?> future : futures) {
			future.get(10, TimeUnit.SECONDS);
		}
		executor.shutdownNow();

		assertThat(successes.get()).isEqualTo(1);
		assertThat(conflicts.get()).isEqualTo(threads - 1);
		assertThat(countByEmail("concurrent@example.com")).isEqualTo(1);
	}

	@Test
	void loginReturnsBusinessDetailsWhenPasswordMatchesHash() {
		businessOnboardingService.register(sampleRequest("login.ok@example.com"));

		BusinessLoginRequest loginRequest = new BusinessLoginRequest();
		loginRequest.setEmail("LOGIN.OK@example.com");
		loginRequest.setPassword("StrongPassword123");

		BusinessProfileResponse profile = businessOnboardingService.login(loginRequest).orElseThrow();
		assertThat(profile.getEmail()).isEqualTo("login.ok@example.com");
		assertThat(profile.getBusinessName()).isEqualTo("ABC Restaurant");
		assertThat(profile.getCity()).isEqualTo("Solapur");
	}

	@Test
	void loginFailsWhenPasswordIsWrong() {
		businessOnboardingService.register(sampleRequest("login.fail@example.com"));

		BusinessLoginRequest loginRequest = new BusinessLoginRequest();
		loginRequest.setEmail("login.fail@example.com");
		loginRequest.setPassword("WrongPassword123");

		assertThat(businessOnboardingService.login(loginRequest)).isEmpty();
	}

	private long countByEmail(String email) {
		return businessLoginRepository.findAll().stream()
				.filter(business -> email.equalsIgnoreCase(business.getEmail()))
				.count();
	}

	private BusinessOnboardingRequest sampleRequest(String email) {
		BusinessOnboardingRequest request = new BusinessOnboardingRequest();
		request.setBusinessName("ABC Restaurant");
		request.setCategory("Food");
		request.setCity("Solapur");
		request.setEmail(email);
		request.setPhone("9876543210");
		request.setPassword("StrongPassword123");
		return request;
	}
}
