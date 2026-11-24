package com.example.influencer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.influencer.entity.Ads;

public interface AdsRepository extends JpaRepository<Ads, Long> {

	List<Ads> findAllByOrderByCreatedAtDesc();
}
