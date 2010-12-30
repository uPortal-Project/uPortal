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
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jasig.portal.portlet.om.PortletCategory;

public class ChannelCategoryBean implements Comparable<ChannelCategoryBean>, Serializable {
	
	private String id;
   	private String name;
   	private String description;
   	private SortedSet<ChannelCategoryBean> categories;
   	private SortedSet<ChannelBean> channels;
   	
   	public ChannelCategoryBean() {
        categories = new TreeSet<ChannelCategoryBean>();
        channels = new TreeSet<ChannelBean>();
   	}
   	
   	public ChannelCategoryBean(PortletCategory category) {
   		this.id = category.getId();
   		this.name = category.getName();
   		this.description = category.getDescription();
   		categories = new TreeSet<ChannelCategoryBean>();
   		channels = new TreeSet<ChannelBean>();
   	}
   	
   	public void addCategory(ChannelCategoryBean category) {
   		this.categories.add(category);
   	}
   	
   	public void addChannel(ChannelBean channel) {
   		this.channels.add(channel);
   	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public SortedSet<ChannelCategoryBean> getCategories() {
		return this.categories;
	}

	public void setCategories(SortedSet<ChannelCategoryBean> categories) {
		this.categories = categories;
	}

	public SortedSet<ChannelBean> getChannels() {
		return this.channels;
	}

	public void setChannels(SortedSet<ChannelBean> channels) {
		this.channels = channels;
	}

    public int compareTo(ChannelCategoryBean category) {
        return new CompareToBuilder()
            .append(this.id, category.getId())
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
        if (!(object instanceof ChannelCategoryBean)) {
            return false;
        }
        ChannelCategoryBean rhs = (ChannelCategoryBean) object;
        return new EqualsBuilder()
            .append(this.id, rhs.getId())
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(464270933, -1074792143)
            .append(this.id)
            .toHashCode();
    }

}
