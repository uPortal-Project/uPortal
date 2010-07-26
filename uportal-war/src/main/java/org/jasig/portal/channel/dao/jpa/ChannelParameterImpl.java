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

package org.jasig.portal.channel.dao.jpa;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jasig.portal.channel.IChannelParameter;

/**
 * JPA implementation of the IChanenlParameter interface.
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 */
@Embeddable
@Table(name = "UP_CHANNEL_PARAM")
public class ChannelParameterImpl implements IChannelParameter, Serializable {

	@Column(name = "CHAN_PARM_NM", length = 255, nullable = false)
	String name;

	@Column(name = "CHAN_PARM_VAL", length = 2000)
	String value;

	@Column(name = "CHAN_PARM_OVRD")
	boolean override;

	@Column(name = "CHAN_PARM_DESC", length = 255)
	String descr;
	
	
	/**
	 * Default constructor required by Hibernate
	 */
	public ChannelParameterImpl() { }

    /**
     * Instantiate a ChannelParameter with a particular name, default value,
     * and indication of whether it can be overridden.
     * @param name name of the channel parameter.
     * @param value default value for the parameter.
     * @param override true if the default value may be overridden.
     */
    public ChannelParameterImpl(String name, String value, boolean override) {
      this.name = name;
      this.value = value;
      this.override = override;
    }
    
    /**
     * Construct a new ChannelParameterImpl from an IChannelParameter 
     * 
     * @param param
     */
    public ChannelParameterImpl(IChannelParameter param) {
    	this.name = param.getName();
    	this.value = param.getValue();
    	this.descr = param.getDescription();
    	this.override = param.getOverride();
    }
    
    
	// Getter methods

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.channel.IChannelParameter#getName()
     */
	public String getName() {
		return this.name;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.channel.IChannelParameter#getValue()
	 */
	public String getValue() {
		return this.value;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.channel.IChannelParameter#getOverride()
	 */
	public boolean getOverride() {
		return this.override;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.channel.IChannelParameter#getDescription()
	 */
	public String getDescription() {
		return this.descr;
	}

	
	// Setter methods

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.channel.IChannelParameter#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.channel.IChannelParameter#setValue(java.lang.String)
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.channel.IChannelParameter#setOverride(boolean)
	 */
	public void setOverride(boolean override) {
		this.override = override;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.channel.IChannelParameter#setDescription(java.lang.String)
	 */
	public void setDescription(String descr) {
		this.descr = descr;
	}

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof IChannelParameter)) {
            return false;
        }
        IChannelParameter rhs = (IChannelParameter) object;
        return new EqualsBuilder()
            .append(this.name, rhs.getName())
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(1915068383, -1044838521)
            .append(this.name)
            .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("name", this.name)
            .append("value", this.value)
            .append("override", this.override)
            .append("descr", this.descr)
            .toString();
    }
}
