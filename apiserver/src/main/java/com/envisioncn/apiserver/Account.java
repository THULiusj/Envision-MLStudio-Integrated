package com.envisioncn.apiserver;

import com.envision.eeop.api.EnvisionClient;

public class Account {
	
	private String name;
	private String password;
	private String accessToken;
	private String refreshToken;
	private long tokenExpire;
	private String userId;
	private EnvisionClient client;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getAccessToken() {
		return accessToken;
	}
	
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public long getTokenExpire() {
		return tokenExpire;
	}

	public void setTokenExpire(long tokenExpire) {
		this.tokenExpire = tokenExpire;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public EnvisionClient getClient() {
		return client;
	}

	public void setClient(EnvisionClient client) {
		this.client = client;
	}
	
}
