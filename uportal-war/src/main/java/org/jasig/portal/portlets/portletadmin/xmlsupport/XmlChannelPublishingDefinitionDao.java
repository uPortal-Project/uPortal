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

package org.jasig.portal.portlets.portletadmin.xmlsupport;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.portlet.om.IPortletType;
import org.jasig.portal.portlet.registry.IPortletTypeRegistry;
import org.jasig.portal.portletpublishing.xml.PortletPublishingDefinition;
import org.jasig.portal.portletpublishing.xml.Step;
import org.jasig.portal.utils.threading.MapCachingDoubleCheckedCreator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service("channelPublishingDefinitionDao")
public class XmlChannelPublishingDefinitionDao implements IChannelPublishingDefinitionDao, ResourceLoaderAware, InitializingBean {
    private static final String CUSTOM_CPD_PATH = "/org/jasig/portal/portlets/portletadmin/CustomChannel.cpd";
    private static final String SHARED_PARAMETERS_PATH = "/org/jasig/portal/portlets/SharedParameters.cpd.xml";

    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private Unmarshaller unmarshaller;
    
    private MapCachingDoubleCheckedCreator<Integer, PortletPublishingDefinition> cpdCreator;
    private List<Step> sharedParameters;

    
    private Map<Integer, PortletPublishingDefinition> cpdCache;
    private IPortletTypeRegistry portletTypeRegistry;
    private ResourceLoader resourceLoader;
    
    
    public XmlChannelPublishingDefinitionDao() throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(PortletPublishingDefinition.class);
        unmarshaller = context.createUnmarshaller();
    }
    
    @javax.annotation.Resource(name="cpdCache")
    public void setCpdCache(Map<Integer, PortletPublishingDefinition> cpdCache) {
        this.cpdCache = cpdCache;
    }
    
    @Autowired(required = true)
    public void setPortletTypeRegistry(IPortletTypeRegistry portletTypeRegistry) {
        this.portletTypeRegistry = portletTypeRegistry;
    }


    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        this.cpdCreator = new CpdCreator(this.cpdCache);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.portletadmin.xmlsupport.IChannelPublishingDefinitionDao#getChannelPublishingDefinition(int)
     */
    @Override
    public PortletPublishingDefinition getChannelPublishingDefinition(int channelTypeId)  {
        return this.cpdCreator.get(channelTypeId);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.portletadmin.xmlsupport.IChannelPublishingDefinitionDao#getChannelPublishingDefinitions()
     */
    @Override
    public Map<IPortletType, PortletPublishingDefinition> getChannelPublishingDefinitions() {
        final List<IPortletType> channelTypes = this.portletTypeRegistry.getPortletTypes();
        
        final Map<IPortletType, PortletPublishingDefinition> cpds = new LinkedHashMap<IPortletType, PortletPublishingDefinition>(channelTypes.size());
        
        for (final IPortletType channelType : channelTypes) {
            final PortletPublishingDefinition cpd = this.getChannelPublishingDefinition(channelType.getId());
            cpds.put(channelType, cpd);
        }
        
        return cpds;
    }
    
    private PortletPublishingDefinition loadChannelPublishingDefinition(int channelTypeId)  {
        // if the CPD is not already in the cache, determine the CPD URI
        final String cpdUri;
        if (channelTypeId >= 0) {
            final IPortletType type = this.portletTypeRegistry.getPortletType(channelTypeId);
            if (type == null) {
                throw new IllegalArgumentException("No ChannelType registered with id: " + channelTypeId);
            }
            cpdUri = type.getCpdUri();
        }
        else {
            cpdUri = CUSTOM_CPD_PATH;
        }
        
        // read and parse the CPD
        final PortletPublishingDefinition def;
        final Resource cpdResource = this.resourceLoader.getResource("classpath:" + cpdUri);
        if (!cpdResource.exists()) {
            throw new MissingResourceException("Failed to find CPD '" + cpdUri + "' for channel type " + channelTypeId, this.getClass().getName(), cpdUri);
        }
        
        final InputStream cpdStream;
        try {
            cpdStream = cpdResource.getInputStream();
        }
        catch (IOException e) {
            throw new MissingResourceException("Failed to load CPD '" + cpdUri + "' for channel type " + channelTypeId, this.getClass().getName(), cpdUri);
        }
        
        try {
            def = (PortletPublishingDefinition) this.unmarshaller.unmarshal(cpdStream);
            final List<Step> sharedParameters = this.getSharedParameters();
            def.getSteps().addAll(sharedParameters);
            // add the CPD to the cache and return it
            this.cpdCache.put(channelTypeId, def);
            
            return def;
        } catch (JAXBException e) {
        }
        finally {
            IOUtils.closeQuietly(cpdStream);
        }
        
        return null;
        
    }

    private List<Step> getSharedParameters() {
        if (this.sharedParameters != null) {
            return this.sharedParameters;
        }
        
        // read and parse the shared CPD
        final Resource paramResource = this.resourceLoader.getResource("classpath:" + SHARED_PARAMETERS_PATH);
        if (!paramResource.exists()) {
            throw new MissingResourceException("Failed to find shared parameters CPD '" + SHARED_PARAMETERS_PATH + "'", this.getClass().getName(), SHARED_PARAMETERS_PATH);
        }
        
        final InputStream paramStream;
        try {
            paramStream = paramResource.getInputStream();
        }
        catch (IOException e) {
            throw new MissingResourceException("Failed to load CPD '" + SHARED_PARAMETERS_PATH + "'", this.getClass().getName(), SHARED_PARAMETERS_PATH);
        }
        
        // parse the shared CPD and add its steps to the end of the type-specific
        try {
            PortletPublishingDefinition config = (PortletPublishingDefinition) unmarshaller.unmarshal(paramStream);
            this.sharedParameters = config.getSteps();
        } catch (JAXBException e) {
            logger.warn("Failed to parse: " + paramResource, e); 
        }
        finally {
            IOUtils.closeQuietly(paramStream);
        }
        return this.sharedParameters;
    }
    
    private class CpdCreator extends MapCachingDoubleCheckedCreator<Integer, PortletPublishingDefinition> {
        public CpdCreator(Map<Integer, PortletPublishingDefinition> cache) {
            super(cache);
        }

        @Override
        protected PortletPublishingDefinition createInternal(Integer key, Object... args) {
            return loadChannelPublishingDefinition(key);
        }

        @Override
        protected Integer getKey(Object... args) {
            return (Integer)args[0];
        }
        
    }
}
