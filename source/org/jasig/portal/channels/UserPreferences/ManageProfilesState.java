/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jasig.portal.channels.UserPreferences;

import org.jasig.portal.*;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.utils.XSLT;
import org.xml.sax.DocumentHandler;
import java.util.*;
import javax.servlet.http.*;
import org.w3c.dom.*;
import org.apache.xalan.xslt.*;
import java.io.StringWriter;
import java.net.URL;

/** <p>CUserPreferences state for managing profiles</p>
 * @author Peter Kharchenko, peterk@interactivebusiness.com
 * @version $Revision$
 */


class ManageProfilesState extends BaseState {
    protected IUserPreferencesDB updb;
    protected Hashtable userProfileList;
    protected Hashtable systemProfileList;
    protected ChannelRuntimeData runtimeData;
    ICoreStylesheetDescriptionDB csddb;

    public ManageProfilesState(CUserPreferences context) {
        super(context);
    }

    protected Hashtable getUserProfileList() throws PortalException {
        if(userProfileList==null)
            userProfileList=this.getUserPreferencesDB().getUserProfileList(context.getUserLayoutManager().getPerson().getID());
        return userProfileList;
    }

    protected Hashtable getSystemProfileList() throws PortalException {
        if(systemProfileList==null)
            systemProfileList=this.getUserPreferencesDB().getSystemProfileList();
        return systemProfileList;
    }

    public void setRuntimeData(ChannelRuntimeData rd) throws PortalException {
        this.runtimeData=rd;
        // local action processing
        String action=runtimeData.getParameter("action");
        if(action!=null) {
            String profileId=runtimeData.getParameter("profileId");
            boolean systemProfile=false;
            if(profileId!=null) {
                String profileType=runtimeData.getParameter("profileType");
                if(profileType!=null && profileType.equals("system")) systemProfile=true;
                if(action.equals("edit")) {
                    // initialize internal edit state
                    CEditProfile epstate=new CEditProfile(this);
                    // clear cached profile list tables
                    userProfileList=systemProfileList=null;
                    epstate.setRuntimeData(rd);
                    internalState=epstate;
                } else if(action.equals("delete")) {
                    // delete a profile
                    if(systemProfile) {
                        // need to check permissions here
                        //			context.getUserPreferencesDB().deleteSystemProfile(Integer.parseInt(profileId));
                        //			systemProfileList=null;
                    } else {
                        this.getUserPreferencesDB().deleteUserProfile(context.getUserLayoutManager().getPerson().getID(),Integer.parseInt(profileId));
                        userProfileList=null;
                    }
                } else if(action.equals("map")) {
                    this.getUserPreferencesDB().setUserBrowserMapping(context.getUserLayoutManager().getPerson().getID(),this.runtimeData.getBrowserInfo().getUserAgent(),Integer.parseInt(profileId));
                    // let userLayoutManager know that the current profile has changed : everything must be reloaded

                }
            }


        }
        if(internalState!=null) internalState.setRuntimeData(rd);
    }

    private IPerson getPerson() {
        return context.getUserLayoutManager().getPerson();
    }

    private StylesheetSet getStylesheetSet() {
        return context.getStylesheetSet();
    }

    private IUserPreferencesDB getUserPreferencesDB() throws PortalException {
        if(updb==null) updb=new UserPreferencesDBImpl();
        if(updb==null) throw new ResourceMissingException("","User preference database","Unable to obtain the list of user profiles, since the user preference database is currently down");
        return updb;
    }

    public ICoreStylesheetDescriptionDB getCoreStylesheetDescriptionDB() throws PortalException {
        if (csddb==null) csddb=new CoreStylesheetDescriptionDBImpl();
        if (csddb==null) throw new ResourceMissingException("","Stylesheet description database","Unable to obtain the list of available stylesheets since the database holding them is not avaiable.");
        return csddb;
    }

    public void renderXML(DocumentHandler out) throws PortalException {
        // check if internal state exists, and if not, proceed with the
        // default screen rendering (profile list screen)
        if(internalState!=null) {
            internalState.renderXML(out);
        } else {
            Document doc = new org.apache.xerces.dom.DocumentImpl();
            Element edEl=doc.createElement("profiles");
            doc.appendChild(edEl);
            // fill out user-defined profiles
            Element uEl=doc.createElement("user");
            for(Enumeration upe=this.getUserProfileList().elements(); upe.hasMoreElements(); ) {
                UserProfile p=(UserProfile) upe.nextElement();
                Element pEl=doc.createElement("profile");
                pEl.setAttribute("id",Integer.toString(p.getProfileId()));
                pEl.setAttribute("name",p.getProfileName());
                Element dEl=doc.createElement("description");
                dEl.appendChild(doc.createTextNode(p.getProfileDescription()));
                pEl.appendChild(dEl);
                uEl.appendChild(pEl);
            }
            edEl.appendChild(uEl);
            // fill out system-defined profiles
            Element sEl=doc.createElement("system");
            for(Enumeration spe=this.getSystemProfileList().elements(); spe.hasMoreElements(); ) {
                UserProfile p=(UserProfile) spe.nextElement();
                Element pEl=doc.createElement("profile");
                pEl.setAttribute("id",Integer.toString(p.getProfileId()));
                pEl.setAttribute("name",p.getProfileName());
                Element dEl=doc.createElement("description");
                dEl.appendChild(doc.createTextNode(p.getProfileDescription()));
                pEl.appendChild(dEl);
                sEl.appendChild(pEl);
            }
            edEl.appendChild(sEl);

            /*  try {
                Logger.log(Logger.DEBUG,UtilitiesBean.dom2PrettyString(doc));
            } catch (Exception e) {
                Logger.log(Logger.ERROR,e);
                }
            */

            // find the stylesheet and transform
            StylesheetSet set=context.getStylesheetSet();
            if(set==null)
                throw new GeneralRenderingException("Unable to determine the stylesheet list");
            String xslURI=runtimeData.getStylesheetURI("profileList", set);

            UserProfile currentProfile=context.getCurrentUserPreferences().getProfile();

            Hashtable params=new Hashtable();

            params.put("baseActionURL", runtimeData.getBaseActionURL());
            params.put("profileId", Integer.toString(currentProfile.getProfileId()));
            if(currentProfile.isSystemProfile()) params.put("profileType","system");
            else params.put("profileType","user");

            if (xslURI != null) {
                try {
                    XSLT.transform(doc, new URL(xslURI), out, params);
                } catch (org.xml.sax.SAXException e) {
                    throw new GeneralRenderingException("Unable to complete transformation");
                } catch (java.io.IOException i) {
                    throw new GeneralRenderingException("IOException has been encountered");
                }
            } else throw new ResourceMissingException("","stylesheet","Unable to find stylesheet to display content for this media");
        }
    }

    /*
     * This state corresponds to an "edit profile" screen.
     */
    protected class CEditProfile extends BaseState {
        ChannelRuntimeData runtimeData;
        protected ManageProfilesState context;
        protected String currentMimeType;
        protected UserProfile profile;         // profile currently being edited
        protected boolean modified=false;

	// location of the properties file relative to the portal base dir.
	protected static final String mimeImagesPropsFile="webpages/media/org/jasig/portal/channels/CUserPreferences/mimeImages.properties";
	protected Properties mimeImagesProps=new Properties();


        public CEditProfile(ManageProfilesState context) {
	    // load the mimetype image properties file
	    try {
		java.io.FileInputStream in = new java.io.FileInputStream(GenericPortalBean.getPortalBaseDir()+java.io.File.separator+mimeImagesPropsFile);
		mimeImagesProps.load(in);
		in.close();
	    } catch (Exception e) {
		Logger.log(Logger.ERROR,"UserPreferences:ManagerProfileState:CEditProfile::CEditProfile() : unable to load mime type images properties file located at "+GenericPortalBean.getPortalBaseDir()+java.io.File.separator+mimeImagesPropsFile);
	    }
            this.context=context;
        }

        public void setRuntimeData(ChannelRuntimeData rd) throws PortalException {
            this.runtimeData=rd;
            // internal state handling
            String action = runtimeData.getParameter ("action");
            if (action != null) {
                if (action.equals("edit")) {
                    // this is an action from the initial profile listing screen
                    // At this point we're supposed to pick up which profile is to be
                    // edited.
                    Integer profileId=null;
                    try {
                        profileId=new Integer(runtimeData.getParameter("profileId"));
                    } catch (NumberFormatException nfe) {}

                    boolean systemProfile=false;
                    if(profileId==null) {
                        // return back to the base state if the profile hasn't been specified
                        context.setState(null);
                    } else {
                        String profileType=runtimeData.getParameter("profileType");
                        if(profileType==null) {
                            // return to the profile listing
                            context.setState(null);
                        } else {
                            if(profileType.equals("system")) systemProfile=true;
                            // find the UserProfile
                            if(systemProfile) {
                                profile=context.getUserPreferencesDB().getSystemProfileById(profileId.intValue());
                            } else {
                                profile=context.getUserPreferencesDB().getUserProfileById(context.getPerson().getID(),profileId.intValue());
                            }
                            if(profile==null) {
                                // failed to find the specified profile, return to the base state
                                context.setState(null);
                            }
                        }
                    }
                } else if (action.equals ("completeEdit")) {
		    if(runtimeData.getParameter("submitCancel")!=null) {
			// cancel button has been hit
			context.setState(null);
		    } else if(runtimeData.getParameter("submitSave")!=null) {
			// save changes
			profile.setProfileName(runtimeData.getParameter("profileName"));
			profile.setProfileDescription(runtimeData.getParameter("profileDescription"));
			// determine new theme stylesheet id
			int newId=Integer.parseInt(runtimeData.getParameter("stylesheetID"));
			if(newId!=profile.getThemeStylesheetId()) {
			    profile.setThemeStylesheetId(newId);
			    // see if the mime type has changed, alert user

			}

                        if(profile.isSystemProfile())
                            // only administrative users should be able to do this
                            context.getUserPreferencesDB().updateSystemProfile(profile);
                        else
                            context.getUserPreferencesDB().updateUserProfile(context.getPerson().getID(),profile);
                        context.setState(null);
		    }
                }
            }
        }


        public void renderXML (DocumentHandler out) throws PortalException {
            // construct gpref XML
            Document doc = new org.apache.xerces.dom.DocumentImpl();
            Element profileEl=doc.createElement("profile");

            if(this.modified)
                profileEl.setAttribute("modified","true");
            else
                profileEl.setAttribute("modified","false");

            // add profile name and description
            {
                Element pnameEl=doc.createElement("name");
                pnameEl.appendChild(doc.createTextNode(profile.getProfileName()));
                profileEl.appendChild(pnameEl);
                Element pdescrEl=doc.createElement("description");
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
                Element themeEl=doc.createElement("themestylesheets");
		Hashtable tsList=context.getCoreStylesheetDescriptionDB().getThemeStylesheetList(profile.getStructureStylesheetId());
                if(tsList==null) throw new ResourceMissingException("","List of theme stylesheets for the structure stylesheet \""+profile.getStructureStylesheetId()+"\"","Unable to obtain a list of theme stylesheets for the specified structure stylesheet");
                // see if the current Theme stylesheet is still in the list, otherwise assign a first one in the hastable
                if(tsList.get(new Integer(profile.getThemeStylesheetId()))==null) {
                    if(!tsList.isEmpty()) {
                        Enumeration e=tsList.keys();
                        profile.setThemeStylesheetId(((Integer)e.nextElement()).intValue());
                    } else {
			//                        profile.setThemeStylesheetId(-1);
		    }
                }

                for(Enumeration me=tsList.keys();me.hasMoreElements();) {
                    Integer ssId=(Integer)me.nextElement();

		    // check if the stylesheet is current
		    boolean current=(ssId.intValue()==profile.getThemeStylesheetId());
		    Element altEl;
		    if(current)
			altEl=doc.createElement("current");
		    else
			altEl=doc.createElement("alternate");

		    ThemeStylesheetDescription tsd=(ThemeStylesheetDescription) tsList.get(ssId);
                    Element altnEl=doc.createElement("name");
                    altnEl.appendChild(doc.createTextNode(tsd.getStylesheetName()));
                    Element altidEl=doc.createElement("id");
                    altidEl.appendChild(doc.createTextNode(Integer.toString(tsd.getId())));

                    Element altdEl=doc.createElement("description");

                    altdEl.appendChild(doc.createTextNode(tsd.getStylesheetWordDescription()));
		    Element altmEl=doc.createElement("mimetype");
		    altmEl.appendChild(doc.createTextNode(tsd.getMimeType()));

		    // determine device icon
		    String deviceIconURI;
		    if((deviceIconURI=mimeImagesProps.getProperty(tsd.getDeviceType()))==null)
			deviceIconURI=mimeImagesProps.getProperty("unknown");
		    Element altdiuEl=doc.createElement("deviceiconuri");
		    altdiuEl.appendChild(doc.createTextNode(deviceIconURI));

		    Element altsuEl=doc.createElement("sampleuri");
		    if(tsd.getSamplePictureURI()==null || tsd.getSamplePictureURI().equals(""))
			altsuEl.appendChild(doc.createTextNode(""));
		    else
			altsuEl.appendChild(doc.createTextNode(tsd.getSamplePictureURI()));

		    Element altsiuEl=doc.createElement("sampleiconuri");
		    if(tsd.getSampleIconURI()==null || tsd.getSampleIconURI().equals(""))
			altsiuEl.appendChild(doc.createTextNode(""));
		    else
			altsiuEl.appendChild(doc.createTextNode(tsd.getSampleIconURI()));


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
            try {
                StringWriter outString = new StringWriter ();
                org.apache.xml.serialize.OutputFormat format=new org.apache.xml.serialize.OutputFormat();
                format.setOmitXMLDeclaration(true);
                format.setIndenting(true);
                org.apache.xml.serialize.XMLSerializer xsl = new org.apache.xml.serialize.XMLSerializer (outString,format);
                xsl.serialize (doc);
                Logger.log(Logger.DEBUG,outString.toString());
            } catch (Exception e) {
                Logger.log(Logger.DEBUG,e);
            }


            StylesheetSet set=context.getStylesheetSet();
            if(set==null)
                throw new GeneralRenderingException("Unable to determine the stylesheet list");
            String xslURI=null;
            xslURI=runtimeData.getStylesheetURI("editProfile", set);

            Hashtable params=new Hashtable();
            params.put("baseActionURL", runtimeData.getBaseActionURL());

            if (xslURI != null) {
                try {
                    XSLT.transform(doc, new URL(xslURI), out, params);
                } catch (org.xml.sax.SAXException e) {
                    throw new GeneralRenderingException("Unable to complete transformation");
                } catch (java.io.IOException i) {
                    throw new GeneralRenderingException("IOException has been encountered");
                }
            } else throw new ResourceMissingException("","stylesheet","Unable to find stylesheet to display content for this media");
        }

    }
}
