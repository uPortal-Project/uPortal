/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
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
    
    protected Hashtable channelAttributeNumbers;
    protected Hashtable channelAttributeValues;
    protected ArrayList defaultChannelAttributeValues;

    public ThemeStylesheetUserPreferences() {
        super();
        channelAttributeNumbers=new Hashtable();
        channelAttributeValues=new Hashtable();
        defaultChannelAttributeValues=new ArrayList();
    }

    public ThemeStylesheetUserPreferences(ThemeStylesheetUserPreferences ssup) {
        super(ssup);
        this.channelAttributeNumbers=new Hashtable(ssup.channelAttributeNumbers);
        this.channelAttributeValues=new Hashtable(ssup.channelAttributeValues);
        this.defaultChannelAttributeValues=new ArrayList(ssup.defaultChannelAttributeValues);
    }

    public String getChannelAttributeValue(String channelSubscribeId,String attributeName) {
        Integer attributeNumber=(Integer)channelAttributeNumbers.get(attributeName);
        if(attributeNumber==null) {
            log.error("ThemeStylesheetUserPreferences::getChannelAttributeValue() : Attempting to obtain a non-existing attribute \""+attributeName+"\".");
            return null;
        }
        String value=null;
        List l=(List) channelAttributeValues.get(channelSubscribeId);
        if(l==null) {
	    //            log.debug("ThemeStylesheetUserPreferences::getChannelAttributeValue() : Attempting to obtain an attribute for a non-existing channel \""+channelSubscribeId+"\".");
	    // return null;
	    return (String) defaultChannelAttributeValues.get(attributeNumber.intValue());
        } else {
            if(attributeNumber.intValue()<l.size()) {
                value=(String) l.get(attributeNumber.intValue());
            }
            if(value==null) {
                try {
                    value=(String) defaultChannelAttributeValues.get(attributeNumber.intValue());
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
        Integer attributeNumber=(Integer)channelAttributeNumbers.get(attributeName);
        if(attributeNumber==null) {
            log.error("ThemeStylesheetUserPreferences::hasDefinedChannelAttributeValue() : Attempting to obtain a non-existing attribute \""+attributeName+"\".");
            return null;
        }
        List l=(List) channelAttributeValues.get(channelSubscribeId);
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
        Integer attributeNumber=(Integer)channelAttributeNumbers.get(attributeName);
        if(attributeNumber==null) {
            log.error("ThemeStylesheetUserPreferences::setChannelAttribute() : Attempting to set a non-existing channel attribute \""+attributeName+"\".");
            return;
        }
        List l=(List) channelAttributeValues.get(channelSubscribeId);
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
        Integer attributeNumber=(Integer)channelAttributeNumbers.get(attributeName);
        defaultChannelAttributeValues.set(attributeNumber.intValue(),defaultValue);
    }

    public void removeChannelAttribute(String attributeName) {
        Integer attributeNumber;
        if((attributeNumber=(Integer)channelAttributeNumbers.get(attributeName))==null) {
            log.error("ThemeStylesheetUserPreferences::removeChannelAttribute() : Attempting to remove a non-existing channel attribute \""+attributeName+"\".");
        } else {
            channelAttributeNumbers.remove(attributeName);
            // do not touch the arraylists
        }
    }

    public Enumeration getChannelAttributeNames() {
        return channelAttributeNumbers.keys();
    }

    public void addChannel(String channelSubscribeId) {
        // check if the channel is there. In general it might be ok to use this functon to default
        // all of the channel's parameters

        ArrayList l=new ArrayList(defaultChannelAttributeValues.size());

        if(channelAttributeValues.put(channelSubscribeId,l)!=null)
            log.debug("ThemeStylesheetUserPreferences::addChannel() : Readding an existing channel (channelSubscribeId=\""+channelSubscribeId+"\"). All values will be set to default.");
    }

    public void removeChannel(String channelSubscribeId) {
        if(channelAttributeValues.remove(channelSubscribeId)==null)
            log.error("ThemeStylesheetUserPreferences::removeChannel() : Attempting to remove an non-existing channel (channelSubscribeId=\""+channelSubscribeId+"\").");
    }

    public Enumeration getChannels() {
        return channelAttributeValues.keys();
    }

    public boolean hasChannel(String channelSubscribeId) {
        return channelAttributeValues.containsKey(channelSubscribeId);
    }

    private ArrayList createChannel(String channelSubscribeId) {
        ArrayList l=new ArrayList(defaultChannelAttributeValues.size());
        channelAttributeValues.put(channelSubscribeId,l);
        return l;
    }

    public String getCacheKey() {
        StringBuffer sbKey = new StringBuffer();
        for(Enumeration e=channelAttributeValues.keys();e.hasMoreElements();) {
            String channelId=(String)e.nextElement();
            sbKey.append("(channel:").append(channelId).append(':');
            List l=(List)channelAttributeValues.get(channelId);
            for(int i=0;i<l.size();i++) {
                String value=(String)l.get(i);
                if(value==null) value=(String)defaultChannelAttributeValues.get(i);
                sbKey.append(value).append(",");
            }
            sbKey.append(")");
        }
        return super.getCacheKey().concat(sbKey.toString());
    }
}
