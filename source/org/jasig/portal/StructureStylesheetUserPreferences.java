package org.jasig.portal;

import java.util.*;

/**
 * User preferences for stylesheets performing theme transformation
 * @author Peter Kharchenko
 * @version $Revision$
 */


// I take it first stylesheet preferences will remain to be more complex then 
// preferences of the second stylesheet
public class StructureStylesheetUserPreferences extends ThemeStylesheetUserPreferences {
    protected ArrayList folderAttributeNames;
    protected Hashtable folderAttributeValues;

    public StructureStylesheetUserPreferences() {
	super();
	folderAttributeNames=new ArrayList();
	folderAttributeValues=new Hashtable();
    }

    public StructureStylesheetUserPreferences( StructureStylesheetUserPreferences fsup) {
	super(fsup);
	this.folderAttributeNames=new ArrayList(fsup.folderAttributeNames);
	this.folderAttributeValues=new Hashtable(fsup.folderAttributeValues);
    }

    public String getFolderAttributeValue(String folderID,String attributeName) {
	int attributeNumber=folderAttributeNames.indexOf(attributeName);
	if(attributeNumber==-1) {
	    Logger.log(Logger.ERROR,"StructureStylesheetUserPreferences::getFolderAttributeValue() : Attempting to obtain a non-existing attribute \""+attributeName+"\".");
	    return null;
	}
        List l=(List) folderAttributeValues.get(folderID);
	if(l==null) { 
	    Logger.log(Logger.ERROR,"StructureStylesheetUserPreferences::getFolderAttributeValue() : Attempting to obtain an attribute for a non-existing folder \""+folderID+"\".");
	    return null;
	} else return (String) l.get(attributeNumber);
    }

    // this should be modified to throw exceptions
    public void setFolderAttributeValue(String folderID,String attributeName,String attributeValue) {
	int attributeNumber=folderAttributeNames.indexOf(attributeName);
	if(attributeNumber==-1) {
	    Logger.log(Logger.ERROR,"StructureStylesheetUserPreferences::setFolderAttributeValue() : Attempting to set a non-existing folder attribute \""+attributeName+"\".");
	    return;
	}
        List l=(List) folderAttributeValues.get(folderID);
	if(l==null)
	    l=this.createFolder(folderID);
	l.set(attributeNumber,attributeValue); 
    }

    public void addFolderAttribute(String attributeName, String defaultValue) {
	if(folderAttributeNames.indexOf(attributeName)!=-1) {
	    Logger.log(Logger.ERROR,"StructureStylesheetUserPreferences::addFolderAttribute() : Attempting to re-add an existing folder attribute \""+attributeName+"\".");
	} else {
	    folderAttributeNames.add(attributeName);
	    for (Enumeration e = folderAttributeValues.elements() ; e.hasMoreElements() ;) 
		((List) e.nextElement()).add(defaultValue);
	}
    }

    public void removeFolderAttribute(String attributeName) {
	int attributeNumber;
	if((attributeNumber=folderAttributeNames.indexOf(attributeName))==-1) {
	    Logger.log(Logger.ERROR,"StructureStylesheetUserPreferences::removeFolderAttribute() : Attempting to remove a non-existing folder attribute \""+attributeName+"\".");
	} else {
	    folderAttributeNames.remove(attributeNumber);
	    for (Enumeration e = folderAttributeValues.elements() ; e.hasMoreElements() ;) 
		((List) e.nextElement()).remove(attributeNumber);
	}
    }

    public List getFolderAttributeNames() {
	return folderAttributeNames;
    }
    
    public void addFolder(String folderID) {
	// check if the folder is there. In general it might be ok to use this functon to default
	// all of the folder's parameters
	
	// prepare a null-filled ArrayList
	ArrayList l=new ArrayList(folderAttributeNames.size());
	for(int i=folderAttributeNames.size();i>0;i--) 
	    l.add(null);
	
	if(folderAttributeValues.put(folderID,l)!=null) 
	    Logger.log(Logger.DEBUG,"Readding an existing folder (folderID=\""+folderID+"\"). All values will be set to default.");
    }
    
    public void removeFolder(String folderID) {
	if(folderAttributeValues.remove(folderID)==null) 
	    Logger.log(Logger.ERROR,"Attempting to remove an non-existing folder (folderID=\""+folderID+"\").");
    }


    public void synchronizeWithDescription(StructureStylesheetDescription sd) {
	super.synchronizeWithDescription(sd);
	// check if all of the folder attributes in the preferences occur in the description
	for(int i=folderAttributeNames.size()-1; i>=0 ; i--) {
	    String pname=(String) folderAttributeNames.get(i);
	    if(!sd.containsAttribute(pname)) {
		this.removeFolderAttribute(pname);
	    }
	}
    }
    
    public void completeWithDescriptionInformation(StructureStylesheetDescription sd) {
	super.completeWithDescriptionInformation(sd);
	for (Enumeration e = sd.getFolderAttributeNames() ; e.hasMoreElements() ;) {
	    String pname=(String) e.nextElement();
	    if(!folderAttributeNames.contains(pname)) {
		this.addFolderAttribute(pname,sd.getFolderAttributeDefaultValue(pname));
		Logger.log(Logger.DEBUG,"StructureStylesheetUserPreferences::completeWithDescriptionInformation() : adding folder attribute "+pname);
	    } else {
		// go over every folder to make sure that the default attribute values are set
		int attributeNumber=folderAttributeNames.indexOf(pname);
		for (Enumeration ce = folderAttributeValues.elements() ; ce.hasMoreElements() ;) {
		    if(attributeNumber==-1) {
			Logger.log(Logger.ERROR,"StructureStylesheetUserPreferences::completeWithDescriptionInformation() : attempting to set a non-existing folder attribute \""+pname+"\".");
			return;
		    }
		    // the following can be used only if the loop is changed to iteratve over .keys() 
		    //		    String folderID=(String) ce.nextElement();
		    //		    List l=(List) folderAttributeValues.get(folderID);
		    List l=(List) ce.nextElement();
		    if(l.get(attributeNumber)==null) {
			l.set(attributeNumber,sd.getFolderAttributeDefaultValue(pname)); 
			//			Logger.log(Logger.DEBUG,"StructureStylesheetUserPreferences::completeWithDescriptionInformation() : setting attribute "+pname+" of folder "+folderID+" to default");
		    }
		}
	    }

	}
    }	



    public Enumeration getCategories() {
	return folderAttributeValues.keys();
    }

    public boolean hasFolder(String folderID) {
	return folderAttributeValues.containsKey(folderID);
    }

    private ArrayList createFolder(String folderID) {
	ArrayList l=new ArrayList(folderAttributeNames.size());
	for(int i=folderAttributeNames.size();i>0;i--) 
	    l.add(null);
	folderAttributeValues.put(folderID,l);
	return l;
    }

    private ArrayList copyFolderAttributeNames() {
	return folderAttributeNames;
    }

    
}
