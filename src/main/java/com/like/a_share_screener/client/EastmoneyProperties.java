package com.like.a_share_screener.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "eastmoney")
public class EastmoneyProperties {
	private String baseUrl;
	private String userAgent;
	private String referer;
	private String ut;
	private String fields1;
	private String fields2;

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String getReferer() {
		return referer;
	}

	public void setReferer(String referer) {
		this.referer = referer;
	}

	public String getUt() {
		return ut;
	}

	public void setUt(String ut) {
		this.ut = ut;
	}

	public String getFields1() {
		return fields1;
	}

	public void setFields1(String fields1) {
		this.fields1 = fields1;
	}

	public String getFields2() {
		return fields2;
	}

	public void setFields2(String fields2) {
		this.fields2 = fields2;
	}
}
