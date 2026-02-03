package com.like.a_share_screener.client;

import java.util.Map;

public interface EastmoneyHttpInvoker {
	String get(String url, Map<String, String> headers, int timeoutMs);
}
