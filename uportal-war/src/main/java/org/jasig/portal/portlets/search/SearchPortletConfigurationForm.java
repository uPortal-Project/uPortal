/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.portlets.search;

public class SearchPortletConfigurationForm {

    private boolean gsaEnabled;
    private String gsaBaseUrl;
    private String gsaSite;

    private boolean directoryEnabled;
    private boolean portletRegistryEnabled;
	
	private boolean throttlingEnabled;
	private int throttleMaxSearches,throttleTimePeriod;
	
	public boolean isThrottlingEnabled() {
		return throttlingEnabled;
	}
	
	public int getThrottleMaxSearches() {
		return throttleMaxSearches;
	}
	
	public int getThrottleTimePeriod() {
		return throttleTimePeriod;
	}
	
	public void setThrottlingEnabled(boolean throttlingEnabled) {
		this.throttlingEnabled = throttlingEnabled;
	}
	
	public void setThrottleMaxSearches(int throttleMaxSearches) {
		this.throttleMaxSearches = throttleMaxSearches;
	}
	
	public void setThrottleTimePeriod(int throttleTimePeriod) {
		this.throttleTimePeriod = throttleTimePeriod;
	}
	
    public boolean isGsaEnabled() {
        return gsaEnabled;
    }

    public void setGsaEnabled(boolean gsaEnabled) {
        this.gsaEnabled = gsaEnabled;
    }

    public String getGsaBaseUrl() {
        return gsaBaseUrl;
    }

    public void setGsaBaseUrl(String gsaBaseUrl) {
        this.gsaBaseUrl = gsaBaseUrl;
    }

    public String getGsaSite() {
        return gsaSite;
    }

    public void setGsaSite(String gsaSite) {
        this.gsaSite = gsaSite;
    }

    public boolean isDirectoryEnabled() {
        return directoryEnabled;
    }

    public void setDirectoryEnabled(boolean directoryEnabled) {
        this.directoryEnabled = directoryEnabled;
    }

    public boolean isPortletRegistryEnabled() {
        return portletRegistryEnabled;
    }

    public void setPortletRegistryEnabled(boolean portletRegistryEnabled) {
        this.portletRegistryEnabled = portletRegistryEnabled;
    }

}
