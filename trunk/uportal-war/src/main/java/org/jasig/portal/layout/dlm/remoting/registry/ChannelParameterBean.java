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

package org.jasig.portal.layout.dlm.remoting.registry;

import java.io.Serializable;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class ChannelParameterBean implements Comparable, Serializable {
	
	private String name;
	private String value;
	private String description;
    private boolean override;
    
    public ChannelParameterBean() { }

    public ChannelParameterBean(String name, String value, String description, boolean override) {
        this.name = name;
        this.override = override;
        this.value = value;
        this.description = description;
    }

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean getOverride() {
		return this.override;
	}

	public void setOverride(boolean override) {
		this.override = override;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

    public int compareTo(Object object) {
        if (object == this) {
            return 0;
        }
        if (!(object instanceof ChannelParameterBean)) {
            throw new IllegalArgumentException("Argument is not a ChannelParameterBean");
        }
        ChannelParameterBean rhs = (ChannelParameterBean) object;
        return new CompareToBuilder()
            .append(this.name, rhs.getName())
            .append(this.value, rhs.getValue())
            .append(this.description, rhs.getDescription())
            .append(this.override, rhs.getOverride())
            .toComparison();
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof ChannelParameterBean)) {
            return false;
        }
        ChannelParameterBean rhs = (ChannelParameterBean) object;
        return new EqualsBuilder()
            .append(this.name, rhs.getName())
            .append(this.value, rhs.getValue())
            .append(this.description, rhs.getDescription())
            .append(this.override, rhs.getOverride())
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(464270933, -1074792143)
            .append(this.name)
            .append(this.value)
            .append(this.description)
            .append(this.override)
            .toHashCode();
    }

}
