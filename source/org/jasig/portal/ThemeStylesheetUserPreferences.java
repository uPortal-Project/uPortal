package org.jasig.portal;

import java.util.*;

public class ThemeStylesheetUserPreferences extends StylesheetUserPreferences {
    protected ArrayList channelAttributeNames;
    protected Hashtable channelAttributeValues;

    public ThemeStylesheetUserPreferences() {
	super();
	channelAttributeNames=new ArrayList();
	channelAttributeValues=new Hashtable();
    }

    public ThemeStylesheetUserPreferences(ThemeStylesheetUserPreferences ssup) {
	super(ssup);
	this.channelAttributeNames=new ArrayList(ssup.channelAttributeNames);
	this.channelAttributeValues=new Hashtable(ssup.channelAttributeValues);
    }

    public String getChannelAttributeValue(String channelID,String attributeName) {
	int attributeNumber=channelAttributeNames.indexOf(attributeName);
	if(attributeNumber==-1) {
	    Logger.log(Logger.ERROR,"ThemeStylesheetUserPreferences::getChannelAttributeValue() : Attempting to obtain a non-existing attribute \""+attributeName+"\".");
	    return null;
	}
        List l=(List) channelAttributeValues.get(channelID);
	if(l==null) { 
	    Logger.log(Logger.ERROR,"ThemeStylesheetUserPreferences::getChannelAttributeValue() : Attempting to obtain an attribute for a non-existing channel \""+channelID+"\".");
	    return null;
	} else return (String) l.get(attributeNumber);
    }

    // this should be modified to throw exceptions
    public void setChannelAttributeValue(String channelID,String attributeName,String attributeValue) {
	int attributeNumber=channelAttributeNames.indexOf(attributeName);
	if(attributeNumber==-1) {
	    Logger.log(Logger.ERROR,"ThemeStylesheetUserPreferences::setChannelAttribute() : Attempting to set a non-existing channel attribute \""+attributeName+"\".");
	    return;
	}
        List l=(List) channelAttributeValues.get(channelID);
	if(l==null)
	    l=this.createChannel(channelID);
	l.set(attributeNumber,attributeValue); 
    }

    public void addChannelAttribute(String attributeName, String defaultValue) {
	if(channelAttributeNames.indexOf(attributeName)!=-1) {
	    Logger.log(Logger.ERROR,"ThemeStylesheetUserPreferences::addChannelAttribute() : Attempting to re-add an existing channel attribute \""+attributeName+"\".");
	} else {
	    channelAttributeNames.add(attributeName);
	    for (Enumeration e = channelAttributeValues.elements() ; e.hasMoreElements() ;) 
		((List) e.nextElement()).add(defaultValue);
	}
    }

    public void removeChannelAttribute(String attributeName) {
	int attributeNumber;
	if((attributeNumber=channelAttributeNames.indexOf(attributeName))==-1) {
	    Logger.log(Logger.ERROR,"ThemeStylesheetUserPreferences::removeChannelAttribute() : Attempting to remove a non-existing channel attribute \""+attributeName+"\".");
	} else {
	    channelAttributeNames.remove(attributeNumber);
	    for (Enumeration e = channelAttributeValues.elements() ; e.hasMoreElements() ;) 
		((List) e.nextElement()).remove(attributeNumber);
	}
    }

    public List getChannelAttributeNames() {
	return channelAttributeNames;
    }
    
    public void addChannel(String channelID) {
	// check if the channel is there. In general it might be ok to use this functon to default
	// all of the channel's parameters
	
	// prepare a null-filled ArrayList
	ArrayList l=new ArrayList(channelAttributeNames.size());
	for(int i=channelAttributeNames.size();i>0;i--) 
	    l.add(null);
	    
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

    public void synchronizeWithDescription(CoreXSLTStylesheetDescription sd) {
	super.synchronizeWithDescription(sd);
	// check if all of the channel attributes in the preferences occur in the description
	for(int i=channelAttributeNames.size()-1; i>=0 ; i--) {
	    String pname=(String) channelAttributeNames.get(i);
	    if(!sd.containsAttribute(pname)) {
		this.removeChannelAttribute(pname);
		Logger.log(Logger.DEBUG,"ThemeStylesheetUserPreferences::synchronizeWithDescription() : removing channel attribute "+pname);
	    }
	}
    }
    
    public void completeWithDescriptionInformation(CoreXSLTStylesheetDescription sd) {
	super.completeWithDescriptionInformation(sd);
	for (Enumeration e = sd.getChannelAttributeNames() ; e.hasMoreElements() ;) {
	    String pname=(String) e.nextElement();
	    if(!channelAttributeNames.contains(pname)) {
		this.addChannelAttribute(pname,sd.getChannelAttributeDefaultValue(pname));
		Logger.log(Logger.DEBUG,"ThemeStylesheetUserPreferences::completeWithDescriptionInformation() : adding channel attribute "+pname);
	    } else {
		// go over every channel to make sure that the default attribute values are set
		int attributeNumber=channelAttributeNames.indexOf(pname);
		for (Enumeration ce = channelAttributeValues.elements() ; ce.hasMoreElements() ;) {
		    if(attributeNumber==-1) {
			Logger.log(Logger.ERROR,"ThemeStylesheetUserPreferences::completeWithDescriptionInformation() : attempting to set a non-existing channel attribute \""+pname+"\".");
			return;
		    }
		    // the following can be used only if the loop is changed to iteratve over .keys() 
		    //		    String channelID=(String) ce.nextElement();
		    //		    List l=(List) channelAttributeValues.get(channelID);
		    List l=(List) ce.nextElement();
		    if(l.get(attributeNumber)==null) {
			l.set(attributeNumber,sd.getChannelAttributeDefaultValue(pname)); 
			//			Logger.log(Logger.DEBUG,"ThemeStylesheetUserPreferences::completeWithDescriptionInformation() : setting attribute "+pname+" of channel "+channelID+" to default");
		    }
		}
	    }
	}
    }	



    private ArrayList createChannel(String channelID) {
	ArrayList l=new ArrayList(channelAttributeNames.size());
	for(int i=channelAttributeNames.size();i>0;i--) 
	    l.add(null);
	channelAttributeValues.put(channelID,l);
	return l;
    }
}
