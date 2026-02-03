package com.like.a_share_screener.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "eastmoney")
public class EastmoneyProperties {
	private String baseUrl;
	private String clistBaseUrl;
	private String userAgent;
	private String referer;
	private String ut;
	private String fields1;
	private String fields2;
	private String clistFields;
	private String clistFs;
	private Integer clistPageSize = 200;

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getClistBaseUrl() {
		return clistBaseUrl;
	}

	public void setClistBaseUrl(String clistBaseUrl) {
		this.clistBaseUrl = clistBaseUrl;
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

	public String getClistFields() {
		return clistFields;
	}

	public void setClistFields(String clistFields) {
		this.clistFields = clistFields;
	}

	public String getClistFs() {
		return clistFs;
	}

	public void setClistFs(String clistFs) {
		this.clistFs = clistFs;
	}

	public Integer getClistPageSize() {
		return clistPageSize;
	}

	public void setClistPageSize(Integer clistPageSize) {
		this.clistPageSize = clistPageSize;
	}

}
