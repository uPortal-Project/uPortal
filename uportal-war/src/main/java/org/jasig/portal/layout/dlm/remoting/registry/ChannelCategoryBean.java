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
import java.util.ArrayList;
import java.util.List;

import org.jasig.portal.ChannelCategory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

public class ChannelCategoryBean implements Serializable {
	
	private String id;
   	private String name;
   	private String description;
   	private List<ChannelCategoryBean> categories;
   	private List<ChannelBean> channels;
   	
   	public ChannelCategoryBean() {
   	    channels = new ArrayList<ChannelBean>();
   	    categories = new ArrayList<ChannelCategoryBean>();
   	}
   	
   	public ChannelCategoryBean(ChannelCategory category) {
   		this.id = category.getId();
   		this.name = category.getName();
   		this.description = category.getDescription();
   		categories = new ArrayList<ChannelCategoryBean>();
   		channels = new ArrayList<ChannelBean>();
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

	public List<ChannelCategoryBean> getCategories() {
		return this.categories;
	}

	public void setCategories(List<ChannelCategoryBean> categories) {
		this.categories = categories;
	}

	public List<ChannelBean> getChannels() {
		return this.channels;
	}

	public void setChannels(List<ChannelBean> channels) {
		this.channels = channels;
	}
	
}
