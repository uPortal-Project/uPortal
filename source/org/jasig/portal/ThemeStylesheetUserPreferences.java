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

import java.util.*;

/**
 * User preferences for stylesheets performing theme transformation
 * @author Peter Kharchenko
 * @version $Revision$
 */

public class ThemeStylesheetUserPreferences extends StylesheetUserPreferences {
    protected Hashtable channelAttributeNumbers;;
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

    public String getChannelAttributeValue(String channelID,String attributeName) {
        Integer attributeNumber=(Integer)channelAttributeNumbers.get(attributeName);
        if(attributeNumber==null) {
            Logger.log(Logger.ERROR,"ThemeStylesheetUserPreferences::getChannelAttributeValue() : Attempting to obtain a non-existing attribute \""+attributeName+"\".");
            return null;
        }
        String value=null;
        List l=(List) channelAttributeValues.get(channelID);
        if(l==null) {
            Logger.log(Logger.ERROR,"ThemeStylesheetUserPreferences::getChannelAttributeValue() : Attempting to obtain an attribute for a non-existing channel \""+channelID+"\".");
            return null;
        } else {
            try {
                value=(String) l.get(attributeNumber.intValue());
            } catch (IndexOutOfBoundsException e) {}
            if(value==null) {
                try {
                    value=(String) defaultChannelAttributeValues.get(attributeNumber.intValue());
                } catch (IndexOutOfBoundsException e) {
                    Logger.log(Logger.ERROR,"ThemeStylesheetUserPreferences::getChannelAttributeValue() : internal error - attribute name is registered, but no default value is provided.");
                    return null;
                }
            }
        }
        return value;
    }

    // this should be modified to throw exceptions
    public void setChannelAttributeValue(String channelID,String attributeName,String attributeValue) {
        Integer attributeNumber=(Integer)channelAttributeNumbers.get(attributeName);
        if(attributeNumber==null) {
            Logger.log(Logger.ERROR,"ThemeStylesheetUserPreferences::setChannelAttribute() : Attempting to set a non-existing channel attribute \""+attributeName+"\".");
            return;
        }
        List l=(List) channelAttributeValues.get(channelID);
        if(l==null)
            l=this.createChannel(channelID);
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
            Logger.log(Logger.ERROR,"ThemeStylesheetUserPreferences::addChannelAttribute() : Attempting to re-add an existing channel attribute \""+attributeName+"\".");
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
            Logger.log(Logger.ERROR,"ThemeStylesheetUserPreferences::removeChannelAttribute() : Attempting to remove a non-existing channel attribute \""+attributeName+"\".");
        } else {
            channelAttributeNumbers.remove(attributeName);
            // do not touch the arraylists
        }
    }

    public Enumeration getChannelAttributeNames() {
        return channelAttributeNumbers.keys();
    }

    public void addChannel(String channelID) {
        // check if the channel is there. In general it might be ok to use this functon to default
        // all of the channel's parameters

        ArrayList l=new ArrayList(defaultChannelAttributeValues.size());

        if(channelAttributeValues.put(channelID,l)!=null)
            Logger.log(Logger.DEBUG,"ThemeStylesheetUserPreferences::addChannel() : Readding an existing channel (channelID=\""+channelID+"\"). All values will be set to default.");
    }

    public void removeChannel(String channelID) {
        if(channelAttributeValues.remove(channelID)==null)
            Logger.log(Logger.ERROR,"ThemeStylesheetUserPreferences::removeChannel() : Attempting to remove an non-existing channel (channelID=\""+channelID+"\").");
    }

    public Enumeration getChannels() {
        return channelAttributeValues.keys();
    }

    public boolean hasChannel(String channelID) {
        return channelAttributeValues.containsKey(channelID);
    }

    public void synchronizeWithDescription(ThemeStylesheetDescription sd) {
        super.synchronizeWithDescription(sd);
        // check if all of the channel attributes in the preferences occur in the description
        for(Enumeration e=channelAttributeNumbers.keys(); e.hasMoreElements(); ) {
            String pname=(String) e.nextElement();
            if(!sd.containsChannelAttribute(pname)) {
                this.removeChannelAttribute(pname);
                Logger.log(Logger.DEBUG,"ThemeStylesheetUserPreferences::synchronizeWithDescription() : removing channel attribute "+pname);
            }
        }
        // need to do the reverse synch. here
    }

    private ArrayList createChannel(String channelID) {
        ArrayList l=new ArrayList(defaultChannelAttributeValues.size());
        channelAttributeValues.put(channelID,l);
        return l;
    }
}
