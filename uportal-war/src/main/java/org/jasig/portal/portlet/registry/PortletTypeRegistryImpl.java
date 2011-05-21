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
