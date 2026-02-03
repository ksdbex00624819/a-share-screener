package com.like.a_share_screener.client;

public class EastmoneyNonRetryableException extends EastmoneyApiException {
	private final Integer rc;

	public EastmoneyNonRetryableException(String message, Integer rc) {
		super(message);
		this.rc = rc;
	}

	public EastmoneyNonRetryableException(String message, Integer rc, Throwable cause) {
		super(message, cause);
		this.rc = rc;
	}

	public Integer getRc() {
		return rc;
	}
}
