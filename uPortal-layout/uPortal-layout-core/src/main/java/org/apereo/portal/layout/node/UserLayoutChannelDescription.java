/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.layout.node;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.portal.PortalException;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletDefinitionParameter;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.spring.locator.ApplicationContextLocator;
import org.apereo.portal.utils.personalize.IPersonalizer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/** A class managing information contained in a user layout channel node. */
public class UserLayoutChannelDescription extends UserLayoutNodeDescription
        implements IUserLayoutChannelDescription {

    Hashtable parameters = new Hashtable();

    private String title = null;
    private String description = null;
    private String className = null;
    private String channelPublishId = null;
    private String channelTypeId = null;
    private String functionalName = null;
    private long timeout = -1;
    private boolean editable = false;
    private boolean hasHelp = false;
    private boolean hasAbout = false;
    private boolean isSecure = false;
    private IPersonalizer personalizer;

    public UserLayoutChannelDescription() {
        super();
    }

    /**
     * Construct channel information from a Channel Definition object.
     *
     * @param person personalization details
     * @param definition
     * @param session lightweight caching
     */
    public UserLayoutChannelDescription(
            IPerson person, IPortletDefinition definition, HttpSession session) {
        this.personalizer =
                ApplicationContextLocator.getApplicationContext().getBean(IPersonalizer.class);

        this.title = personalizer.personalize(person, definition.getTitle(), session);
        this.name = definition.getName();
        this.description = personalizer.personalize(person, definition.getDescription(), session);
        this.channelPublishId = String.valueOf(definition.getPortletDefinitionId().getStringId());
        this.channelTypeId = String.valueOf(definition.getType().getId());
        this.functionalName = definition.getFName();
        this.timeout = definition.getTimeout();

        for (IPortletDefinitionParameter param : definition.getParameters()) {
            this.setParameterValue(
                    param.getName(), personalizer.personalize(person, param.getValue(), session));
        }
    }

    /**
     * Reconstruct channel information from an xml <code>Element</code>
     *
     * @param xmlNode a user layout channel <code>Element</code> value
     * @exception PortalException if xml is malformed
     */
    public UserLayoutChannelDescription(Element xmlNode) throws PortalException {
        super(xmlNode);

        if (!xmlNode.getNodeName().equals("channel")) {
            throw new PortalException("Given XML Element is not a channel!");
        }

        // channel-specific attributes
        this.setTitle(xmlNode.getAttribute("title"));
        this.setDescription(xmlNode.getAttribute("description"));
        this.setClassName(xmlNode.getAttribute("class"));
        this.setChannelPublishId(xmlNode.getAttribute("chanID"));
        this.setChannelTypeId(xmlNode.getAttribute("typeID"));
        this.setFunctionalName(xmlNode.getAttribute("fname"));
        this.setTimeout(Long.parseLong(xmlNode.getAttribute("timeout")));
        this.setEditable(Boolean.valueOf(xmlNode.getAttribute("editable")).booleanValue());
        this.setHasHelp(Boolean.valueOf(xmlNode.getAttribute("hasHelp")).booleanValue());
        this.setHasAbout(Boolean.valueOf(xmlNode.getAttribute("hasAbout")).booleanValue());
        this.setIsSecure(Boolean.valueOf(xmlNode.getAttribute("secure")).booleanValue());

        // process parameter elements
        for (Node n = xmlNode.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) n;
                if (e.getNodeName().equals("parameter")) {
                    // get parameter name and value
                    String pName = e.getAttribute("name");
                    String pValue = e.getAttribute("value");

                    if (pName != null && pValue != null) {
                        this.setParameterValue(pName, pValue);
                    }
                }
            }
        }
    }

    /**
     * Determine if the channel supports "about" action.
     *
     * @return value of hasAbout.
     */
    @Override
    public boolean hasAbout() {
        return hasAbout;
    }

    /**
     * Specify whether the channel supports "about" action.
     *
     * @param v Value to assign to hasAbout.
     */
    @Override
    public void setHasAbout(boolean v) {
        this.hasAbout = v;
    }

    /**
     * Determine if the channel supports "help" action.
     *
     * @return value of hasHelp.
     */
    @Override
    public boolean hasHelp() {
        return hasHelp;
    }

    /**
     * Specify whether the channel supports "help" action.
     *
     * @param v Value to assign to hasHelp.
     */
    @Override
    public void setHasHelp(boolean v) {
        this.hasHelp = v;
    }

    /**
     * Determine if the channel is editable.
     *
     * @return value of editable.
     */
    @Override
    public boolean isEditable() {
        return editable;
    }

    /**
     * Specify whether the channel is editable.
     *
     * @param v Value to assign to editable.
     */
    @Override
    public void setEditable(boolean v) {
        this.editable = v;
    }

    /**
     * Get the value of channel timeout in milliseconds.
     *
     * @return value of timeout.
     */
    @Override
    public long getTimeout() {
        return timeout;
    }

    /**
     * Set the value of channel timeout in milliseconds.
     *
     * @param v Value to assign to timeout.
     */
    @Override
    public void setTimeout(long v) {
        this.timeout = v;
    }

    /**
     * Get the value of secure setting.
     *
     * @return value of secure.
     */
    @Override
    public boolean isSecure() {
        return isSecure;
    }

    /**
     * Set the value of channel secure setting.
     *
     * @param secure Value to assign to secure
     */
    @Override
    public void setIsSecure(boolean secure) {
        this.isSecure = secure;
    }

    /**
     * Get the channel type for portlet / not portlet
     *
     * @return the channel type for portlet / not portlet
     */
    @Override
    public boolean isPortlet() {
        return true;
    }

    /**
     * Get the value of functionalName.
     *
     * @return value of functionalName.
     */
    @Override
    public String getFunctionalName() {
        return functionalName;
    }

    /**
     * Set the value of functionalName.
     *
     * @param v Value to assign to functionalName.
     */
    @Override
    public void setFunctionalName(String v) {
        this.functionalName = v;
    }

    /**
     * Get the value of channelSubscribeId.
     *
     * @return value of channelSubscribeId.
     */
    @Override
    public String getChannelSubscribeId() {
        return super.getId();
    }

    /**
     * Set the value of channelSubscribeId.
     *
     * @param v Value to assign to channelSubscribeId.
     */
    @Override
    public void setChannelSubscribeId(String v) {
        super.setId(v);
    }

    /**
     * Get the value of channelTypeId.
     *
     * @return value of channelTypeId.
     */
    @Override
    public String getChannelTypeId() {
        return channelTypeId;
    }

    /**
     * Set the value of channelTypeId.
     *
     * @param v Value to assign to channelTypeId.
     */
    @Override
    public void setChannelTypeId(String v) {
        this.channelTypeId = v;
    }

    /**
     * Get the value of channelPublishId for this channel.
     *
     * @return value of channelPublishId.
     */
    @Override
    public String getChannelPublishId() {
        return channelPublishId;
    }

    /**
     * Set the value of channelPublishId for this channel.
     *
     * @param v Value to assign to channelPublishId.
     */
    @Override
    public void setChannelPublishId(String v) {
        this.channelPublishId = v;
    }

    /**
     * Get the value of className implementing this channel.
     *
     * @return value of className.
     */
    @Override
    public String getClassName() {
        return className;
    }

    /**
     * Set the value of className implementing this channel.
     *
     * @param v Value to assign to className.
     */
    @Override
    public void setClassName(String v) {
        this.className = v;
    }

    /**
     * Get the value of title.
     *
     * @return value of title.
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * Set the value of title.
     *
     * @param v Value to assign to title.
     */
    @Override
    public void setTitle(String v) {
        this.title = v;
    }

    /**
     * Get the value of description.
     *
     * @return value of description.
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Set the value of description.
     *
     * @param v Value to assign to description.
     */
    @Override
    public void setDescription(String v) {
        this.description = v;
    }

    // channel parameter methods

    /**
     * Set a channel parameter value.
     *
     * @param parameterValue a <code>String</code> value
     * @param parameterName a <code>String</code> value
     * @return a <code>String</code> value that was set.
     */
    @Override
    public String setParameterValue(String parameterName, String parameterValue) {
        // don't try to store a null value
        if (parameterValue == null) return null;
        return (String) parameters.put(parameterName, parameterValue);
    }

    /**
     * Obtain a channel parameter value.
     *
     * @param parameterName a <code>String</code> value
     * @return a <code>String</code> value
     */
    @Override
    public String getParameterValue(String parameterName) {
        return (String) parameters.get(parameterName);
    }

    /**
     * Obtain values of all existing channel parameters.
     *
     * @return a <code>Collection</code> of <code>String</code> parameter values.
     */
    @Override
    public Collection getParameterValues() {
        return parameters.values();
    }

    /**
     * Obtain a set of channel parameter names.
     *
     * @return a <code>Set</code> of <code>String</code> parameter names.
     */
    @Override
    public Enumeration getParameterNames() {
        return parameters.keys();
    }

    /**
     * Returns an entire mapping of parameters.
     *
     * @return a <code>Map</code> of parameter names on parameter values.
     */
    @Override
    public Map getParameterMap() {
        return Collections.unmodifiableMap(parameters);
    }

    /**
     * Creates a <code>org.w3c.dom.Element</code> representation of the current channel.
     *
     * @param root a <code>Document</code> for which the <code>Element</code> should be created.
     * @return a <code>Node</code> value
     */
    @Override
    public Element getXML(Document root) {
        Element node = root.createElement("channel");
        this.addNodeAttributes(node);
        this.addParameterChildren(node, root);
        return node;
    }

    private void addParameterChildren(Element node, Document root) {
        for (Enumeration enum1 = this.getParameterNames(); enum1.hasMoreElements(); ) {
            Element pElement = root.createElement("parameter");
            String pName = (String) enum1.nextElement();
            pElement.setAttribute("name", pName);
            pElement.setAttribute("value", getParameterValue(pName));
            node.appendChild(pElement);
        }
    }

    @Override
    public void addNodeAttributes(Element node) {
        super.addNodeAttributes(node);
        node.setAttribute("title", this.getTitle());
        node.setAttribute("name", this.getName());
        node.setAttribute("description", this.getDescription());
        node.setAttribute("class", this.getClassName());
        node.setAttribute("chanID", this.getChannelPublishId());
        node.setAttribute("typeID", this.getChannelTypeId());
        node.setAttribute("fname", this.getFunctionalName());
        node.setAttribute("timeout", String.valueOf(this.getTimeout()));
        node.setAttribute("editable", String.valueOf(this.isEditable()));
        node.setAttribute("hasHelp", String.valueOf(this.hasHelp()));
        node.setAttribute("hasAbout", String.valueOf(this.hasAbout()));
        node.setAttribute("secure", String.valueOf(this.isSecure()));
        node.setAttribute("isPortlet", String.valueOf(this.isPortlet()));
    }

    /**
     * Returns a type of the node, could be FOLDER or CHANNEL integer constant.
     *
     * @return a type
     */
    @Override
    public LayoutNodeType getType() {
        return LayoutNodeType.PORTLET;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("ID", this.id)
                .append("name", this.name)
                .append("channelPublishId", this.channelPublishId)
                .append("channelTypeId", this.channelTypeId)
                .append("nodeType", this.getType())
                .append("precedence", this.precedence)
                .append("moveAllowed", this.moveAllowed)
                .append("removable", !this.unremovable)
                .append("deleteAllowed", this.deleteAllowed)
                .append("immutable", this.immutable)
                .append("editAllowed", this.editAllowed)
                .append("precedence", this.precedence)
                .toString();
    }
}
