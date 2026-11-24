package com.example.influencer.exception;

public class DuplicateEmailException extends RuntimeException {

	public DuplicateEmailException() {
		super("Email duplicate");
	}
}
