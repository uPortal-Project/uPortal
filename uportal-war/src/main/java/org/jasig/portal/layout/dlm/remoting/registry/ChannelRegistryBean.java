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

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("registry")
public class ChannelRegistryBean implements Serializable {

   	@XStreamImplicit(itemFieldName="category")
   	private List<ChannelCategoryBean> categories;
   	
   	@XStreamImplicit(itemFieldName="channel")
   	private List<ChannelBean> channels;
   	
   	public ChannelRegistryBean() {
   		this.categories = new ArrayList<ChannelCategoryBean>();
   		this.channels = new ArrayList<ChannelBean>();
   	}
   	
   	public void addCategory(ChannelCategoryBean category) {
   		this.categories.add(category);
   	}

   	public void addChannel(ChannelBean channel) {
   		this.channels.add(channel);
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
