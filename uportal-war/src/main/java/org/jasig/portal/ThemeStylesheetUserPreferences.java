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

 package org.jasig.portal;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * User preferences for stylesheets performing theme transformation
 * @author Peter Kharchenko <a href="mailto:">pkharchenko@interactivebusiness.com</>a
 * @version $Revision$
 */

public class ThemeStylesheetUserPreferences extends StylesheetUserPreferences {
    
    private static final Log log = LogFactory.getLog(ThemeStylesheetUserPreferences.class);
    
    protected Hashtable<String, Integer> channelAttributeNumbers;
    protected Hashtable<String, List<String>> channelAttributeValues;
    protected ArrayList<String> defaultChannelAttributeValues;

    public ThemeStylesheetUserPreferences() {
        super();
        channelAttributeNumbers=new Hashtable<String, Integer>();
        channelAttributeValues=new Hashtable<String, List<String>>();
        defaultChannelAttributeValues=new ArrayList<String>();
    }

    public ThemeStylesheetUserPreferences(ThemeStylesheetUserPreferences ssup) {
        super(ssup);
        this.channelAttributeNumbers=new Hashtable<String, Integer>(ssup.channelAttributeNumbers);
        this.channelAttributeValues=new Hashtable<String, List<String>>(ssup.channelAttributeValues);
        this.defaultChannelAttributeValues=new ArrayList<String>(ssup.defaultChannelAttributeValues);
    }
    /**
     * Provides a copy of this object with all fields instantiated to reflect 
     * the values of this object. This allows subclasses to override to add
     * correct copying behavior for their added fields.
     * 
     * @return a copy of this object
     */
    public Object newInstance()
    {
        return new ThemeStylesheetUserPreferences(this);
    }

    public String getChannelAttributeValue(String channelSubscribeId,String attributeName) {
        Integer attributeNumber=channelAttributeNumbers.get(attributeName);
        if(attributeNumber==null) {
            log.error("ThemeStylesheetUserPreferences::getChannelAttributeValue() : Attempting to obtain a non-existing attribute \""+attributeName+"\".");
            return null;
        }
        String value=null;
        List l=channelAttributeValues.get(channelSubscribeId);
        if(l==null) {
	    //            log.debug("ThemeStylesheetUserPreferences::getChannelAttributeValue() : Attempting to obtain an attribute for a non-existing channel \""+channelSubscribeId+"\".");
	    // return null;
	    return defaultChannelAttributeValues.get(attributeNumber.intValue());
        } else {
            if(attributeNumber.intValue()<l.size()) {
                value=(String) l.get(attributeNumber.intValue());
            }
            if(value==null) {
                try {
                    value=defaultChannelAttributeValues.get(attributeNumber.intValue());
                } catch (IndexOutOfBoundsException e) {
                    log.error("ThemeStylesheetUserPreferences::getChannelAttributeValue() : internal error - attribute name is registered, but no default value is provided.");
                    return null;
                }
            }
        }
        return value;
    }

    /**
     * Returns channel attribute value only if it has been assigned specifically.
     * @param channelSubscribeId channel id
     * @param attributeName name of the attribute
     * @return attribute value or null if the value is determined by the attribute default
     */
    public String getDefinedChannelAttributeValue(String channelSubscribeId,String attributeName) {
        Integer attributeNumber=channelAttributeNumbers.get(attributeName);
        if(attributeNumber==null) {
            log.error("ThemeStylesheetUserPreferences::hasDefinedChannelAttributeValue() : Attempting to obtain a non-existing attribute \""+attributeName+"\".");
            return null;
        }
        List l=channelAttributeValues.get(channelSubscribeId);
        if(l==null) {
	    return null;
	} else {
	    if(attributeNumber.intValue()<l.size())
		return (String) l.get(attributeNumber.intValue());
	    else
		return null;
	}
    }

    // this should be modified to throw exceptions
    public void setChannelAttributeValue(String channelSubscribeId,String attributeName,String attributeValue) {
        Integer attributeNumber=channelAttributeNumbers.get(attributeName);
        if(attributeNumber==null) {
            log.error("ThemeStylesheetUserPreferences::setChannelAttribute() : Attempting to set a non-existing channel attribute \""+attributeName+"\".");
            return;
        }
        List<String> l=channelAttributeValues.get(channelSubscribeId);
        if(l==null)
            l=this.createChannel(channelSubscribeId);
        try {
            l.set(attributeNumber.intValue(),attributeValue);
        } catch (IndexOutOfBoundsException e) {
            // bring up the array to the right size
            for(int i=l.size();i<attributeNumber.intValue();i++) {
                l.add((String)null);
            }
            l.add(attributeValue);
        }
    }


    public void addChannelAttribute(String attributeName, String defaultValue) {
        if(channelAttributeNumbers.get(attributeName)!=null) {
            log.error("ThemeStylesheetUserPreferences::addChannelAttribute() : Attempting to re-add an existing channel attribute \""+attributeName+"\".");
        } else {
            channelAttributeNumbers.put(attributeName,new Integer(defaultChannelAttributeValues.size()));
            // append to the end of the default value array
            defaultChannelAttributeValues.add(defaultValue);
        }
    }

    public void setChannelAttributeDefaultValue(String attributeName, String defaultValue) {
        Integer attributeNumber=channelAttributeNumbers.get(attributeName);
        defaultChannelAttributeValues.set(attributeNumber.intValue(),defaultValue);
    }

    public void removeChannelAttribute(String attributeName) {
        Integer attributeNumber;
        if((attributeNumber=channelAttributeNumbers.get(attributeName))==null) {
            log.error("ThemeStylesheetUserPreferences::removeChannelAttribute() : Attempting to remove a non-existing channel attribute \""+attributeName+"\".");
        } else {
            channelAttributeNumbers.remove(attributeName);
            // do not touch the arraylists
        }
    }

    public Enumeration<String> getChannelAttributeNames() {
        return channelAttributeNumbers.keys();
    }

    public void addChannel(String channelSubscribeId) {
        // check if the channel is there. In general it might be ok to use this functon to default
        // all of the channel's parameters

        ArrayList<String> l=new ArrayList<String>(defaultChannelAttributeValues.size());

        if(channelAttributeValues.put(channelSubscribeId,l)!=null && log.isDebugEnabled())
            log.debug("ThemeStylesheetUserPreferences::addChannel() : Readding an existing channel (channelSubscribeId=\""+channelSubscribeId+"\"). All values will be set to default.");
    }

    public void removeChannel(String channelSubscribeId) {
        if(channelAttributeValues.remove(channelSubscribeId)==null && log.isDebugEnabled())
            log.error("ThemeStylesheetUserPreferences::removeChannel() : Attempting to remove an non-existing channel (channelSubscribeId=\""+channelSubscribeId+"\").");
    }

    public Enumeration<String> getChannels() {
        return channelAttributeValues.keys();
    }

    public boolean hasChannel(String channelSubscribeId) {
        return channelAttributeValues.containsKey(channelSubscribeId);
    }

    private ArrayList<String> createChannel(String channelSubscribeId) {
        ArrayList<String> l=new ArrayList<String>(defaultChannelAttributeValues.size());
        channelAttributeValues.put(channelSubscribeId,l);
        return l;
    }

    public String getCacheKey() {
        StringBuffer sbKey = new StringBuffer();
        for(Enumeration<String> e=channelAttributeValues.keys();e.hasMoreElements();) {
            String channelId=e.nextElement();
            sbKey.append("(channel:").append(channelId).append(':');
            List l=channelAttributeValues.get(channelId);
            for(int i=0;i<l.size();i++) {
                String value=(String)l.get(i);
                if(value==null) value=defaultChannelAttributeValues.get(i);
                sbKey.append(value).append(",");
            }
            sbKey.append(")");
        }
        return super.getCacheKey().concat(sbKey.toString());
    }
}
