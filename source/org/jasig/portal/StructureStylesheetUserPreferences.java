/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

 package org.jasig.portal;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * User preferences for stylesheets performing structure transformation
 * @author Peter Kharchenko
 * @version $Revision$
 */


// structure stylesheet preferences will remain to be more complex then
// preferences of the second stylesheet, hence the derivation
public class StructureStylesheetUserPreferences extends ThemeStylesheetUserPreferences {
    
    private static final Log log = LogFactory.getLog(StructureStylesheetUserPreferences.class);
    
    protected Hashtable folderAttributeNumbers;
    protected Hashtable folderAttributeValues;
    protected ArrayList defaultFolderAttributeValues;

    public StructureStylesheetUserPreferences() {
        super();
        folderAttributeNumbers=new Hashtable();
        folderAttributeValues=new Hashtable();
        defaultFolderAttributeValues=new ArrayList();
    }

    public StructureStylesheetUserPreferences( StructureStylesheetUserPreferences fsup) {
        super(fsup);
        this.folderAttributeNumbers=new Hashtable(fsup.folderAttributeNumbers);
        this.folderAttributeValues=new Hashtable(fsup.folderAttributeValues);
        this.defaultFolderAttributeValues=new ArrayList(fsup.defaultFolderAttributeValues);
    }

    public String getFolderAttributeValue(String folderID,String attributeName) {
        Integer attributeNumber=(Integer)folderAttributeNumbers.get(attributeName);
        if(attributeNumber==null) {
            log.error("StructureStylesheetUserPreferences::getFolderAttributeValue() : Attempting to obtain a non-existing attribute \""+attributeName+"\".");
            return null;
        }
        String value=null;
        List l=(List) folderAttributeValues.get(folderID);
        if(l==null) {
	    //            log.error("StructureStylesheetUserPreferences::getFolderAttributeValue() : Attempting to obtain an attribute for a non-existing folder \""+folderID+"\".");
	    //            return null;
	    return (String) defaultFolderAttributeValues.get(attributeNumber.intValue());
        } else {
            if(attributeNumber.intValue()<l.size()) {
                value=(String) l.get(attributeNumber.intValue());
            }
            if(value==null) {
                try {
                    value=(String) defaultFolderAttributeValues.get(attributeNumber.intValue());
                } catch (IndexOutOfBoundsException e) {
                    log.error("StructureStylesheetUserPreferences::getFolderAttributeValue() : internal error - attribute name is registered, but no default value is provided.");
                    return null;
                }
            }
        }
        return value;
    }

    /**
     * Returns folder attribute value only if it has been assigned specifically.
     * @param folderID folder id
     * @param attributeName name of the attribute
     * @return attribute value or null if the value is determined by the attribute default
     */
    public String getDefinedFolderAttributeValue(String folderID, String attributeName) {
        Integer attributeNumber=(Integer)folderAttributeNumbers.get(attributeName);
        if(attributeNumber==null) {
            log.error("ThemeStylesheetUserPreferences::hasDefinedFolderAttributeValue() : Attempting to obtain a non-existing attribute \""+attributeName+"\".");
            return null;
        }
        List l=(List) folderAttributeValues.get(folderID);
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
    public void setFolderAttributeValue(String folderID,String attributeName,String attributeValue) {
        Integer attributeNumber=(Integer)folderAttributeNumbers.get(attributeName);
        if(attributeNumber==null) {
            log.error("StructureStylesheetUserPreferences::setFolderAttribute() : Attempting to set a non-existing folder attribute \""+attributeName+"\".");
            return;
        }
        List l=(List) folderAttributeValues.get(folderID);
        if(l==null)
            l=this.createFolder(folderID);
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

    public void addFolderAttribute(String attributeName, String defaultValue) {
        if(folderAttributeNumbers.get(attributeName)!=null) {
            log.error("StructureStylesheetUserPreferences::addFolderAttribute() : Attempting to re-add an existing folder attribute \""+attributeName+"\".");
        } else {
            folderAttributeNumbers.put(attributeName,new Integer(defaultFolderAttributeValues.size()));
            // append to the end of the default value array
            defaultFolderAttributeValues.add(defaultValue);
        }
    }

    public void setFolderAttributeDefaultValue(String attributeName, String defaultValue) {
        Integer attributeNumber=(Integer)folderAttributeNumbers.get(attributeName);
        defaultFolderAttributeValues.set(attributeNumber.intValue(),defaultValue);
    }

    public void removeFolderAttribute(String attributeName) {
        Integer attributeNumber;
        if((attributeNumber=(Integer)folderAttributeNumbers.get(attributeName))==null) {
            log.error("StructureStylesheetUserPreferences::removeFolderAttribute() : Attempting to remove a non-existing folder attribute \""+attributeName+"\".");
        } else {
            folderAttributeNumbers.remove(attributeName);
            // do not touch the arraylists
        }
    }

    public Enumeration getFolderAttributeNames() {
        return folderAttributeNumbers.keys();
    }

    public void addFolder(String folderID) {
        // check if the folder is there. In general it might be ok to use this functon to default
        // all of the folder's parameters

        ArrayList l=new ArrayList(defaultFolderAttributeValues.size());

        if(folderAttributeValues.put(folderID,l)!=null)
            log.debug("StructureStylesheetUserPreferences::addFolder() : Readding an existing folder (folderID=\""+folderID+"\"). All values will be set to default.");
    }

    public void removeFolder(String folderID) {
        if(folderAttributeValues.remove(folderID)==null)
            log.error("StructureStylesheetUserPreferences::removeFolder() : Attempting to remove an non-existing folder (folderID=\""+folderID+"\").");
    }


    public Enumeration getFolders() {
        return folderAttributeValues.keys();
    }

    public boolean hasFolder(String folderID) {
        return folderAttributeValues.containsKey(folderID);
    }

    private ArrayList createFolder(String folderID) {
        ArrayList l=new ArrayList(defaultFolderAttributeValues.size());
        folderAttributeValues.put(folderID,l);
        return l;
    }

    private Hashtable copyFolderAttributeNames() {
        return folderAttributeNumbers;
    }

    public String getCacheKey() {
        StringBuffer sbKey = new StringBuffer();
        for(Enumeration e=folderAttributeValues.keys();e.hasMoreElements();) {
            String folderId=(String)e.nextElement();
            sbKey.append("(folder:").append(folderId).append(':');
            List l=(List)folderAttributeValues.get(folderId);
            for(int i=0;i<l.size();i++) {
                String value=(String)l.get(i);
                if(value==null) value=(String)defaultFolderAttributeValues.get(i);
                sbKey.append(value).append(",");
            }
            sbKey.append(")");
        }
        return super.getCacheKey().concat(sbKey.toString());
    }

}
