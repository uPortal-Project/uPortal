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
    protected ArrayList categoryAttributeNames;
    protected Hashtable categoryAttributeValues;

    public StructureStylesheetUserPreferences() {
	super();
	categoryAttributeNames=new ArrayList();
	categoryAttributeValues=new Hashtable();
    }

    public StructureStylesheetUserPreferences( StructureStylesheetUserPreferences fsup) {
	super(fsup);
	this.categoryAttributeNames=new ArrayList(fsup.categoryAttributeNames);
	this.categoryAttributeValues=new Hashtable(fsup.categoryAttributeValues);
    }

    public String getCategoryAttributeValue(String categoryID,String attributeName) {
	int attributeNumber=categoryAttributeNames.indexOf(attributeName);
	if(attributeNumber==-1) {
	    Logger.log(Logger.ERROR,"StructureStylesheetUserPreferences::getCategoryAttributeValue() : Attempting to obtain a non-existing attribute \""+attributeName+"\".");
	    return null;
	}
        List l=(List) categoryAttributeValues.get(categoryID);
	if(l==null) { 
	    Logger.log(Logger.ERROR,"StructureStylesheetUserPreferences::getCategoryAttributeValue() : Attempting to obtain an attribute for a non-existing category \""+categoryID+"\".");
	    return null;
	} else return (String) l.get(attributeNumber);
    }

    // this should be modified to throw exceptions
    public void setCategoryAttributeValue(String categoryID,String attributeName,String attributeValue) {
	int attributeNumber=categoryAttributeNames.indexOf(attributeName);
	if(attributeNumber==-1) {
	    Logger.log(Logger.ERROR,"StructureStylesheetUserPreferences::setCategoryAttributeValue() : Attempting to set a non-existing category attribute \""+attributeName+"\".");
	    return;
	}
        List l=(List) categoryAttributeValues.get(categoryID);
	if(l==null)
	    l=this.createCategory(categoryID);
	l.set(attributeNumber,attributeValue); 
    }

    public void addCategoryAttribute(String attributeName, String defaultValue) {
	if(categoryAttributeNames.indexOf(attributeName)!=-1) {
	    Logger.log(Logger.ERROR,"StructureStylesheetUserPreferences::addCategoryAttribute() : Attempting to re-add an existing category attribute \""+attributeName+"\".");
	} else {
	    categoryAttributeNames.add(attributeName);
	    for (Enumeration e = categoryAttributeValues.elements() ; e.hasMoreElements() ;) 
		((List) e.nextElement()).add(defaultValue);
	}
    }

    public void removeCategoryAttribute(String attributeName) {
	int attributeNumber;
	if((attributeNumber=categoryAttributeNames.indexOf(attributeName))==-1) {
	    Logger.log(Logger.ERROR,"StructureStylesheetUserPreferences::removeCategoryAttribute() : Attempting to remove a non-existing category attribute \""+attributeName+"\".");
	} else {
	    categoryAttributeNames.remove(attributeNumber);
	    for (Enumeration e = categoryAttributeValues.elements() ; e.hasMoreElements() ;) 
		((List) e.nextElement()).remove(attributeNumber);
	}
    }

    public List getCategoryAttributeNames() {
	return categoryAttributeNames;
    }
    
    public void addCategory(String categoryID) {
	// check if the category is there. In general it might be ok to use this functon to default
	// all of the category's parameters
	
	// prepare a null-filled ArrayList
	ArrayList l=new ArrayList(categoryAttributeNames.size());
	for(int i=categoryAttributeNames.size();i>0;i--) 
	    l.add(null);
	
	if(categoryAttributeValues.put(categoryID,l)!=null) 
	    Logger.log(Logger.DEBUG,"Readding an existing category (categoryID=\""+categoryID+"\"). All values will be set to default.");
    }
    
    public void removeCategory(String categoryID) {
	if(categoryAttributeValues.remove(categoryID)==null) 
	    Logger.log(Logger.ERROR,"Attempting to remove an non-existing category (categoryID=\""+categoryID+"\").");
    }


    public void synchronizeWithDescription(StructureStylesheetDescription sd) {
	super.synchronizeWithDescription(sd);
	// check if all of the category attributes in the preferences occur in the description
	for(int i=categoryAttributeNames.size()-1; i>=0 ; i--) {
	    String pname=(String) categoryAttributeNames.get(i);
	    if(!sd.containsAttribute(pname)) {
		this.removeCategoryAttribute(pname);
	    }
	}
    }
    
    public void completeWithDescriptionInformation(StructureStylesheetDescription sd) {
	super.completeWithDescriptionInformation(sd);
	for (Enumeration e = sd.getCategoryAttributeNames() ; e.hasMoreElements() ;) {
	    String pname=(String) e.nextElement();
	    if(!categoryAttributeNames.contains(pname)) {
		this.addCategoryAttribute(pname,sd.getCategoryAttributeDefaultValue(pname));
		Logger.log(Logger.DEBUG,"StructureStylesheetUserPreferences::completeWithDescriptionInformation() : adding category attribute "+pname);
	    } else {
		// go over every category to make sure that the default attribute values are set
		int attributeNumber=categoryAttributeNames.indexOf(pname);
		for (Enumeration ce = categoryAttributeValues.elements() ; ce.hasMoreElements() ;) {
		    if(attributeNumber==-1) {
			Logger.log(Logger.ERROR,"StructureStylesheetUserPreferences::completeWithDescriptionInformation() : attempting to set a non-existing category attribute \""+pname+"\".");
			return;
		    }
		    // the following can be used only if the loop is changed to iteratve over .keys() 
		    //		    String categoryID=(String) ce.nextElement();
		    //		    List l=(List) categoryAttributeValues.get(categoryID);
		    List l=(List) ce.nextElement();
		    if(l.get(attributeNumber)==null) {
			l.set(attributeNumber,sd.getCategoryAttributeDefaultValue(pname)); 
			//			Logger.log(Logger.DEBUG,"StructureStylesheetUserPreferences::completeWithDescriptionInformation() : setting attribute "+pname+" of category "+categoryID+" to default");
		    }
		}
	    }

	}
    }	



    public Enumeration getCategories() {
	return categoryAttributeValues.keys();
    }

    public boolean hasCategory(String categoryID) {
	return categoryAttributeValues.containsKey(categoryID);
    }

    private ArrayList createCategory(String categoryID) {
	ArrayList l=new ArrayList(categoryAttributeNames.size());
	for(int i=categoryAttributeNames.size();i>0;i--) 
	    l.add(null);
	categoryAttributeValues.put(categoryID,l);
	return l;
    }

    private ArrayList copyCategoryAttributeNames() {
	return categoryAttributeNames;
    }

    
}
