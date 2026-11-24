package com.example.influencer.exception;

public class BusinessEmailExistsException extends RuntimeException {

	public BusinessEmailExistsException() {
		super("EMAIL EXISTS");
	}
}
