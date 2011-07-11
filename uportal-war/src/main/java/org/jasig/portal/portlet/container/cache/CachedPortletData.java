/**
 * 
 */
package org.jasig.portal.portlet.container.cache;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import javax.portlet.CacheControl;

import org.apache.commons.lang.time.DateUtils;

/**
 * Java bean to represent the data cached for a portlet request via 
 * {@link CacheControl}s.
 * 
 * @author Nicholas Blair, npblair@wisc.edu
 * @version $Id$
 */
public class CachedPortletData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5509299103587289000L;

	private String etag;
	private Date timeStored;
	private int expirationTimeSeconds;
	private byte[] byteData;
	private String stringData;
	private String contentType;
	private Map<String, String[]> headers;
	private Map<String, String> properties;
	
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
		this.etag = etag;
	}
	/**
	 * @return the timeStored
	 */
	public Date getTimeStored() {
		return timeStored;
	}
	/**
	 * @param timeStored the timeStored to set
	 */
	public void setTimeStored(Date timeStored) {
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
	 * @return the headers
	 */
	public Map<String, String[]> getHeaders() {
		return headers;
	}
	/**
	 * @param headers the headers to set
	 */
	public void setHeaders(Map<String, String[]> headers) {
		this.headers = headers;
	}
	/**
	 * @return the properties
	 */
	public Map<String, String> getProperties() {
		return properties;
	}
	/**
	 * @param properties the properties to set
	 */
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
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
			Date now = new Date();
			Date referencePoint = DateUtils.addSeconds(now, -expirationTimeSeconds);
			
			return timeStored.before(referencePoint);
		}
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(byteData);
		result = prime * result
				+ ((contentType == null) ? 0 : contentType.hashCode());
		result = prime * result + ((etag == null) ? 0 : etag.hashCode());
		result = prime * result + expirationTimeSeconds;
		result = prime * result + ((headers == null) ? 0 : headers.hashCode());
		result = prime * result
				+ ((properties == null) ? 0 : properties.hashCode());
		result = prime * result
				+ ((stringData == null) ? 0 : stringData.hashCode());
		result = prime * result
				+ ((timeStored == null) ? 0 : timeStored.hashCode());
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
		if (contentType == null) {
			if (other.contentType != null)
				return false;
		} else if (!contentType.equals(other.contentType))
			return false;
		if (etag == null) {
			if (other.etag != null)
				return false;
		} else if (!etag.equals(other.etag))
			return false;
		if (expirationTimeSeconds != other.expirationTimeSeconds)
			return false;
		if (headers == null) {
			if (other.headers != null)
				return false;
		} else if (!headers.equals(other.headers))
			return false;
		if (properties == null) {
			if (other.properties != null)
				return false;
		} else if (!properties.equals(other.properties))
			return false;
		if (stringData == null) {
			if (other.stringData != null)
				return false;
		} else if (!stringData.equals(other.stringData))
			return false;
		if (timeStored == null) {
			if (other.timeStored != null)
				return false;
		} else if (!timeStored.equals(other.timeStored))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "CachedPortletData [etag=" + etag + ", timeStored=" + timeStored
				+ ", expirationTimeSeconds=" + expirationTimeSeconds
				+ ", contentType=" + contentType + ", headers=" + headers
				+ ", properties=" + properties + "]";
	}
	
	
	
}
