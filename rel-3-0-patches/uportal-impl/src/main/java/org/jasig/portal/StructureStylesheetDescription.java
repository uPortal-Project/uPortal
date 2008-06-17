/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

 package org.jasig.portal;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Stylesheet description for stylesheets performing structure transformation
 * @author Peter Kharchenko
 * @version $Revision$
 */

public class StructureStylesheetDescription extends CoreXSLTStylesheetDescription {
    Hashtable folderAttributeTable;

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
    public void setFolderAttributeWordDescription(String attributeName,String wordDescription) {
        DescriptionDefaultValuePair pair=(DescriptionDefaultValuePair) folderAttributeTable.get(attributeName);
        if(pair!=null) pair.wordDescription=wordDescription;
    }
    public void setFolderAttributeDefaultValue(String attributeName,String defaultValue) {
        DescriptionDefaultValuePair pair=(DescriptionDefaultValuePair) folderAttributeTable.get(attributeName);
        if(pair!=null) pair.defaultValue=defaultValue;
    }

    public boolean containsFolderAttribute(String attributeName) {
        return folderAttributeTable.containsKey(attributeName);
    }

    public void addFolderAttribute(String name, String defaultValue, String wordDescription) {
        DescriptionDefaultValuePair pair=new DescriptionDefaultValuePair();
        pair.defaultValue=defaultValue; pair.wordDescription=wordDescription;
        folderAttributeTable.put(name,pair);
    }

}
