package org.jasig.portal.portlet.registry;

import java.util.List;

import org.jasig.portal.portlet.om.IPortletType;

public interface IPortletTypeRegistry {

    public IPortletType createPortletType(String name, String cpdUri);
    
    public IPortletType getPortletType(int channelTypeId);

    public IPortletType getPortletType(String name);

    public List<IPortletType> getPortletTypes();
    
    public IPortletType savePortletType(IPortletType chanType);
    
    public void deleteChannelType(IPortletType chanType);

}
