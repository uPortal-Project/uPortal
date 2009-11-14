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
