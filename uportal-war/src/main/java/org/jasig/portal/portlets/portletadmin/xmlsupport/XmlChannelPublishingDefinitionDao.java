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

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.IChannelRegistryStore;
import org.jasig.portal.channel.IChannelType;
import org.jasig.portal.utils.threading.MapCachingDoubleCheckedCreator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.thoughtworks.xstream.XStream;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class XmlChannelPublishingDefinitionDao implements IChannelPublishingDefinitionDao, ResourceLoaderAware, InitializingBean {
    private static final String CUSTOM_CPD_PATH = "/org/jasig/portal/portlets/portletadmin/CustomChannel.cpd";
    private static final String SHARED_PARAMETERS_PATH = "/org/jasig/portal/channels/SharedParameters.cpd";

    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private final XStream channelPublishingDefinitionParser = new XStream();
    
    private MapCachingDoubleCheckedCreator<Integer, ChannelPublishingDefinition> cpdCreator;
    private CPDParameterList sharedParameters;

    
    private Map<Integer, ChannelPublishingDefinition> cpdCache;
    private IChannelRegistryStore channelRegistryStore;
    private ResourceLoader resourceLoader;
    
    
    public XmlChannelPublishingDefinitionDao() {
        this.channelPublishingDefinitionParser.processAnnotations(new Class[] { ChannelPublishingDefinition.class, CPDParameterList.class });
    }
    
    public void setCpdCache(Map<Integer, ChannelPublishingDefinition> cpdCache) {
        this.cpdCache = cpdCache;
    }
    public void setChannelRegistryStore(IChannelRegistryStore channelRegistryStore) {
        this.channelRegistryStore = channelRegistryStore;
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
    public ChannelPublishingDefinition getChannelPublishingDefinition(int channelTypeId)  {
        return this.cpdCreator.get(channelTypeId);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.portletadmin.xmlsupport.IChannelPublishingDefinitionDao#getChannelPublishingDefinitions()
     */
    @Override
    public Map<IChannelType, ChannelPublishingDefinition> getChannelPublishingDefinitions() {
        final List<IChannelType> channelTypes = this.channelRegistryStore.getChannelTypes();
        
        final Map<IChannelType, ChannelPublishingDefinition> cpds = new LinkedHashMap<IChannelType, ChannelPublishingDefinition>(channelTypes.size());
        
        for (final IChannelType channelType : channelTypes) {
            final ChannelPublishingDefinition cpd = this.getChannelPublishingDefinition(channelType.getId());
            cpds.put(channelType, cpd);
        }
        
        return cpds;
    }
    
    private ChannelPublishingDefinition loadChannelPublishingDefinition(int channelTypeId)  {
        // if the CPD is not already in the cache, determine the CPD URI
        final String cpdUri;
        if (channelTypeId >= 0) {
            final IChannelType type = this.channelRegistryStore.getChannelType(channelTypeId);
            if (type == null) {
                throw new IllegalArgumentException("No ChannelType registered with id: " + channelTypeId);
            }
            cpdUri = type.getCpdUri();
        }
        else {
            cpdUri = CUSTOM_CPD_PATH;
        }
        
        // read and parse the CPD
        final ChannelPublishingDefinition def;
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
            def = (ChannelPublishingDefinition) this.channelPublishingDefinitionParser.fromXML(cpdStream);
        }
        finally {
            IOUtils.closeQuietly(cpdStream);
        }
        
        
        final CPDParameterList sharedParameters = this.getSharedParameters();
        
        final CPDParameterList params = def.getParams();
        final List<CPDStep> steps = params.getSteps();
        int stepId = steps.size();
        for (CPDStep step : sharedParameters.getSteps()) {
            stepId = stepId++;
            step.setId(String.valueOf(stepId));
        }
        
        steps.addAll(sharedParameters.getSteps());
        
        // add the CPD to the cache and return it
        this.cpdCache.put(channelTypeId, def);
        
        return def;
    }

    private CPDParameterList getSharedParameters() {
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
            this.sharedParameters = (CPDParameterList) this.channelPublishingDefinitionParser.fromXML(paramStream);
        }
        finally {
            IOUtils.closeQuietly(paramStream);
        }
        return this.sharedParameters;
    }
    
    private class CpdCreator extends MapCachingDoubleCheckedCreator<Integer, ChannelPublishingDefinition> {
        public CpdCreator(Map<Integer, ChannelPublishingDefinition> cache) {
            super(cache);
        }

        @Override
        protected ChannelPublishingDefinition createInternal(Integer key, Object... args) {
            return loadChannelPublishingDefinition(key);
        }

        @Override
        protected Integer getKey(Object... args) {
            return (Integer)args[0];
        }
        
    }
}
