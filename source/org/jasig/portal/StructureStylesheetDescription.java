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
    Hashtable categoryAttributeTable;
    protected Vector mediaList;

    public StructureStylesheetDescription() {
	super();
	categoryAttributeTable=new Hashtable();
    }

    public Enumeration getCategoryAttributeNames() {
	return categoryAttributeTable.keys();
    }
    public String getCategoryAttributeWordDescription(String attributeName) {
	DescriptionDefaultValuePair pair=(DescriptionDefaultValuePair) categoryAttributeTable.get(attributeName);
	if(pair!=null) return pair.wordDescription;
	else return null;
    }
    public String getCategoryAttributeDefaultValue(String attributeName) {
	DescriptionDefaultValuePair pair=(DescriptionDefaultValuePair) categoryAttributeTable.get(attributeName);
	if(pair!=null) return pair.defaultValue;
	else return null;
    }

    public boolean containsAttribute(String attributeName) {
	return categoryAttributeTable.containsKey(attributeName);
    }

    public void addCategoryAttribute(String name, String defaultValue, String wordDescription) {
	DescriptionDefaultValuePair pair=new DescriptionDefaultValuePair();
	pair.defaultValue=defaultValue; pair.wordDescription=wordDescription;
	categoryAttributeTable.put(name,pair);
    }

    public Vector getStylesheetMediaList() { return mediaList; }
    public void setStylesheetMediaList(Vector list) {
	mediaList=list;
    }

};
