/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
 package org.jasig.portal;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * CoreXSLTStyleSheetDescription contains properties of core XSLT stylesheets.
 * This includes structure and theme transformations.
 * 
 * @author Peter Kharchenko
 * @version $Revision$
 */

public class CoreXSLTStylesheetDescription extends CoreStylesheetDescription {

    Hashtable channelAttributeTable;

    public CoreXSLTStylesheetDescription() {
        super();
        channelAttributeTable=new Hashtable();
    }

    public Enumeration getChannelAttributeNames() {
        return channelAttributeTable.keys();
    }
    public String getChannelAttributeWordDescription(String attributeName) {
        DescriptionDefaultValuePair pair=(DescriptionDefaultValuePair) channelAttributeTable.get(attributeName);
        if(pair!=null) return pair.wordDescription;
        else return null;
    }
    public String getChannelAttributeDefaultValue(String attributeName) {
        DescriptionDefaultValuePair pair=(DescriptionDefaultValuePair) channelAttributeTable.get(attributeName);
        if(pair!=null) return pair.defaultValue;
        else return null;
    }
    public void setChannelAttributeWordDescription(String attributeName,String wordDescription) {
        DescriptionDefaultValuePair pair=(DescriptionDefaultValuePair) channelAttributeTable.get(attributeName);
        if(pair!=null) pair.wordDescription=wordDescription;
    }
    public void setChannelAttributeDefaultValue(String attributeName,String defaultValue) {
        DescriptionDefaultValuePair pair=(DescriptionDefaultValuePair) channelAttributeTable.get(attributeName);
        if(pair!=null) pair.defaultValue=defaultValue;
    }

    public boolean containsChannelAttribute(String attributeName) {
        return channelAttributeTable.containsKey(attributeName);
    }

    public void addChannelAttribute(String name, String defaultValue, String wordDescription) {
        DescriptionDefaultValuePair pair=new DescriptionDefaultValuePair();
        pair.defaultValue=defaultValue; pair.wordDescription=wordDescription;
        channelAttributeTable.put(name,pair);
    }

}
