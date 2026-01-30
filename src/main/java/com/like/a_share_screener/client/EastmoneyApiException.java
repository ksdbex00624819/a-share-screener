package com.like.a_share_screener.client;

public class EastmoneyApiException extends RuntimeException {
	public EastmoneyApiException(String message) {
		super(message);
	}

	public EastmoneyApiException(String message, Throwable cause) {
		super(message, cause);
	}
}
