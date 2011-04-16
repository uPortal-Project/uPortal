package org.jasig.portal.portlet.registry;

import java.util.List;

import org.jasig.portal.portlet.dao.IPortletTypeDao;
import org.jasig.portal.portlet.om.IPortletType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
@Service("portletTypeRegistry")
public class PortletTypeRegistryImpl implements IPortletTypeRegistry {

    private IPortletTypeDao portletTypeDao;

    @Autowired(required = true)
    public void setPortletTypeDao(IPortletTypeDao portletTypeDao) {
    	this.portletTypeDao = portletTypeDao;
    }

    @Override
    public IPortletType createPortletType(String name, String cpdUri) {
    	return this.portletTypeDao.createPortletType(name, cpdUri);
    }
    
    @Override
    public IPortletType getPortletType(int channelTypeId) {
    	return portletTypeDao.getPortletType(channelTypeId);
    }

    @Override
    public IPortletType getPortletType(String name) {
        return portletTypeDao.getPortletType(name);
    }

    @Override
    public List<IPortletType> getPortletTypes() {
        return portletTypeDao.getPortletTypes();
    }
    
    @Override
    public IPortletType savePortletType(IPortletType chanType) {
        return portletTypeDao.updatePortletType(chanType);
    }
    
    @Override
    public void deleteChannelType(IPortletType chanType) {
    	portletTypeDao.deletePortletType(chanType);
    }

}
