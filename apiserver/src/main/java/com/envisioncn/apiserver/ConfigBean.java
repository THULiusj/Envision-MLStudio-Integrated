package com.envisioncn.apiserver;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "com.envisioncn.apiserver")
public class ConfigBean {
	
	private String appkey;
	private String appsecret;
	private String enosurl;
	private String downloadaddr;
	private String requestdownloadpath;
	private String requeststatuspath;
	private String orgid;
	private String rootpath;
	private long expiration;
	private long retentionperiod;
	
	public String getAppkey() {
		return appkey;
	}
	
	public void setAppkey(String appkey) {
		this.appkey = appkey;
	}
	
	public String getAppsecret() {
		return appsecret;
	}
	
	public void setAppsecret(String appsecret) {
		this.appsecret = appsecret;
	}
	
	public String getEnosurl() {
		return enosurl;
	}
	
	public void setEnosurl(String enosurl) {
		this.enosurl = enosurl;
	}
	
	public String getDownloadaddr() {
		return downloadaddr;
	}
	
	public void setDownloadaddr(String downloadaddr) {
		this.downloadaddr = downloadaddr;
	}
	
	public String getRequestdownloadpath() {
		return requestdownloadpath;
	}
	
	public void setRequestdownloadpath(String requestdownloadpath) {
		this.requestdownloadpath = requestdownloadpath;
	}
	
	public String getRequeststatuspath() {
		return requeststatuspath;
	}
	
	public void setRequeststatuspath(String requeststatuspath) {
		this.requeststatuspath = requeststatuspath;
	}
	
	public String getOrgid() {
		return orgid;
	}
	
	public void setOrgid(String orgid) {
		this.orgid = orgid;
	}
	
	public String getRootpath() {
		return rootpath;
	}
	
	public void setRootpath(String rootpath) {
		this.rootpath = rootpath;
	}
	
	public long getExpiration() {
		return expiration;
	}
	
	public void setExpiration(long expiration) {
		this.expiration = expiration;
	}
	
	public long getRetentionperiod() {
		return retentionperiod;
	}
	
	public void setRetentionperiod(long retentionperiod) {
		this.retentionperiod = retentionperiod;
	}
	
}
