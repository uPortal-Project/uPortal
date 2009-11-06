/**
 * 
 */
package org.jasig.portal.web.skin;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Java bean to represent the "js" type in the 
 * "http://www.jasig.org/uportal/web/skin" schema.
 * 
 * @author Nicholas Blair, npblair@wisc.edu
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "value"
})
@XmlRootElement(name = "js", namespace = "http://www.jasig.org/uportal/web/skin")
public class Js {

	@XmlValue
    private String value;
    @XmlAttribute
    private String conditional;
    @XmlAttribute
    private boolean compressed = false;
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	/**
	 * @return the conditional
	 */
	public String getConditional() {
		return conditional;
	}
	/**
	 * @param conditional the conditional to set
	 */
	public void setConditional(String conditional) {
		this.conditional = conditional;
	}
	 /**
	 * @return whether this instance is already "compressed"
	 */
	public boolean isCompressed() {
		return compressed;
	}
	/**
	 * @param compressed the minified to set
	 */
	public void setCompressed(boolean compressed) {
		this.compressed = compressed;
	}
	
	/**
     * 
     * @return
     */
    public boolean isAbsolute() {
    	if(null == value) { return false; }
    	return value.startsWith("/");
    }
    
    /**
     * 
     * @return true if the "conditional" property of this instance is not blank.
     */
    public boolean isConditional() {
    	return StringUtils.isNotBlank(this.conditional);
    }
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Js)) {
			return false;
		}
		Js rhs = (Js) obj;
		EqualsBuilder builder = new EqualsBuilder();
		builder.append(this.conditional, rhs.conditional);
		builder.append(this.compressed, rhs.compressed);
		builder.append(this.value, rhs.value);
		return builder.isEquals();
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(this.conditional);
		builder.append(this.compressed);
		builder.append(this.value);
		return builder.toHashCode();
	}
    
	/**
	 * Similar to the {@link #equals(Object)} method, this will return
	 * true if this object and the argument are "aggregatable".
	 * 
	 * 2 {@link Js} objects are aggregatable if and only if:
	 * <ol>
	 * <li>Neither object returns true for {@link #isCompressed()}</li>
	 * <li>Neither object returns true for {@link #isAbsolute()}</li>
	 * <li>The values of their "conditional" properties are equivalent</li>
	 * <li>The "paths" of their values are equivalent</li>
	 * </ol>
	 * 
	 * The last rule mentioned above uses {@link FilenameUtils#getFullPath(String)}
	 * to compare each object's value. In short, the final file name in the value's path
	 * need not be equal, but the rest of the path in the value must be equal.
	 * 
	 * @param other
	 * @return
	 */
    public boolean willAggregateWith(final Js other) {
    	Validate.notNull(other, "Js cannot be null");
    	// never aggregate compressed
    	if(this.isCompressed() || other.isCompressed()) {
    		return false;
    	}
    	// never aggregate absolutes
    	if(this.isAbsolute() || other.isAbsolute()) {
    		return false;
    	}
    	EqualsBuilder builder = new EqualsBuilder();
		builder.append(this.conditional, other.conditional);
		String thisFullPath = FilenameUtils.getFullPath(this.value);
		String otherFullPath = FilenameUtils.getFullPath(other.value);
		builder.append(thisFullPath, otherFullPath);
		return builder.isEquals();
    }
}
