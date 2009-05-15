package org.jasig.portal.channel.dao;

import java.util.List;

import org.jasig.portal.channel.IChannelType;

public interface IChannelTypeDao {
	
	public IChannelType saveChannelType(IChannelType type);
	
	public void deleteChannelType(IChannelType type);
	
	public IChannelType getChannelType(int id);
	
	public IChannelType getChannelType(String fname);
	
	public List<IChannelType> getChannelTypes();

}
