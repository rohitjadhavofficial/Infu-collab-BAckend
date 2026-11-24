package com.example.influencer.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.influencer.entity.BusinessLogin;

public interface BusinessLoginRepository extends JpaRepository<BusinessLogin, Long> {

	boolean existsByEmailIgnoreCase(String email);

	Optional<BusinessLogin> findByEmailIgnoreCase(String email);
}
