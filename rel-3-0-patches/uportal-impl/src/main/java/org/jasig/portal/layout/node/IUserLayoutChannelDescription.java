/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.node;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;

import org.jasig.portal.PortalException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;



/**
 * An interface managing information contained in a user layout channel node.
 *
 * @author Peter Kharchenko  {@link <a href="mailto:pkharchenko@interactivebusiness.com"">pkharchenko@interactivebusiness.com"</a>}
 * @version 1.0
 */
public interface IUserLayoutChannelDescription extends IUserLayoutNodeDescription {

    /**
     * Determine if the channel supports "about" action.
     * @return value of hasAbout.
     */
    public boolean hasAbout();

    /**
     * Specify whether the channel supports "about" action.
     * @param v  Value to assign to hasAbout.
     */
    public void setHasAbout(boolean  v);

    /**
     * Determine if the channel supports "help" action.
     * @return value of hasHelp.
     */
    public boolean hasHelp();

    /**
     * Specify whether the channel supports "help" action.
     * @param v  Value to assign to hasHelp.
     */
    public void setHasHelp(boolean  v);

    /**
     * Determine if the channel is editable.
     * @return value of editable.
     */
    public boolean isEditable();

    /**
     * Specify whether the channel is editable.
     * @param v  Value to assign to editable.
     */
    public void setEditable(boolean  v);

    /**
     * Get the value of channel timeout in milliseconds.
     * @return value of timeout.
     */
    public long getTimeout();

    /**
     * Set the value of channel timeout in milliseconds.
     * @param v  Value to assign to timeout.
     */
    public void setTimeout(long  v);


    /**
     * Get the value of functionalName.
     * @return value of functionalName.
     */
    public String getFunctionalName();

    /**
     * Set the value of functionalName.
     * @param v  Value to assign to functionalName.
     */
    public void setFunctionalName(String  v);

    /**
     * Get the value of channelSubscribeId.
     * @return value of channelSubscribeId.
     */
    public String getChannelSubscribeId();

    /**
     * Set the value of channelSubscribeId.
     * @param v  Value to assign to channelSubscribeId.
     */
    public void setChannelSubscribeId(String  v);

    /**
     * Get the value of channelTypeId.
     * @return value of channelTypeId.
     */
    public String getChannelTypeId();

    /**
     * Set the value of channelTypeId.
     * @param v  Value to assign to channelTypeId.
     */
    public void setChannelTypeId(String  v);

    /**
     * Get the value of channelPublishId for this channel.
     * @return value of channelPublishId.
     */
    public String getChannelPublishId();

    /**
     * Set the value of channelPublishId for this channel.
     * @param v  Value to assign to channelPublishId.
     */
    public void setChannelPublishId(String  v);

    /**
     * Get the value of className implementing this channel.
     * @return value of className.
     */
    public String getClassName();

    /**
     * Set the value of className implementing this channel.
     * @param v  Value to assign to className.
     */
    public void setClassName(String  v);

    /**
     * Get the value of title.
     * @return value of title.
     */
    public String getTitle();

    /**
     * Set the value of title.
     * @param v  Value to assign to title.
     */
    public void setTitle(String  v);



    /**
     * Get the value of description.
     * @return value of description.
     */
    public String getDescription();

    /**
     * Set the value of description.
     * @param v  Value to assign to description.
     */
    public void setDescription(String  v);

    /**
     * Get the value of secure.
     * @return value of secure.
     */
    public boolean isSecure();

    /**
     * Set the value of secure.
     * @param v  Value to assign to secure.
     */
    public void setIsSecure(boolean  v);

    /**
     * Return true if the described channel is a JSR-168 portlet, false
     * otherwise.
     * @return true if the described channel is a JSR-168 portlet, 
     * false otherwise
     */
    public boolean isPortlet();    
    
    // channel parameter methods

    /**
     * Set a channel parameter value.
     *
     * @param parameterValue a <code>String</code> value
     * @param parameterName a <code>String</code> value
     * @return a <code>String</code> value that was set.
     */
    public String setParameterValue(String parameterName, String parameterValue);


    /**
     * Reset a channel parameter value. Since parameter changes by channels
     * can be persisted if override is allowed this method enables resetting to
     * the original value or, if the parameter is ad-hoc meaning that the
     * channel definition does not provide a value for this parameter, then the
     * parameter value is removed.
     *
     * @param parameterName a <code>String</code> value
     * @throws PortalException
     */
    public void resetParameter(String parameterName) throws PortalException;


    /**
     * Obtain a channel parameter value.
     *
     * @param parameterName a <code>String</code> value
     * @return a <code>String</code> value
     */
    public String getParameterValue(String parameterName);

     /**
     * Obtain a channel parameter override value.
     *
     * @param parameterName a <code>String</code> value
     * @return a <code>boolean</code> value
     */
    public boolean getParameterOverrideValue(String parameterName);

    /**
     * Obtain values of all existing channel parameters.
     *
     * @return a <code>Collection</code> of <code>String</code> parameter values.
     */
    public Collection getParameterValues();

    /**
     * Determines the number of existing channel parameters.
     *
     * @return an <code>int</code> value
     */
    public int numberOfParameters();

    /**
     * Clears all of the channel parameters.
     *
     */
    public void clearParameters();

    /**
     * Determine if a given parameter can be overriden by the user.
     * (defaults to true)
     * @param parameterName a <code>String</code> value
     * @return a <code>boolean</code> value
     */
    public boolean canOverrideParameter(String parameterName);

    /**
     * Set parameter override flag.
     *
     * @param parameterName a <code>String</code> value
     * @param canOverride a <code>boolean</code> flag.
     */
    public void setParameterOverride(String parameterName, boolean canOverride);

    /**
     * Remove a channel parameter.
     *
     * @param parameterName a <code>String</code> parameter name.
     * @return an old parameter value.
     */
    public String remove(String parameterName);

    /**
     * Obtain a set of channel parameter names.
     *
     * @return a <code>Set</code> of <code>String</code> parameter names.
     */
    public Enumeration getParameterNames();

    /**
     * Returns an entire mapping of parameters.
     *
     * @return a <code>Map</code> of parameter names on parameter values.
     */
    public Map getParameterMap();

    /**
     * Determine if the channel has any parameters.
     *
     * @return a <code>boolean</code> value
     */
    public boolean hasParameters();

    /**
     * Determines if a certain parameter name is present.
     *
     * @param parameterName a <code>String</code> parameter name.
     * @return a <code>boolean</code> value
     */
    public boolean containsParameter(String parameterName);

    /**
     * Creates a <code>org.w3c.dom.Element</code> representation of the current channel.
     *
     * @param root a <code>Document</code> for which the <code>Element</code> should be created.
     * @return a <code>Node</code> value
     */
    public Element getXML(Document root);

};
