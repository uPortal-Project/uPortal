package org.jasig.portal.channel.dao;

import java.util.List;

import org.jasig.portal.channel.IChannelDefinition;

public interface IChannelDefinitionDao {

	public IChannelDefinition saveChannelDefinition(IChannelDefinition definition);
	
	public void deleteChannelDefinition(IChannelDefinition definition);
	
	public IChannelDefinition getChannelDefinition(int id);
	
	public IChannelDefinition getChannelDefinition(String fname);
	
	public List<IChannelDefinition> getChannelDefinitions();
}
