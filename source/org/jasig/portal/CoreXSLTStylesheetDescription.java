package org.jasig.portal;

import java.util.Hashtable;
import java.util.Enumeration;

/**
 * Description of core XSLT stylesheets (i.e. structure and theme transformations)
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

    public boolean containsChannelAttribute(String attributeName) {
	return channelAttributeTable.containsKey(attributeName);
    }

    public void addChannelAttribute(String name, String defaultValue, String wordDescription) {
	DescriptionDefaultValuePair pair=new DescriptionDefaultValuePair();
	pair.defaultValue=defaultValue; pair.wordDescription=wordDescription;
	channelAttributeTable.put(name,pair);
    }
    
}
