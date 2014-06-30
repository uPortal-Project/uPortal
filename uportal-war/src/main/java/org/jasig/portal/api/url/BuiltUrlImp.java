package org.jasig.portal.api.url;

import org.jasig.portal.api.url.BuiltUrl;

public class BuiltUrlImp implements BuiltUrl {

	private String url;
	
	private Boolean success;
	
	public BuiltUrlImp() {
	}

	@Override
	public String getUrl() {
		return url;
	}

	@Override
	public Boolean isSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
