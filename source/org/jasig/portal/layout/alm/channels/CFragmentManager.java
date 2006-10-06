/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/


package org.jasig.portal.layout.alm.channels;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Collection;
import java.util.HashMap;

import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.IServant;
import org.jasig.portal.channels.groupsmanager.CGroupsManagerServantFactory;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.AuthorizationException;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalException;
import org.jasig.portal.layout.alm.ALFragment;
import org.jasig.portal.layout.alm.ALNode;
import org.jasig.portal.layout.alm.IALFolderDescription;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.utils.CommonUtils;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.utils.SAX2FilterImpl;
import org.jasig.portal.utils.XSLT;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;


/**
 * A channel for adding new content to a layout.
 * 
 * Prior to uPortal 2.5, this channel existed in the package 
 * org.jasig.portal.layout.channels.  It was moved to its present package to reflect that
 * it is part of Aggregated Layouts.
 * 
 * @author Michael Ivanov, mvi@immagic.com
 * @version $Revision$
 * @since uPortal 2.5
 */
public class CFragmentManager extends FragmentManager {

	private static final String sslLocation = "/org/jasig/portal/channels/CFragmentManager/CFragmentManager.ssl";
	private boolean servantRender = false;
	private String servantFragmentId;
	private static Map servants = new HashMap();
	private final static int MAX_SERVANTS = 50; 

	public CFragmentManager() throws PortalException {
		super();
	}
	
	private class ServantSAXFilter extends SAX2FilterImpl {

		 public ServantSAXFilter(ContentHandler ch) throws PortalException {
			super(ch);
		 }
		   
		 public void startElement (String uri, String localName, String qName,  Attributes atts) throws SAXException {
		  try {	
			super.startElement(uri,localName,qName,atts);
			if(qName.equals("groupServant")) {
			  if ( servantRender ) {
			   getGroupServant().renderXML(contentHandler);
			  } 
			}	
			
		  } catch ( Exception e ) {
		  	  throw new SAXException(e.toString());	
		  }
		 }	

	}

	private synchronized IGroupMember[] getGroupMembers(String fragmentId) throws PortalException {
		Collection groupKeys = alm.getPublishGroups(fragmentId);
		ArrayList members = new ArrayList();
		int i = 0;
		IGroupMember member;
		for ( Iterator keys = groupKeys.iterator(); keys.hasNext(); i++ ) {
			String groupKey = (String) keys.next();
			member = GroupService.findGroup(groupKey);
			if (member != null){
				members.add(member);
			}else{
				log.warn("Unable to find group member for the groupKey: "+groupKey);
			}
		}
		return (IGroupMember[])members.toArray(new IGroupMember[0]);
	}   


    private String getServantKey() {
      return CommonUtils.envl(servantFragmentId,"new"); 	
    }

	/**
		 * Produces a group servant
		 * @return the group servant
		 */
	private synchronized IServant getGroupServant() throws PortalException {
		if ( servants.size() > MAX_SERVANTS ) servants.clear();	
		IServant groupServant = (IServant) servants.get(getServantKey());
		if ( groupServant == null ) {
				 try {
				  // create the appropriate servant
				  if ( servantFragmentId != null && CommonUtils.parseInt(servantFragmentId) > 0  ) {
					IGroupMember[] members = getGroupMembers(servantFragmentId);
					groupServant = CGroupsManagerServantFactory.getGroupsServantforSelection(staticData,
													"Please select groups or people who should have access to this fragment:",
													GroupService.EVERYONE,true,true,members);	
				  } else 
					groupServant = CGroupsManagerServantFactory.getGroupsServantforSelection(staticData,
								"Please select groups or people who should have access to this fragment:",
								GroupService.EVERYONE);		
								
				  if ( groupServant != null ) 
				     servants.put(getServantKey(),groupServant);	
				  									
				} catch (Exception e) {
					throw new PortalException(e);
				}
		}	
		 groupServant.setRuntimeData((ChannelRuntimeData)runtimeData.clone());		  
		 return groupServant;
    }

	private String createFolder( ALFragment fragment ) throws PortalException {
		IALFolderDescription folderDesc = (IALFolderDescription)alm.createNodeDescription(IUserLayoutNodeDescription.FOLDER);
		folderDesc.setName("Fragment column");
		folderDesc.setFragmentId(fragment.getId());
		return alm.addNode(folderDesc, getFragmentRootId(fragment.getId()), null).getId();
	}

	protected void analyzeParameters( XSLT xslt ) throws PortalException {
		
		String fragmentId = CommonUtils.nvl(runtimeData.getParameter("uPcFM_selectedID"));
		String action = CommonUtils.nvl(runtimeData.getParameter("uPcFM_action"));
		  
				if (action.equals("save_new")) {
					String fragmentName = runtimeData.getParameter("fragment_name");
					String funcName = runtimeData.getParameter("fragment_fname");
					String fragmentDesc = runtimeData.getParameter("fragment_desc");
					String fragmentType = runtimeData.getParameter("fragment_type");
					String fragmentFolder = runtimeData.getParameter("fragment_add_folder");
					boolean isPushedFragment = ("pushed".equals(fragmentType))?true:false;
					fragmentId = alm.createFragment(CommonUtils.nvl(funcName),CommonUtils.nvl(fragmentDesc),CommonUtils.nvl(fragmentName));
					ALFragment newFragment = (ALFragment) alm.getFragment(fragmentId);
					if ( newFragment != null ) { 
					  if ( isPushedFragment ) 
					    newFragment.setPushedFragment(); 
					  else
					    newFragment.setPulledFragment();
					  // Saving the changes in the database  
					  alm.saveFragment(newFragment);
                      // Saving user's layout to database
                      alm.saveUserLayout();
					  // Updating the fragments map
					  fragments.put(fragmentId,newFragment); 
					  // Check if we need to create an additional folder on the fragment root
					  if ( "true".equals(fragmentFolder) ) {
					  	alm.loadFragment(fragmentId);
					  	createFolder(newFragment);
					  	alm.saveFragment();
					  }
					}     
				} else if (action.equals("save")) {
					String funcName = runtimeData.getParameter("fragment_fname");
					String fragmentName = runtimeData.getParameter("fragment_name");
					String fragmentDesc = runtimeData.getParameter("fragment_desc");
					String fragmentType = runtimeData.getParameter("fragment_type");
					boolean isPushedFragment = ("pushed".equals(fragmentType))?true:false;
					ALFragment fragment = (ALFragment) fragments.get(fragmentId);
				    if ( fragment != null ) { 
					   if ( isPushedFragment ) 
					     fragment.setPushedFragment(); 
					   else
					     fragment.setPulledFragment();
					   fragment.setFunctionalName(CommonUtils.nvl(funcName));
					   fragment.setDescription(CommonUtils.nvl(fragmentDesc));  
					   String fragmentRootId = getFragmentRootId(fragmentId);
					   ALNode fragmentRoot = fragment.getNode(fragmentRootId);
					   fragmentRoot.getNodeDescription().setName(fragmentName);
					   // Saving the changes in the database  
					   alm.saveFragment(fragment);					
					}     
				} else if (action.equals("delete")) {
					if (CommonUtils.parseInt(fragmentId) > 0) {
					  alm.deleteFragment(fragmentId);
					  // Updating the fragments map
					  fragments.remove(fragmentId);  
					  fragmentId = (fragments != null && !fragments.isEmpty())?(String) fragments.keySet().toArray()[0]:"";
					} else
					   new PortalException ( "Invalid fragment ID="+fragmentId);
				} else if (action.equals("publish")) {
					servantRender = true; 
					servantFragmentId = fragmentId;
				}  else if (action.equals("publish_finish")) {
					IGroupMember[] gms = (IGroupMember[]) getGroupServant().getResults();
					for ( int i = 0; i < gms.length; i++ )
				      System.out.println ( "group key: " + gms[i].getKey());
		            servantRender = false; 
	               }
				
				
		           if ( getGroupServant().isFinished() ) {
					IGroupMember[] gms = (IGroupMember[]) getGroupServant().getResults();
					if ( gms != null && "Done".equals(runtimeData.getParameter("grpCommand")))
					  alm.setPublishGroups(gms,servantFragmentId);
		           	servants.remove(getServantKey());
					servantRender = false;
		           } 	 
				
				   boolean noAction = action.equals("");
				   if ( servantRender && noAction ) {
				     fragmentId = servantFragmentId;
				     action = "publish";	
				   }
				   
				   // Loading the default layout if the fragment is loaded in the theme
				   if ( "default".equals(action) && alm.isFragmentLoaded() )
				      alm.loadUserLayout();
				
				xslt.setStylesheetParameter("uPcFM_selectedID",fragmentId);
			    xslt.setStylesheetParameter("uPcFM_action",action);	
		
	}
	
	
	/**
	 * Passes portal control structure to the channel.
	 * @see PortalControlStructures
	 */
	public void setPortalControlStructures(PortalControlStructures pcs) throws PortalException {
	    super.setPortalControlStructures(pcs);
	    if ( alm == null )  
		  throw new PortalException ("The layout manager must have type IAgreggatedUserLayoutManager!"); 
		refreshFragmentMap();  
	}

	private Document getFragmentList() throws PortalException {
		Document document = DocumentFactory.getNewDocument();
		super.getFragmentList(document);
		return document;
	}

	protected Collection getFragments() throws PortalException {
		return alm.getFragments();
	}

	public void renderXML(ContentHandler out) throws PortalException {
		
            if (!staticData.getAuthorizationPrincipal().canPublish()) {
                final String msg = 
                    "User is not authorized to access Fragment Manager";
                throw new AuthorizationException(msg , false, false);
            }
		
		XSLT xslt = XSLT.getTransformer(this, runtimeData.getLocales());
		
		analyzeParameters(xslt);
		
		xslt.setXML(getFragmentList());
		xslt.setXSL(sslLocation,"fragmentManager",runtimeData.getBrowserInfo());
		xslt.setTarget(new ServantSAXFilter(out));
		xslt.setStylesheetParameter("baseActionURL",runtimeData.getBaseActionURL());
		
		xslt.transform();    
	}

}
