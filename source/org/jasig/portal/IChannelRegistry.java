package org.jasig.portal;

/**
 * Interface defining how the portal retrieves it's channels and categories.
 * Methods are also provided to allow for publishing and unpublishing content.
 * The intent is that this task can be performed based on channel, category, and role.
 * @author John Laker
 * @version $Revision$
 */

public interface IChannelRegistry {
    public String getRegistryXML(String category, String role);
    public void setRegistryXML(String registryXML);
    public void removeChannel(String catID[], String chanID, String role[]);
    public void addChannel(String catID[], String chanXML, String role[]);
}

