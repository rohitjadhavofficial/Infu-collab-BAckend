package com.example.influencer.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.influencer.entity.InfluencerLogin;

import java.util.Optional;

public interface InfluencerLoginRepository extends JpaRepository<InfluencerLogin, String> {

	boolean existsByEmailIgnoreCase(String email);

	Optional<InfluencerLogin> findByEmailIgnoreCase(String email);
}
