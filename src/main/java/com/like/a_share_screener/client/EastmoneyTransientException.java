package com.like.a_share_screener.client;

public class EastmoneyTransientException extends EastmoneyApiException {
	private final Integer rc;

	public EastmoneyTransientException(String message, Integer rc) {
		super(message);
		this.rc = rc;
	}

	public EastmoneyTransientException(String message, Integer rc, Throwable cause) {
		super(message, cause);
		this.rc = rc;
	}

	public Integer getRc() {
		return rc;
	}
}
