/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.node;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import org.jasig.portal.PortalException;
import org.jasig.portal.properties.PropertiesManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * A class managing information contained in a user layout channel node.
 *
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version 1.0
 */
public class UserLayoutChannelDescription extends UserLayoutNodeDescription implements IUserLayoutChannelDescription  {

    Hashtable parameters;
    Hashtable override;

    String title=null;
    String description=null;
    String className=null;
    String channelPublishId=null;
    String channelTypeId=null;
    String functionalName=null;
    long timeout=-1;
    boolean editable=false;
    boolean hasHelp=false;
    boolean hasAbout=false;
    boolean isSecure=false;
    
    public UserLayoutChannelDescription() {
        parameters=new Hashtable();
        override=new Hashtable();
    }

    public UserLayoutChannelDescription(IUserLayoutChannelDescription d) {
        this();
        this.title=d.getTitle();
        this.description=d.getDescription();
        this.className=d.getClassName();
        this.channelPublishId=d.getChannelPublishId();
        this.channelTypeId=d.getChannelTypeId();
        this.functionalName=d.getFunctionalName();
        this.timeout=d.getTimeout();
        this.editable=d.isEditable();
        this.hasHelp=d.hasHelp();
        this.hasAbout=d.hasAbout();

        for(Enumeration enum = d.getParameterNames(); enum.hasMoreElements();) {
            String pName=(String)enum.nextElement();
            this.setParameterValue(pName,d.getParameterValue(pName));
            this.setParameterOverride(pName,d.getParameterOverrideValue(pName));
        }
    }

    /**
     * Reconstruct channel information from an xml <code>Element</code>
     *
     * @param xmlNode a user layout channel <code>Element</code> value
     * @exception PortalException if xml is malformed
     */
    public UserLayoutChannelDescription(Element xmlNode) throws PortalException {
        this();
        if(!xmlNode.getNodeName().equals("channel")) {
            throw new PortalException("Given XML Element is not a channel!");
        }

        // could do some validation here, but this code will probably go away anyhow

        // standard Node attributes
        this.setId(xmlNode.getAttribute("ID"));
        this.setName(xmlNode.getAttribute("name"));
        this.setUnremovable((new Boolean(xmlNode.getAttribute("unremovable"))).booleanValue());
        this.setImmutable((new Boolean(xmlNode.getAttribute("immutable"))).booleanValue());

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
        for(Node n=xmlNode.getFirstChild(); n!=null;n=n.getNextSibling()) {
            if(n.getNodeType()==Node.ELEMENT_NODE) {
                Element e=(Element) n;
                if(e.getNodeName().equals("parameter")) {
                    // get parameter name and value
                    String pName=e.getAttribute("name");
                    String pValue=e.getAttribute("value");

                    Boolean canOverride=new Boolean(false);
                    String str_override=e.getAttribute("override");
                    if(str_override!=null && str_override.equals("yes")) {
                        canOverride=new Boolean(true);
                    }

                    if(pName!=null && pValue!=null) {
                        this.setParameterValue(pName,pValue);
                        this.setParameterOverride(pName,canOverride.booleanValue());
                    }
                }
            }
        }
    }

    /**
     * Determine if the channel supports "about" action.
     * @return value of hasAbout.
     */
    public boolean hasAbout() {
        return hasAbout;
    }

    /**
     * Specify whether the channel supports "about" action.
     * @param v  Value to assign to hasAbout.
     */
    public void setHasAbout(boolean  v) {
        this.hasAbout = v;
    }

    /**
     * Determine if the channel supports "help" action.
     * @return value of hasHelp.
     */
    public boolean hasHelp() {
        return hasHelp;
    }

    /**
     * Specify whether the channel supports "help" action.
     * @param v  Value to assign to hasHelp.
     */
    public void setHasHelp(boolean  v) {
        this.hasHelp = v;
    }

    /**
     * Determine if the channel is editable.
     * @return value of editable.
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * Specify whether the channel is editable.
     * @param v  Value to assign to editable.
     */
    public void setEditable(boolean  v) {
        this.editable = v;
    }

    /**
     * Get the value of channel timeout in milliseconds.
     * @return value of timeout.
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * Set the value of channel timeout in milliseconds.
     * @param v  Value to assign to timeout.
     */
    public void setTimeout(long  v) {
        this.timeout = v;
    }

    /**
     * Get the value of secure setting.
     * @return value of secure.
     */    
    public boolean isSecure(){
        return isSecure;
    }

    /**
     * Set the value of channel secure setting.
     * @param secure  Value to assign to secure
     */    
    public void setIsSecure(boolean secure){
        this.isSecure = secure;
    }
    
    
    /**
     * Get the channel type for portlet / not portlet
     * @return the channel type for portlet / not portlet
     */
    public boolean isPortlet() {
        if (this.className != null) {
            final String portletClassName = PropertiesManager.getProperty("org.jasig.portal.portletAdapter");
            
            if (this.className.equals(portletClassName)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Get the value of functionalName.
     * @return value of functionalName.
     */
    public String getFunctionalName() {
        return functionalName;
    }

    /**
     * Set the value of functionalName.
     * @param v  Value to assign to functionalName.
     */
    public void setFunctionalName(String  v) {
        this.functionalName = v;
    }

    /**
     * Get the value of channelSubscribeId.
     * @return value of channelSubscribeId.
     */
    public String getChannelSubscribeId() {
        return super.getId();
    }

    /**
     * Set the value of channelSubscribeId.
     * @param v  Value to assign to channelSubscribeId.
     */
    public void setChannelSubscribeId(String  v) {
        super.setId(v);
    }

    /**
     * Get the value of channelTypeId.
     * @return value of channelTypeId.
     */
    public String getChannelTypeId() {
        return channelTypeId;
    }

    /**
     * Set the value of channelTypeId.
     * @param v  Value to assign to channelTypeId.
     */
    public void setChannelTypeId(String  v) {
        this.channelTypeId = v;
    }

    /**
     * Get the value of channelPublishId for this channel.
     * @return value of channelPublishId.
     */
    public String getChannelPublishId() {
        return channelPublishId;
    }

    /**
     * Set the value of channelPublishId for this channel.
     * @param v  Value to assign to channelPublishId.
     */
    public void setChannelPublishId(String  v) {
        this.channelPublishId = v;
    }

    /**
     * Get the value of className implementing this channel.
     * @return value of className.
     */
    public String getClassName() {
        return className;
    }

    /**
     * Set the value of className implementing this channel.
     * @param v  Value to assign to className.
     */
    public void setClassName(String  v) {
        this.className = v;
    }

    /**
     * Get the value of title.
     * @return value of title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the value of title.
     * @param v  Value to assign to title.
     */
    public void setTitle(String  v) {
        this.title = v;
    }



    /**
     * Get the value of description.
     * @return value of description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the value of description.
     * @param v  Value to assign to description.
     */
    public void setDescription(String  v) {
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
    public String setParameterValue(String parameterName, String parameterValue) {
        return (String) parameters.put(parameterName,parameterValue);
    }


    /**
     * Obtain a channel parameter value.
     *
     * @param parameterName a <code>String</code> value
     * @return a <code>String</code> value
     */
    public String getParameterValue(String parameterName) {
        return (String) parameters.get(parameterName);
    }

     /**
     * Obtain a channel parameter override value.
     *
     * @param parameterName a <code>String</code> value
     * @return a <code>boolean</code> value
     */
    public boolean getParameterOverrideValue(String parameterName) {
        Boolean boolValue = (Boolean)override.get(parameterName);
        if ( boolValue != null )
         return boolValue.booleanValue();
         return true;
    }

    /**
     * Obtain values of all existing channel parameters.
     *
     * @return a <code>Collection</code> of <code>String</code> parameter values.
     */
    public Collection getParameterValues()  {
        return parameters.values();
    }


    /**
     * Determines the number of existing channel parameters.
     *
     * @return an <code>int</code> value
     */
    public int numberOfParameters() {
        return parameters.size();
    }


    /**
     * Clears all of the channel parameters.
     *
     */
    public void clearParameters() {
        parameters.clear();
        override.clear();
    }

    /**
     * Determine if a given parameter can be overriden by the user.
     * (defaults to true)
     * @param parameterName a <code>String</code> value
     * @return a <code>boolean</code> value
     */
    public boolean canOverrideParameter(String parameterName) {
        Boolean ov=(Boolean) override.get(parameterName);
        if(ov!=null) {
            return ov.booleanValue();
        } else {
            return false;
        }
    }

    /**
     * Set parameter override flag.
     *
     * @param parameterName a <code>String</code> value
     * @param canOverride a <code>boolean</code> flag.
     */
    public void setParameterOverride(String parameterName, boolean canOverride) {
        if(parameters.get(parameterName)!=null) {
            this.override.put(parameterName,new Boolean(canOverride));
        }
    }


    /**
     * Remove a channel parameter.
     *
     * @param parameterName a <code>String</code> parameter name.
     * @return an old parameter value.
     */
    public String remove(String parameterName) {
        return (String) parameters.remove(parameterName);
    }

    /**
     * Obtain a set of channel parameter names.
     *
     * @return a <code>Set</code> of <code>String</code> parameter names.
     */
    public Enumeration getParameterNames() {
        return parameters.keys();
    }

    /**
     * Returns an entire mapping of parameters.
     *
     * @return a <code>Map</code> of parameter names on parameter values.
     */
    public Map getParameterMap() {
        return Collections.unmodifiableMap(parameters);
    }

    /**
     * Determine if the channel has any parameters.
     *
     * @return a <code>boolean</code> value
     */
    public boolean hasParameters() {
        return !parameters.isEmpty();
    }

    /**
     * Determines if a certain parameter name is present.
     *
     * @param parameterName a <code>String</code> parameter name.
     * @return a <code>boolean</code> value
     */
    public boolean containsParameter(String parameterName) {
        return parameters.containsKey(parameterName);
    }

    /**
     * Creates a <code>org.w3c.dom.Element</code> representation of the current channel.
     *
     * @param root a <code>Document</code> for which the <code>Element</code> should be created.
     * @return a <code>Node</code> value
     */
    public Element getXML(Document root) {
        Element node=root.createElement("channel");
        this.addNodeAttributes(node);
        this.addParameterChildren(node,root);
        return node;
    }

    public void addParameterChildren(Element node, Document root) {
        for(Enumeration enum = this.getParameterNames(); enum.hasMoreElements();) {
            Element pElement=root.createElement("parameter");
            String pName=(String)enum.nextElement();
            pElement.setAttribute("name",pName);
            pElement.setAttribute("value",getParameterValue(pName));
            pElement.setAttribute("override",getParameterOverrideValue(pName) ? "yes" : "no");
            node.appendChild(pElement);
        }
    }

    public void addNodeAttributes(Element node) {
        super.addNodeAttributes(node);
        node.setAttribute("title",this.getTitle());
        node.setAttribute("description",this.getDescription());
        node.setAttribute("class",this.getClassName());
        node.setAttribute("chanID",this.getChannelPublishId());
        node.setAttribute("typeID",this.getChannelTypeId());
        node.setAttribute("fname",this.getFunctionalName());
        node.setAttribute("timeout",Long.toString(this.getTimeout()));
        node.setAttribute("editable",(new Boolean(this.isEditable())).toString());
        node.setAttribute("hasHelp",(new Boolean(this.hasHelp())).toString());
        node.setAttribute("hasAbout",(new Boolean(this.hasAbout())).toString());
        node.setAttribute("secure",(new Boolean(this.isSecure())).toString());        
    }

    /**
     * Returns a type of the node, could be FOLDER or CHANNEL integer constant.
     *
     * @return a type
     */
    public int getType() {
      return CHANNEL;
    }


}
