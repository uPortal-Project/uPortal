/* Copyright 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.channels.portlet.IPortletAdaptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Describes a published channel.
 * @author George Lindholm, ITServices, UBC
 * @version $Revision$ $Date$
 */
public class ChannelDefinition implements IBasicEntity {
    private static final Log log = LogFactory.getLog(ChannelDefinition.class);
  private int id;
  private String chanFName;
  private String chanName;
  private String chanDesc;
  private String chanTitle;
  private String chanClass;
  private int chanTimeout;
  private int chanTypeId;
  private int chanPupblUsrId;
  private int chanApvlId;
  private Date chanPublDt;
  private Date chanApvlDt;
  private boolean chanEditable;
  private boolean chanHasHelp;
  private boolean chanHasAbout;
  private boolean chanIsSecure;    
  private Map parameters; // Consider implementing as a Set
  private String chanLocale;
  private Hashtable chanDescs;
  private Hashtable chanTitles;
  private Hashtable chanNames;

  /**
   * Constructs a channel definition.
   * @param id the channel definition ID
   */
  public ChannelDefinition(int id) {
    this.id = id;
    this.chanTitle = "";
    this.chanDesc = "";
    this.chanClass = "";
    this.parameters = new HashMap();
    this.chanLocale = null;
    this.chanTitles = new Hashtable();
    this.chanNames = new Hashtable();
    this.chanDescs = new Hashtable();
  }

  // Getter methods
  public int getId() { return id; }
  public String getFName() { return chanFName; }
  public String getName() { return chanName; }
  public String getDescription() { return chanDesc; }
  public String getTitle() { return chanTitle; }
  public String getJavaClass() { return chanClass; }
  public int getTimeout() { return chanTimeout; }
  public int getTypeId() { return chanTypeId; }
  public int getPublisherId() { return chanPupblUsrId; }
  public int getApproverId() { return chanApvlId; }
  public Date getPublishDate() { return chanPublDt; }
  public Date getApprovalDate() { return chanApvlDt;}
  public boolean isEditable() { return chanEditable; }
  public boolean hasHelp() { return chanHasHelp; }
  public boolean hasAbout() { return chanHasAbout; }
  public boolean isSecure() { return chanIsSecure; }    
  
  /**
   * Returns true if this channel definition defines a portlet.
   * Returns false if this channel definition does not define a portlet or
   * whether this channel definition defines a portlet or not cannot be 
   * determined because this definition's channel class is not set or cannot
   * be loaded.
   * @return true if we know we're a portlet, false otherwise
   */
  public boolean isPortlet() {
  	
  	/*
  	 * We believe we are a portlet if the channel class implements 
  	 * IPortletAdaptor.
  	 */
  	
      if (this.chanClass != null) {
          try {
			Class channelClass = Class.forName(this.chanClass);
			return IPortletAdaptor.class.isAssignableFrom(channelClass);
		} catch (ClassNotFoundException e) {
			log.error("Unable to load class for name [" + this.chanClass 
					+ "] and so do not know whether is a portlet.");
		}
      }
      
      return false;
  }
  
  public ChannelParameter[] getParameters() { return (ChannelParameter[])parameters.values().toArray(new ChannelParameter[0]); }
  public String getLocale() { return chanLocale; }
  
  // I18n
  public String getName(String locale) {
      String chanName=(String)chanNames.get(locale);
      if (chanName == null) {
          return this.chanName; // fallback on "en_US"
      }  else {
          return chanName;
      }
  }
  
  public String getDescription(String locale) {
      /*
      return chanDesc;
      */
      String chanDesc=(String)chanDescs.get(locale);
      if (chanDesc == null) {
          return this.chanDesc; // fallback on "en_US"
      }  else {
          return chanDesc;
      }
  }
  
  public String getTitle(String locale) {
      /*
      return chanTitle;
      */
      String chanTitle=(String)chanTitles.get(locale);
      if (chanTitle == null) {
          return this.chanTitle; // fallback on "en_US"
      }  else {
          return chanTitle;
      }
  }

  // Setter methods
  public void setFName(String fname) {this.chanFName =fname; }
  public void setName(String name) {this.chanName = name; }
  public void setDescription(String descr) {this.chanDesc = descr; }
  public void setTitle(String title) {this.chanTitle = title; }
  public void setJavaClass(String javaClass) {this.chanClass = javaClass; }
  public void setTimeout(int timeout) {this.chanTimeout = timeout; }
  public void setTypeId(int typeId) {this.chanTypeId = typeId; }
  public void setPublisherId(int publisherId) {this.chanPupblUsrId = publisherId; }
  public void setApproverId(int approvalId) {this.chanApvlId = approvalId; }
  public void setPublishDate(Date publishDate) {this.chanPublDt = publishDate; }
  public void setApprovalDate(Date approvalDate) {this.chanApvlDt = approvalDate; }
  public void setEditable(boolean editable) {this.chanEditable = editable; }
  public void setHasHelp(boolean hasHelp) {this.chanHasHelp = hasHelp; }
  public void setHasAbout(boolean hasAbout) {this.chanHasAbout = hasAbout; }
  public void setIsSecure(boolean isSecure) {this.chanIsSecure = isSecure; }    
  public void setLocale(String locale) {
      if (locale!=null)
          this.chanLocale = locale;
  }
  public void clearParameters() { this.parameters.clear(); }
  public void setParameters(ChannelParameter[] parameters) {
    for (int i = 0; i < parameters.length; i++) {
      this.parameters.put(parameters[i].getName(), parameters[i]);
    }
  }
  public void replaceParameters(ChannelParameter[] parameters) {
    clearParameters();
    setParameters(parameters);
  }
  public void putChanTitles(String locale, String chanTitle) {
      chanTitles.put(locale, chanTitle);
  }
  public void putChanNames(String locale, String chanName) {
      chanNames.put(locale, chanName);
  }
  public void putChanDescs(String locale, String chanDesc) {
      chanDescs.put(locale, chanDesc);
  }
  
  /**
   * Implementation required by IBasicEntity interface.
   * @return EntityIdentifier
   */
  public EntityIdentifier getEntityIdentifier() {
    return new EntityIdentifier(String.valueOf(id), ChannelDefinition.class);
  }

  /**
   * Adds a parameter to this channel definition
   * @param parameter the channel parameter to add
   */      
  public void addParameter(ChannelParameter parameter) {
    parameters.put(parameter.getName(), parameter);
  }
  
  /**
   * Adds a parameter to this channel definition
   * @param name the channel parameter name
   * @param value the channel parameter value
   * @param override the channel parameter override setting
   */   
  public void addParameter(String name, String value, String override) {
    parameters.put(name, new ChannelParameter(name, value,RDBMServices.dbFlag(override)));
  }
  
  /**
   * Removes a parameter from this channel definition
   * @param parameter the channel parameter to remove
   */    
  public void removeParameter(ChannelParameter parameter) {
    removeParameter(parameter.getName());
  }  
  
  /**
   * Removes a parameter from this channel definition
   * @param name the parameter name
   */  
  public void removeParameter(String name) {
    parameters.remove(name);
  }

  /**
   * Get an Element expressing the minimum attributes necessary to represent
   * a channel.
   * @param doc Document that will be the owner of the Element returned
   * @param idTag Value of the identifier for the channel
   * @param chanClassArg fully qualified class name of the channel
   * @param editable true if the channel handles the Edit event
   * @param hasHelp true if the channel handles the Help event
   * @param hasAbout true if the channel handles the About event 
   * @return Element representing the channel
   */
  private Element getBase(Document doc, String idTag, String chanClassArg,
    boolean editable, boolean hasHelp, boolean  hasAbout) {
    Element channel = doc.createElement("channel");
    
    // the ID attribute is the identifier for the Channel element
    channel.setAttribute("ID", idTag);
    channel.setIdAttribute("ID", true);
    
    channel.setAttribute("chanID", this.id + "");
    channel.setAttribute("timeout", this.chanTimeout + "");
    if (this.chanLocale != null) {
        channel.setAttribute("name", getName(this.chanLocale));
        channel.setAttribute("title", getTitle(this.chanLocale));
        channel.setAttribute("locale", this.chanLocale);
    }  else {
        channel.setAttribute("name", this.chanName);
        channel.setAttribute("title", this.chanTitle);
    }
    channel.setAttribute("fname", this.chanFName);
    
    // chanClassArg is so named to highlight that we are using the argument
    // to the method rather than the instance variable chanClass
    channel.setAttribute("class", chanClassArg);
    channel.setAttribute("typeID", this.chanTypeId + "");
    channel.setAttribute("editable", editable ? "true" : "false");
    channel.setAttribute("hasHelp", hasHelp ? "true" : "false");
    channel.setAttribute("hasAbout", hasAbout ? "true" : "false");
    channel.setAttribute("secure", this.chanIsSecure ? "true" : "false");    
    return channel;
  }

  private final Element nodeParameter(Document doc, String name, int value) {
    return nodeParameter(doc, name, Integer.toString(value));
  }

  private final Element nodeParameter(Document doc, String name, String value) {
    Element parameter = doc.createElement("parameter");
    parameter.setAttribute("name", name);
    parameter.setAttribute("value", value);
    return parameter;
  }

  private final void addParameters(Document doc, Element channel) {
    if (parameters != null) {
      Iterator iter = parameters.values().iterator();
      while (iter.hasNext()) {
        ChannelParameter cp = (ChannelParameter)iter.next();

        Element parameter = nodeParameter(doc, cp.name, cp.value);
        if (cp.override) {
          parameter.setAttribute("override", "yes");
        }
        channel.appendChild(parameter);
      }
    }
  }

  /**
   * Display a message where this channel should be
   */
  public Element getDocument(Document doc, String idTag, String statusMsg, int errorId) {
    Element channel = getBase(doc, idTag, "org.jasig.portal.channels.CError", false, false, false);
    addParameters(doc, channel);
    channel.appendChild(nodeParameter(doc, "CErrorMessage", statusMsg));
    channel.appendChild(nodeParameter(doc, "CErrorChanId", idTag));
    channel.appendChild(nodeParameter(doc, "CErrorErrorId", errorId));
    return channel;
  }

  /**
   * return an xml representation of this channel
   */
  public Element getDocument(Document doc, String idTag) {
    Element channel = getBase(doc, idTag, chanClass, chanEditable, chanHasHelp, chanHasAbout);
    channel.setAttribute("description", chanDesc);
    addParameters(doc, channel);
    return channel;
  }

  /**
   * Is it time to reload me from the data store
   */
  public boolean refreshMe() {
    return false;
  }

}

