package org.jasig.portal.portlets.localization;

import java.io.Serializable;

public class LocaleBean implements Serializable {
	
	private String locale;
	private String countryCode;
	private String displayValue;
	
	public LocaleBean(String locale, String countryCode, String displayValue) {
		this.locale = locale;
		this.countryCode = countryCode;
		this.displayValue = displayValue;
	}
	
	public String getLocale() {
		return this.locale;
	}
	public void setLocale(String locale) {
		this.locale = locale;
	}
	public String getCountryCode() {
		return this.countryCode;
	}
	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}
	public String getDisplayValue() {
		return this.displayValue;
	}
	public void setDisplayValue(String displayValue) {
		this.displayValue = displayValue;
	}

	
}
