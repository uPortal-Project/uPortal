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
