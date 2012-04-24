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

package org.jasig.portal.rendering;

import java.util.Map;

import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.om.IStylesheetDescriptor;
import org.jasig.portal.layout.om.IStylesheetParameterDescriptor;
import org.jasig.portal.portlet.PortletUtils;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.rendering.IPortletRenderer;
import org.jasig.portal.url.IPortalRequestInfo;
import org.jasig.portal.url.IUrlSyntaxProvider;
import org.jasig.portal.utils.Tuple;
import org.jasig.portal.utils.cache.CacheKey;
import org.jasig.portal.xml.stream.FilteringXMLEventReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Enforces a specific WindowState based on the "dashboardForcedWindowState" {@link IStylesheetDescriptor} parameter. For renderings
 * where a specific portlet is not specified and the stylesheet descriptor specifies a dashboardForcedWindowState then all portlets
 * in the pipeline will have their window state set to the specified value. If the IStylesheetDescriptor is not specified a check is
 * made to ensure the window states are not {@link WindowState#MAXIMIZED}, {@link IPortletRenderer#DETACHED}, or 
 * {@link IPortletRenderer#EXCLUSIVE} 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class WindowStateSettingsStAXComponent extends StAXPipelineComponentWrapper {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private IUrlSyntaxProvider urlSyntaxProvider;
    private IPortletWindowRegistry portletWindowRegistry;
    private StylesheetAttributeSource stylesheetAttributeSource;

    @Autowired
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }

    @Autowired
    public void setUrlSyntaxProvider(IUrlSyntaxProvider urlSyntaxProvider) {
        this.urlSyntaxProvider = urlSyntaxProvider;
    }

    public void setStylesheetAttributeSource(StylesheetAttributeSource stylesheetAttributeSource) {
        this.stylesheetAttributeSource = stylesheetAttributeSource;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.PipelineComponent#getCacheKey(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
        //Initiating rendering of portlets will change the stream at all
        return this.wrappedComponent.getCacheKey(request, response);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.PipelineComponent#getEventReader(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public PipelineEventReader<XMLEventReader, XMLEvent> getEventReader(HttpServletRequest request, HttpServletResponse response) {
        final PipelineEventReader<XMLEventReader, XMLEvent> pipelineEventReader = this.wrappedComponent.getEventReader(request, response);

        final XMLEventReader eventReader = pipelineEventReader.getEventReader();
        
        final IStylesheetDescriptor stylesheetDescriptor = stylesheetAttributeSource.getStylesheetDescriptor(request);
        
        final IPortalRequestInfo portalRequestInfo = this.urlSyntaxProvider.getPortalRequestInfo(request);
        
        final XMLEventReader filteredEventReader;
        if (portalRequestInfo.getTargetedPortletWindowId() == null) {
            final IStylesheetParameterDescriptor defaultWindowStateParam = stylesheetDescriptor.getStylesheetParameterDescriptor("dashboardForcedWindowState");
            if (defaultWindowStateParam != null) {
                //Set all window states to the specified default
                final WindowState windowState = PortletUtils.getWindowState(defaultWindowStateParam.getDefaultValue());
                filteredEventReader = new SinglePortletWindowStateSettingXMLEventReader(request, eventReader, windowState);
            }
            else {
                //Make sure there aren't any portlets in a "targeted" window state
                filteredEventReader = new NonTargetedPortletWindowStateSettingXMLEventReader(request, eventReader);
            }
        }
        else {
            //Not mobile, don't bother filtering
            filteredEventReader = eventReader;
        }
        
        final Map<String, String> outputProperties = pipelineEventReader.getOutputProperties();
        return new PipelineEventReaderImpl<XMLEventReader, XMLEvent>(filteredEventReader, outputProperties);
    }

    private abstract class PortletWindowStateSettingXMLEventReader extends FilteringXMLEventReader {
        private final HttpServletRequest request;
        
        public PortletWindowStateSettingXMLEventReader(HttpServletRequest request, XMLEventReader reader) {
            super(reader);
            this.request = request;
        }

        @Override
        protected final XMLEvent filterEvent(XMLEvent event, boolean peek) {
            if (event.isStartElement()) {
                final StartElement startElement = event.asStartElement();
                
                final QName name = startElement.getName();
                final String localName = name.getLocalPart();
                if (IUserLayoutManager.CHANNEL.equals(localName) || IUserLayoutManager.CHANNEL_HEADER.equals(localName)) {
                    final Tuple<IPortletWindow, StartElement> portletWindowAndElement = portletWindowRegistry.getPortletWindow(request, startElement);
                    if (portletWindowAndElement == null) {
                        logger.warn("No portlet window found for: {}", localName);
                        return event;
                    }
                    
                    final IPortletWindow portletWindow = portletWindowAndElement.first;
                    
                    final WindowState windowState = getWindowState(portletWindow);
                    
                    //Set the portlet's state, skip if the state is already correct
                    final WindowState currentWindowState = portletWindow.getWindowState();
                    if (!windowState.equals(currentWindowState)) {
                        portletWindow.setWindowState(windowState);
                        
                        if (logger.isDebugEnabled()) {
                            logger.debug("Changing WindowState from {} to {} for {}", new Object[] {
                                    currentWindowState, windowState, portletWindow });
                        }
                        
                        portletWindowRegistry.storePortletWindow(request, portletWindow);
                    }
                    
                    return portletWindowAndElement.second;
                }
            } 
            
            return event;
        }

        protected abstract WindowState getWindowState(IPortletWindow portletWindow);
    }

    private class SinglePortletWindowStateSettingXMLEventReader extends PortletWindowStateSettingXMLEventReader {
        private final WindowState state;
        
        public SinglePortletWindowStateSettingXMLEventReader(HttpServletRequest request, XMLEventReader reader, WindowState state) {
            super(request, reader);
            this.state = state;
        }

        @Override
        protected WindowState getWindowState(IPortletWindow portletWindow) {
            return state;
        }
    }

    private class NonTargetedPortletWindowStateSettingXMLEventReader extends PortletWindowStateSettingXMLEventReader {
        
        public NonTargetedPortletWindowStateSettingXMLEventReader(HttpServletRequest request, XMLEventReader reader) {
            super(request, reader);
        }

        @Override
        protected WindowState getWindowState(IPortletWindow portletWindow) {
            final WindowState windowState = portletWindow.getWindowState();
            if (windowState != null && PortletUtils.isTargetedWindowState(windowState)) {
                return WindowState.NORMAL;
            }
            return windowState;
        }
    }
}
