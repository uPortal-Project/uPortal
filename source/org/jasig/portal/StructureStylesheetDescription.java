package org.jasig.portal;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Stylesheet description for stylesheets performing structure transformation
 * @author Peter Kharchenko
 * @version $Revision$
 */

public class StructureStylesheetDescription extends CoreXSLTStylesheetDescription {
    Hashtable folderAttributeTable;
    protected Vector mediaList;

    public StructureStylesheetDescription() {
	super();
	folderAttributeTable=new Hashtable();
    }

    public Enumeration getFolderAttributeNames() {
	return folderAttributeTable.keys();
    }
    public String getFolderAttributeWordDescription(String attributeName) {
	DescriptionDefaultValuePair pair=(DescriptionDefaultValuePair) folderAttributeTable.get(attributeName);
	if(pair!=null) return pair.wordDescription;
	else return null;
    }
    public String getFolderAttributeDefaultValue(String attributeName) {
	DescriptionDefaultValuePair pair=(DescriptionDefaultValuePair) folderAttributeTable.get(attributeName);
	if(pair!=null) return pair.defaultValue;
	else return null;
    }

    public boolean containsAttribute(String attributeName) {
	return folderAttributeTable.containsKey(attributeName);
    }

    public void addFolderAttribute(String name, String defaultValue, String wordDescription) {
	DescriptionDefaultValuePair pair=new DescriptionDefaultValuePair();
	pair.defaultValue=defaultValue; pair.wordDescription=wordDescription;
	folderAttributeTable.put(name,pair);
    }

    public Vector getStylesheetMediaList() { return mediaList; }
    public void setStylesheetMediaList(Vector list) {
	mediaList=list;
    }
    
}
