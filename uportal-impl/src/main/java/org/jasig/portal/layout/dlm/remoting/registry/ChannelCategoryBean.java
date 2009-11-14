package org.jasig.portal.layout.dlm.remoting.registry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jasig.portal.ChannelCategory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("category")
public class ChannelCategoryBean implements Serializable {
	
   	@XStreamAlias("ID")
   	@XStreamAsAttribute
	private String id;

   	@XStreamAsAttribute
   	private String name;
	
   	@XStreamAsAttribute
   	private String description;
   	
   	@XStreamImplicit(itemFieldName="category")
   	private List<ChannelCategoryBean> categories;
   	
   	@XStreamImplicit(itemFieldName="channel")
   	private List<ChannelBean> channels;
   	
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
