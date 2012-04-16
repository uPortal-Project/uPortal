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
/**
 * 
 */
package org.jasig.portal.portlet.container.cache;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.portlet.CacheControl;

import org.apache.commons.lang.StringUtils;

/**
 * Java bean to represent the data cached for a portlet request via 
 * {@link CacheControl}s.
 * 
 * @author Nicholas Blair, npblair@wisc.edu
 * @version $Id$
 */
public class CachedPortletData implements Serializable {
	private static final long serialVersionUID = 5509299103587289000L;

	private String etag;
	private long timeStored;
	private int expirationTimeSeconds;
	private int cacheConfigurationMaxTTL;
	
	private byte[] byteData;
	private String stringData;
	
	private Integer status;
	private String statusMessage;
	private String contentType;
    private String charset;
    private Integer contentLength;
    private Locale locale;
	private Map<String, List<Object>> headers = new LinkedHashMap<String, List<Object>>();

	
	/**
	 * @return
	 */
	public String getEtag() {
		return etag;
	}
	/**
	 * @param etag
	 */
	public void setEtag(String etag) {
		this.etag = StringUtils.trimToNull(etag);
	}
	/**
	 * @return the timeStored
	 */
	public long getTimeStored() {
		return timeStored;
	}
	/**
	 * @param timeStored the timeStored to set
	 */
	public void setTimeStored(long timeStored) {
		this.timeStored = timeStored;
	}
	
	public int getExpirationTimeSeconds() {
		return expirationTimeSeconds;
	}
	public void setExpirationTimeSeconds(int expirationTimeSeconds) {
		this.expirationTimeSeconds = expirationTimeSeconds;
	}
	/**
	 * @return the byteData
	 */
	public byte[] getByteData() {
		return byteData;
	}
	/**
	 * @param byteData the byteData to set
	 */
	public void setByteData(byte[] byteData) {
		this.byteData = byteData;
	}
	/**
	 * @return the stringData
	 */
	public String getStringData() {
		return stringData;
	}
	/**
	 * @param stringData the stringData to set
	 */
	public void setStringData(String stringData) {
		this.stringData = stringData;
	}
	/**
	 * @return the contentType
	 */
	public String getContentType() {
		return contentType;
	}
	/**
	 * @param contentType the contentType to set
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	/**
     * @return the charset
     */
    public String getCharacterEncoding() {
        return charset;
    }
    /**
     * @param charset the charset to set
     */
    public void setCharacterEncoding(String charset) {
        this.charset = charset;
    }
    /**
     * @return the contentLength
     */
    public Integer getContentLength() {
        return contentLength;
    }
    /**
     * @param contentLength the contentLength to set
     */
    public void setContentLength(Integer contentLength) {
        this.contentLength = contentLength;
    }
    /**
     * @return the locale
     */
    public Locale getLocale() {
        return locale;
    }
    /**
     * @param locale the locale to set
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
    /**
     * @return the headers
     */
    public Map<String, List<Object>> getHeaders() {
        return headers;
    }
    /**
     * @param headers the headers to set
     */
    public void setHeaders(Map<String, List<Object>> headers) {
        this.headers = headers;
    }
    /**
     * @return the status
     */
    public Integer getStatus() {
        return status;
    }
    /**
     * @param status the status to set
     */
    public void setStatus(Integer status) {
        this.status = status;
    }
    /**
     * @return the statusMessage
     */
    public String getStatusMessage() {
        return statusMessage;
    }
    /**
     * @param statusMessage the statusMessage to set
     */
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
    /**
	 * @return the cacheConfigurationMaxTTL
	 */
	public int getCacheConfigurationMaxTTL() {
		return cacheConfigurationMaxTTL;
	}
	/**
	 * @param cacheConfigurationMaxTTL the cacheConfigurationMaxTTL to set
	 */
	public void setCacheConfigurationMaxTTL(int cacheConfigurationMaxTTL) {
		this.cacheConfigurationMaxTTL = cacheConfigurationMaxTTL;
	}
	/**
	 * Mutator method to allow the Portlet renderer to update the expiration time
	 * for CachedPortletData instances that are expired.
	 * 
	 * Uses the min value of the argument and the {@link #getCacheConfigurationMaxTTL()}
	 * values to update this fields timeStored.
	 * 
	 * Note: This method is synchronized, as multiple threads may be interacting with the same 
	 * CachedPortletData instance concurrently.
	 * 
	 * the same portlet
	 * @param requestedExpirationTimeSeconds
	 */
	public synchronized void updateExpirationTime(int requestedExpirationTimeSeconds) { 
		this.expirationTimeSeconds = Math.min(requestedExpirationTimeSeconds, getCacheConfigurationMaxTTL());
		this.timeStored = System.currentTimeMillis();
	}
	/**
	 * 
	 * @return true if the {@link #getTimeStored()} is before the current time less expirationTimeSeconds, or if {@link CacheControl#getExpirationTime()} == 0
	 */
	public boolean isExpired() {
		if(expirationTimeSeconds == -1) {
			// never expires
			return false;
		} else if (expirationTimeSeconds == 0) {
			// always expired
			return true;
		} else {
			final long referencePoint = (System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(expirationTimeSeconds));
			return timeStored < referencePoint;
		}
	}

	@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(byteData);
        result = prime * result + cacheConfigurationMaxTTL;
        result = prime * result + ((charset == null) ? 0 : charset.hashCode());
        result = prime * result + ((contentLength == null) ? 0 : contentLength.hashCode());
        result = prime * result + ((contentType == null) ? 0 : contentType.hashCode());
        result = prime * result + ((etag == null) ? 0 : etag.hashCode());
        result = prime * result + expirationTimeSeconds;
        result = prime * result + ((headers == null) ? 0 : headers.hashCode());
        result = prime * result + ((locale == null) ? 0 : locale.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((statusMessage == null) ? 0 : statusMessage.hashCode());
        result = prime * result + ((stringData == null) ? 0 : stringData.hashCode());
        result = prime * result + (int) (timeStored ^ (timeStored >>> 32));
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CachedPortletData other = (CachedPortletData) obj;
        if (!Arrays.equals(byteData, other.byteData))
            return false;
        if (cacheConfigurationMaxTTL != other.cacheConfigurationMaxTTL)
            return false;
        if (charset == null) {
            if (other.charset != null)
                return false;
        }
        else if (!charset.equals(other.charset))
            return false;
        if (contentLength == null) {
            if (other.contentLength != null)
                return false;
        }
        else if (!contentLength.equals(other.contentLength))
            return false;
        if (contentType == null) {
            if (other.contentType != null)
                return false;
        }
        else if (!contentType.equals(other.contentType))
            return false;
        if (etag == null) {
            if (other.etag != null)
                return false;
        }
        else if (!etag.equals(other.etag))
            return false;
        if (expirationTimeSeconds != other.expirationTimeSeconds)
            return false;
        if (headers == null) {
            if (other.headers != null)
                return false;
        }
        else if (!headers.equals(other.headers))
            return false;
        if (locale == null) {
            if (other.locale != null)
                return false;
        }
        else if (!locale.equals(other.locale))
            return false;
        if (status == null) {
            if (other.status != null)
                return false;
        }
        else if (!status.equals(other.status))
            return false;
        if (statusMessage == null) {
            if (other.statusMessage != null)
                return false;
        }
        else if (!statusMessage.equals(other.statusMessage))
            return false;
        if (stringData == null) {
            if (other.stringData != null)
                return false;
        }
        else if (!stringData.equals(other.stringData))
            return false;
        if (timeStored != other.timeStored)
            return false;
        return true;
    }
    @Override
	public String toString() {
		return "CachedPortletData [etag=" + etag + ", timeStored=" + timeStored
				+ ", expirationTimeSeconds=" + expirationTimeSeconds
				+ ", contentType=" + contentType + ", headers=" + headers + "]";
	}
	
	
	
}
