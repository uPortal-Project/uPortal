/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
 */


package  org.jasig.portal.channels.DLMUserPreferences;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.ResourceBundle;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.GeneralRenderingException;
import org.jasig.portal.PortalException;
import org.jasig.portal.PortalSessionManager;
import org.jasig.portal.ResourceMissingException;
import org.jasig.portal.StylesheetSet;
import org.jasig.portal.ThemeStylesheetDescription;
import org.jasig.portal.UserLayoutStoreFactory;
import org.jasig.portal.UserProfile;
import org.jasig.portal.layout.IUserLayoutStore;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.security.IPerson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.utils.XSLT;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/** 
 * <p>CUserPreferences state for managing profiles</p>
 * @author Peter Kharchenko, peterk@interactivebusiness.com
 * @version $Revision$
 */
class ManageProfilesState extends BaseState {
  private static final Log log = LogFactory.getLog(ManageProfilesState.class);
  
    /**
     * Default value for ALLOW_USER_PROFILES.
     * This value will be used when the relevant property cannot be accessed.
     */
    private static final boolean DEFAULT_ALLOW_USER_PROFILES = true;
    
    /**
     * Default value for ALLOW_SYSTEM_BROWSER_MAPPING.
     * This value will be used when the relevant property cannot be accessed.
     */
    private static final boolean DEFAULT_ALLOW_SYSTEM_BROWSER_MAPPING = true;
    
    /**
     * Default value for ALLOW_NEW_PROFILE_USER_BUTTON.
     * This value will be used when the relevant property cannot be accessed.
     */
    private static final boolean DEFAULT_ALLOW_NEW_PROFILE_BUTTON = true;
  
  protected Hashtable userProfileList;
  protected Hashtable systemProfileList;
  protected Hashtable userExpandStates;
  protected Hashtable systemExpandStates;
  protected ChannelRuntimeData runtimeData;
  IUserLayoutStore ulsdb;

  private static final String bundleLocation = "/org/jasig/portal/channels/ManageProfilesState/ManageProfilesState";
  static final boolean ALLOW_USER_PROFILES=PropertiesManager.getPropertyAsBoolean("org.jasig.portal.channels.UserPreferences.ManageProfilesState.allowUserProfiles", DEFAULT_ALLOW_USER_PROFILES);
  static final boolean ALLOW_SYSTEM_BROWSER_MAPPING=PropertiesManager.getPropertyAsBoolean("org.jasig.portal.channels.UserPreferences.ManageProfilesState.allowSystemProfileBrowserMapping", DEFAULT_ALLOW_SYSTEM_BROWSER_MAPPING);
  static final boolean ALLOW_NEW_PROFILE_BUTTON=PropertiesManager.getPropertyAsBoolean("org.jasig.portal.channels.UserPreferences.ManageProfilesState.allowNewProfileCreation", DEFAULT_ALLOW_NEW_PROFILE_BUTTON);


  public ManageProfilesState(CUserPreferences context) {
    super(context);
    userExpandStates=new Hashtable();
    systemExpandStates=new Hashtable();
  }


  protected Hashtable getUserProfileList() throws PortalException {
    if (userProfileList == null) {
      try {
        userProfileList = this.getUserLayoutStore().getUserProfileList(context.getUserPreferencesManager().getPerson());
      } catch (Exception e) {
        throw new PortalException(e);
      }
    }
    return  userProfileList;
  }


  protected Hashtable getSystemProfileList() throws PortalException {
    if (systemProfileList == null) {
      try {
        systemProfileList = this.getUserLayoutStore().getSystemProfileList();
      } catch (Exception e) {
        throw new PortalException(e);
      }
    }
    return  systemProfileList;
  }


    public void setRuntimeData(ChannelRuntimeData rd) throws PortalException {
        this.runtimeData = rd;
        // local action processing
        String action = runtimeData.getParameter("action");
        if (action != null) {
            String profileId = runtimeData.getParameter("profileId");
            boolean systemProfile = false;
            if (profileId != null) {
                String profileType = runtimeData.getParameter("profileType");
                if (profileType != null && profileType.equals("system"))
                    systemProfile = true;
                if (action.equals("edit")) {
                    // initialize internal edit state
                    CEditProfile epstate = new CEditProfile(this);
                    // clear cached profile list tables
                    userProfileList = systemProfileList = null;
                    epstate.setRuntimeData(rd);
                    internalState = epstate;
                } else if (action.equals("copy")) {
                    // retrieve a profile from the database
                    UserProfile p=null;
                    if(systemProfile) {
                        p=(UserProfile)systemProfileList.get(new Integer(profileId));
                    } else {
                        p=(UserProfile)userProfileList.get(new Integer(profileId));
                    }
                    
                    if(p!=null) {
                        // create a new layout
                        try {
                          p=this.getUserLayoutStore().addUserProfile(context.getUserPreferencesManager().getPerson(),p);
                        } catch (Exception e) {
                          throw new PortalException(e);
                        }
                        // reset user profile listing
                        userProfileList=null;
                    }
                } else if (action.equals("delete")) {
                    // delete a profile
                    if (systemProfile) {
                        // need to check permissions here
                        // context.getUserPreferencesStore().deleteSystemProfile(Integer.parseInt(profileId));
                        // systemProfileList=null;
                    } else {
                      try {
                        this.getUserLayoutStore().deleteUserProfile(context.getUserPreferencesManager().getPerson(), Integer.parseInt(profileId));
                      } catch (Exception e) {
                        throw new PortalException(e);
                      }

                      userProfileList = null;
                    }
                } else if (action.equals("map")) {
                  try {
                    this.getUserLayoutStore().setUserBrowserMapping(context.getUserPreferencesManager().getPerson(), this.runtimeData.getBrowserInfo().getUserAgent(), Integer.parseInt(profileId));
                  } catch (Exception e) {
                    throw new PortalException(e);
                  }
                  // let userPreferencesManager know that the current profile has changed : everything must be reloaded
                } else if (action.equals("changeView")) {
                    String view=runtimeData.getParameter("view");
                    boolean expand=false;
                    if(view.equals("expanded")) expand=true;
                    if(systemProfile) {
                        systemExpandStates.put(profileId,new Boolean(expand));
                    } else {
                        userExpandStates.put(profileId,new Boolean(expand));
                    }
                }
            }

            if(action.equals("newProfile")) {
                // get a copy of a current layout to copy the values from
                UserProfile cp=context.getCurrentUserPreferences().getProfile();
                if(cp!=null) {
                    // create a new profile
                    UserProfile p=new UserProfile(0,"new profile","please edit the profile",cp.getLayoutId(),cp.getStructureStylesheetId(),cp.getThemeStylesheetId());
                    try {
                      p=this.getUserLayoutStore().addUserProfile(context.getUserPreferencesManager().getPerson(),p);
                    } catch (Exception e) {
                      throw new PortalException(e);
                    }

                    // reset user profile listing
                    userProfileList=null;
                }
            } else if(action.equals("condenseAll")) {
                String profileType = runtimeData.getParameter("profileType");
                if (profileType != null && profileType.equals("system")) {
                    // system profiles
                    systemExpandStates.clear();
                } else {
                    // user profiles
                    userExpandStates.clear();
                }
            } else if(action.equals("expandAll")) {
                String profileType = runtimeData.getParameter("profileType");
                if (profileType != null && profileType.equals("system")) {
                    // system profiles
                    systemExpandStates.clear();
                    Boolean expState=new Boolean(true);
                    for (Enumeration upe = this.getSystemProfileList().elements(); upe.hasMoreElements();) {
                        UserProfile p = (UserProfile)upe.nextElement();
                        systemExpandStates.put(Integer.toString(p.getProfileId()),expState);
                    }
                } else {
                    // user profiles
                    userExpandStates.clear();
                    Boolean expState=new Boolean(true);
                    for (Enumeration upe = this.getUserProfileList().elements(); upe.hasMoreElements();) {
                        UserProfile p = (UserProfile)upe.nextElement();
                        userExpandStates.put(Integer.toString(p.getProfileId()),expState);
                    }
                }
            }
        }
        if (internalState != null)
            internalState.setRuntimeData(rd);
    }

 
  private IPerson getPerson() {
    return  context.getUserPreferencesManager().getPerson();
  }

  private StylesheetSet getStylesheetSet() {
    return  context.getStylesheetSet();
  }

 
  private IUserLayoutStore getUserLayoutStore() throws PortalException {
    // Should obtain implementation in a different way!!
    if (ulsdb == null) {
      ulsdb = UserLayoutStoreFactory.getUserLayoutStoreImpl();
    }
    if (ulsdb == null) {
      throw  new ResourceMissingException("", "User Layout database", "Unable to obtain the list of user profiles, since the user preference database is currently down");
    }
    return  ulsdb;
  }

 
  public void renderXML(ContentHandler out) throws PortalException {
    // check if internal state exists, and if not, proceed with the
    // default screen rendering (profile list screen)
    if (internalState != null) {
      internalState.renderXML(out);
    } else {
      Document doc = DocumentFactory.getNewDocument();
      Element edEl = doc.createElement("profiles");
      doc.appendChild(edEl);
      if(ALLOW_USER_PROFILES) {
          // fill out user-defined profiles
          Element uEl = doc.createElement("user");
          Hashtable upList=this.getUserProfileList();

          for(Enumeration upe = this.getUserProfileList().elements(); upe.hasMoreElements();) {
              UserProfile p = (UserProfile)upe.nextElement();
              Element pEl = doc.createElement("profile");
              Boolean expState=(Boolean) userExpandStates.get(Integer.toString(p.getProfileId()));
              if(expState!=null && expState.booleanValue()) {
                  pEl.setAttribute("view","expanded");
              } else {
                  pEl.setAttribute("view","condensed");
              }
              pEl.setAttribute("id", Integer.toString(p.getProfileId()));
              pEl.setAttribute("name", p.getProfileName());
              Element dEl = doc.createElement("description");
              dEl.appendChild(doc.createTextNode(p.getProfileDescription()));
              pEl.appendChild(dEl);
              uEl.appendChild(pEl);
          }
          edEl.appendChild(uEl);
      }
      // fill out system-defined profiles
      Element sEl = doc.createElement("system");
      for (Enumeration spe = this.getSystemProfileList().elements(); spe.hasMoreElements();) {
        UserProfile p = (UserProfile)spe.nextElement();
        Element pEl = doc.createElement("profile");

        Boolean expState=(Boolean)systemExpandStates.get(Integer.toString(p.getProfileId()));
        if(expState!=null && expState.booleanValue()) {
            pEl.setAttribute("view","expanded");
        } else {
            pEl.setAttribute("view","condensed");
        }
        pEl.setAttribute("id", Integer.toString(p.getProfileId()));
        pEl.setAttribute("name", p.getProfileName());
        Element dEl = doc.createElement("description");
        dEl.appendChild(doc.createTextNode(p.getProfileDescription()));
        pEl.appendChild(dEl);
        sEl.appendChild(pEl);
      }
      edEl.appendChild(sEl);
      /*  try {
       log.debug(org.jasig.portal.utils.XML.serializeNode(doc));
       } catch (Exception e) {
       log.error(e);
       }
       */
      // debug printout of the document sent to the XSLT
      /*
      StringWriter dbwr1 = new StringWriter();
      org.apache.xml.serialize.OutputFormat outputFormat = new org.apache.xml.serialize.OutputFormat();
      outputFormat.setIndenting(true);
      org.apache.xml.serialize.XMLSerializer dbser1 = new org.apache.xml.serialize.XMLSerializer(dbwr1, outputFormat);
      try {
          dbser1.serialize(doc);
      log.debug("ManageProfilesState::renderXML() : XML incoming to the XSLT :\n\n" + dbwr1.toString() + "\n\n");
      } catch (Exception e) {
          log.debug("ManageProfilesState::renderXML() : problems serializing incoming XML");
      }
      */

      // find the stylesheet and transform
      StylesheetSet set = context.getStylesheetSet();
      if (set == null)
        throw  new GeneralRenderingException("Unable to determine the stylesheet list");

      String xslURI = set.getStylesheetURI("profileList", runtimeData.getBrowserInfo());
      UserProfile currentProfile = context.getCurrentUserPreferences().getProfile();
      Hashtable params = new Hashtable();

      params.put("allowNewProfile",new Boolean(ALLOW_NEW_PROFILE_BUTTON));
      params.put("allowSystemProfileMapping",new Boolean(ALLOW_SYSTEM_BROWSER_MAPPING));


      params.put("baseActionURL", runtimeData.getBaseActionURL());
      params.put("profileId", Integer.toString(currentProfile.getProfileId()));
      if (currentProfile.isSystemProfile()) {
          params.put("profileType", "system");
      } else {
          params.put("profileType", "user");
      }

      if (xslURI != null) {
        
      	ResourceBundle l18n = ResourceBundle.getBundle(bundleLocation,runtimeData.getLocales()[0]);

      	//Now perform the transformation
		XSLT xslt = XSLT.getTransformer(this, runtimeData.getLocales());
		xslt.setResourceBundle(l18n);

		xslt.setXML(doc);
        xslt.setXSL(this.getClass().getResource(xslURI).toString());
        xslt.setTarget(out);
        xslt.setStylesheetParameters(params);
        xslt.transform();
      } else {
          throw  new ResourceMissingException("", "stylesheet", "Unable to find stylesheet to display content for this media");
      }
    }
  }

  /*
   * This state corresponds to an "edit profile" screen.
   */
  protected class CEditProfile extends BaseState {
    ChannelRuntimeData runtimeData;
    protected ManageProfilesState context;
    protected String currentMimeType;
    protected UserProfile profile;              // profile currently being edited
    protected boolean modified = false;
    // location of the properties file relative to the portal base dir.
    protected static final String mimeImagesPropsFile = "media/org/jasig/portal/channels/CUserPreferences/mimeImages.properties";
    protected Properties mimeImagesProps = new Properties();


    public CEditProfile(ManageProfilesState context) {
      // load the mimetype image properties file
      java.io.InputStream in = null;
      try {
        in = PortalSessionManager.getResourceAsStream(mimeImagesPropsFile);
        mimeImagesProps.load(in);
      } catch (Exception e) {
        log.error( "Unable to load mime type images properties file located at " + mimeImagesPropsFile);
      } finally {
				try {
					if (in != null)
						in.close();
				} catch (IOException ioe) {
              log.error( "Unable to close InputStream " + ioe);
				}
      }
      this.context = context;
    }

    public void setRuntimeData(ChannelRuntimeData rd) throws PortalException {
      this.runtimeData = rd;
      // internal state handling
      String action = runtimeData.getParameter("action");
      if (action != null) {
        if (action.equals("edit")) {
          // this is an action from the initial profile listing screen
          // At this point we're supposed to pick up which profile is to be
          // edited.
          Integer profileId = null;
          try {
            profileId = new Integer(runtimeData.getParameter("profileId"));
          } catch (NumberFormatException nfe) {}
          boolean systemProfile = false;
          if (profileId == null) {
            // return back to the base state if the profile hasn't been specified
            context.setState(null);
          } else {
            String profileType = runtimeData.getParameter("profileType");
            if (profileType == null) {
              // return to the profile listing
              context.setState(null);
            } else {
              if (profileType.equals("system"))
                systemProfile = true;
              // find the UserProfile
              try {
                if (systemProfile) {
                  profile = context.getUserLayoutStore().getSystemProfileById(profileId.intValue());
                } else {
                    profile = context.getUserLayoutStore().getUserProfileById(context.getPerson(), profileId.intValue());
                }
              } catch (Exception e) {
                throw new PortalException(e);
              }
              
              if (profile == null) {
                // failed to find the specified profile, return to the base state
                context.setState(null);
              }
            }
          }
        } else if (action.equals("completeEdit")) {
          if (runtimeData.getParameter("submitCancel") != null) {
            // cancel button has been hit
            context.setState(null);
          } else if (runtimeData.getParameter("submitSave") != null) {
            // save changes
            profile.setProfileName(runtimeData.getParameter("profileName"));
            profile.setProfileDescription(runtimeData.getParameter("profileDescription"));
            // determine new theme stylesheet id
            int newId = Integer.parseInt(runtimeData.getParameter("stylesheetID"));
            if (newId != profile.getThemeStylesheetId()) {
              profile.setThemeStylesheetId(newId);
              // see if the mime type has changed, alert user
            }
            try {
                if (profile.isSystemProfile()) {
                    // only administrative users should be able to do this
                    context.getUserLayoutStore().updateSystemProfile(profile);
                } else {
                    context.getUserLayoutStore().updateUserProfile(context.getPerson(), profile);
                }
            } catch (Exception e) {
              throw new PortalException(e);
            }
            context.setState(null);
          }
        }
      }
    }

 
    public void renderXML(ContentHandler out) throws PortalException {
      // construct gpref XML
      Document doc = DocumentFactory.getNewDocument();
      Element profileEl = doc.createElement("profile");
      if (this.modified) {
          profileEl.setAttribute("modified", "true");
      } else {
          profileEl.setAttribute("modified", "false");
      }

      // add profile name and description
      {
        Element pnameEl = doc.createElement("name");
        pnameEl.appendChild(doc.createTextNode(profile.getProfileName()));
        profileEl.appendChild(pnameEl);
        Element pdescrEl = doc.createElement("description");
        pdescrEl.appendChild(doc.createTextNode(profile.getProfileDescription()));
        profileEl.appendChild(pdescrEl);
      }
      /*
       // process mime type information
       {
       Element mimeEl=doc.createElement("mimetypes");
       Hashtable mimeTypeList=context.getCoreStylesheetDescriptionDB().getMimeTypeList();
       if(mimeTypeList==null) throw new ResourceMissingException("","Mime type list","Unable to retreive a listing of mime types available at this installation.");
       // determine mime type currently assigned to this profile
       Element cmtEl=doc.createElement("current");
       if(currentMimeType==null) {
       // first rendering, mime type needs to be acquired from the theme stylesheet description
       if(profile.getThemeStylesheetName()!=null) {
       ThemeStylesheetDescription tsd=context.getCoreStylesheetDescriptionDB().getThemeStylesheetDescription(profile.getThemeStylesheetName());
       if(tsd==null) {
       throw new ResourceMissingException("","Description of stylesheet \""+profile.getThemeStylesheetName()+"\"","Unable to retreive description of the theme stylesheet associated with the profile being edited.");
       }
       currentMimeType=tsd.getMimeType();
       } else {
       // may be this is a new profile, and nothing has been assigned yet, in this case set the default mimeType to text/html
       currentMimeType="text/html";
       }
       }
       Element cmtnEl=doc.createElement("name");
       cmtnEl.appendChild(doc.createTextNode(currentMimeType));
       Element cmtdEl=doc.createElement("description");
       cmtdEl.appendChild(doc.createTextNode((String)mimeTypeList.get(currentMimeType)));
       cmtEl.appendChild(cmtnEl);
       cmtEl.appendChild(cmtdEl);
       mimeEl.appendChild(cmtEl);
       // list alternative mime types
       // first, remove the current one from the alternate listing
       mimeTypeList.remove(currentMimeType);
       for(Enumeration me=mimeTypeList.keys();me.hasMoreElements();) {
       Element altEl=doc.createElement("alternate");
       String mimeType=(String)me.nextElement();
       Element altnEl=doc.createElement("name");
       altnEl.appendChild(doc.createTextNode(mimeType));
       Element altdEl=doc.createElement("description");
       altdEl.appendChild(doc.createTextNode((String)mimeTypeList.get(mimeType)));
       altEl.appendChild(altnEl);
       altEl.appendChild(altdEl);
       mimeEl.appendChild(altEl);
       }
       profileEl.appendChild(mimeEl);
       }
       // deal with structure stylesheets
       {
       Element structEl=doc.createElement("structurestylesheets");
       Hashtable ssList=context.getCoreStylesheetDescriptionDB().getStructureStylesheetList(currentMimeType);
       if(ssList==null) throw new ResourceMissingException("","List of structure stylesheets for the mimeType=\""+currentMimeType+"\"","Unable to obtain a list of structure stylesheets supporting specified mime type");
       // see if the current structure stylesheet is still in the listing
       if(ssList.get(profile.getStructureStylesheetName())==null) {
       if(!ssList.isEmpty()) {
       // assign a first one in the table as a current
       Enumeration e=ssList.keys();
       profile.setStructureStylesheetName((String)e.nextElement());
       } else {
       // no alternatives :(
       profile.setStructureStylesheetName(null);
       }
       }
       // if any theme stylesheet is currently assigned
       if(profile.getStructureStylesheetName()!=null) {
       Element cssEl=doc.createElement("current");
       Element cssnEl=doc.createElement("name");
       cssnEl.appendChild(doc.createTextNode(profile.getStructureStylesheetName()));
       Element cssdEl=doc.createElement("description");
       cssdEl.appendChild(doc.createTextNode((String)ssList.get(profile.getStructureStylesheetName())));
       cssEl.appendChild(cssnEl);
       cssEl.appendChild(cssdEl);
       structEl.appendChild(cssEl);
       // remove the current one from the alternate listing
       ssList.remove(profile.getStructureStylesheetName());
       }
       // list alternative structure stylesheets
       for(Enumeration me=ssList.keys();me.hasMoreElements();) {
       Element altEl=doc.createElement("alternate");
       String ssName=(String)me.nextElement();
       Element altnEl=doc.createElement("name");
       altnEl.appendChild(doc.createTextNode(ssName));
       Element altdEl=doc.createElement("description");
       altdEl.appendChild(doc.createTextNode((String)ssList.get(ssName)));
       altEl.appendChild(altnEl);
       altEl.appendChild(altdEl);
       structEl.appendChild(altEl);
       }
       profileEl.appendChild(structEl);
       }*/


      // deal with theme stylesheets
      {
        Element themeEl = doc.createElement("themestylesheets");
        Hashtable tsList;
        try {
          tsList = context.getUserLayoutStore().getThemeStylesheetList(profile.getStructureStylesheetId());
        } catch (Exception e) {
          throw new PortalException(e);
        }
        if (tsList == null) {
            throw  new ResourceMissingException("", "List of theme stylesheets for the structure stylesheet \"" + profile.getStructureStylesheetId()+ "\"", "Unable to obtain a list of theme stylesheets for the specified structure stylesheet");
        }

        // see if the current Theme stylesheet is still in the list, otherwise assign a first one in the hastable
        if (tsList.get(new Integer(profile.getThemeStylesheetId())) == null) {
          if (!tsList.isEmpty()) {
            Enumeration e = tsList.keys();
            profile.setThemeStylesheetId(((Integer)e.nextElement()).intValue());
          }
          else {
          //                        profile.setThemeStylesheetId(-1);
          }
        }

        for (Enumeration me = tsList.keys(); me.hasMoreElements();) {
          Integer ssId = (Integer)me.nextElement();
          // check if the stylesheet is current
          boolean current = (ssId.intValue() == profile.getThemeStylesheetId());
          Element altEl;
          if (current) {
              altEl = doc.createElement("current");
          } else {
              altEl = doc.createElement("alternate");
          }

          ThemeStylesheetDescription tsd = (ThemeStylesheetDescription)tsList.get(ssId);
          Element altnEl = doc.createElement("name");
          altnEl.appendChild(doc.createTextNode(tsd.getStylesheetName()));
          Element altidEl = doc.createElement("id");
          altidEl.appendChild(doc.createTextNode(Integer.toString(tsd.getId())));
          Element altdEl = doc.createElement("description");
          altdEl.appendChild(doc.createTextNode(tsd.getStylesheetWordDescription()));
          Element altmEl = doc.createElement("mimetype");
          altmEl.appendChild(doc.createTextNode(tsd.getMimeType()));
          // determine device icon
          String deviceIconURI;
          if ((deviceIconURI = mimeImagesProps.getProperty(tsd.getDeviceType())) == null) {
              deviceIconURI = mimeImagesProps.getProperty("unknown");
          }
          Element altdiuEl = doc.createElement("deviceiconuri");
          altdiuEl.appendChild(doc.createTextNode(deviceIconURI));
          Element altsuEl = doc.createElement("sampleuri");
          if (tsd.getSamplePictureURI() == null || tsd.getSamplePictureURI().equals("")) {
              altsuEl.appendChild(doc.createTextNode(""));
          } else {
              altsuEl.appendChild(doc.createTextNode(tsd.getSamplePictureURI()));
          }

          Element altsiuEl = doc.createElement("sampleiconuri");
          if (tsd.getSampleIconURI() == null || tsd.getSampleIconURI().equals("")) {
              altsiuEl.appendChild(doc.createTextNode(""));
          } else {
              altsiuEl.appendChild(doc.createTextNode(tsd.getSampleIconURI()));
          }

          altEl.appendChild(altnEl);
          altEl.appendChild(altidEl);
          altEl.appendChild(altdEl);
          altEl.appendChild(altmEl);
          altEl.appendChild(altdiuEl);
          altEl.appendChild(altsuEl);
          altEl.appendChild(altsiuEl);
          themeEl.appendChild(altEl);
        }
        profileEl.appendChild(themeEl);
      }
      doc.appendChild(profileEl);
      // debug printout of the prepared xml
      if (log.isDebugEnabled()) {
      try {
          StringWriter outString = new StringWriter();
                    /*
                     * This should be reviewed at some point to see if we can
                     * use the DOM3 LS capability and hence a standard way of
                     * doing this rather than using an internal implementation
                     * class.
           */
          OutputFormat format = new OutputFormat();
          format.setOmitXMLDeclaration(true);
          format.setIndenting(true);
                    XMLSerializer serializer = new XMLSerializer(outString,
                            format);
                    serializer.serialize(doc);
                    log.debug(outString.toString());
      } catch (Exception e) {
                    log.debug(e);
      }
      }
      // end debug block
      
      StylesheetSet set = context.getStylesheetSet();
      if (set == null)
        throw  new GeneralRenderingException("Unable to determine the stylesheet list");
      String xslURI = set.getStylesheetURI("editProfile", runtimeData.getBrowserInfo());
      if (xslURI != null) {

      	ResourceBundle l18n = ResourceBundle.getBundle(bundleLocation,runtimeData.getLocales()[0]);
      	
      	//Now perform the transformation
      	XSLT xslt = XSLT.getTransformer(this, runtimeData.getLocales());
      	xslt.setResourceBundle(l18n);

      	xslt.setXML(doc);
        xslt.setXSL(this.getClass().getResource(xslURI).toString());
        xslt.setTarget(out);
        xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
        xslt.transform();
      } else {
          throw  new ResourceMissingException("", "stylesheet", "Unable to find stylesheet to display content for this media");
      }
    }
  }
}



