/**
 * Copyright © 2003 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.layout.channels;

import org.jasig.portal.ChannelRegistryManager;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IPrivileged;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalException;
import org.jasig.portal.channels.BaseChannel;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.utils.CommonUtils;
import org.jasig.portal.utils.XSLT;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
//import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;
import java.util.Vector;

  /**
   * A channel for adding new content to a layout.
   * @author Michael Ivanov, mvi@immagic.com
   * @version $Revision$
   */
  public class CContentSubscriber extends BaseChannel implements IPrivileged {

    private static final String sslLocation = "/org/jasig/portal/channels/CContentSubscriber/CContentSubscriber.ssl";
    private PortalControlStructures controlStructures;
    private IUserLayoutManager ulm;
    private static Document channelRegistry;
    private Vector expandedCategories, condensedCategories;
    private Vector expandedChannels, condensedChannels;
    private Vector expandedFragments, condensedFragments;
    private Vector[] expandedItems;
	private Vector[] condensedItems;

    public CContentSubscriber() {
       super();
	   expandedCategories = new Vector();
	   expandedChannels = new Vector();
	   expandedFragments = new Vector();
	   condensedCategories = new Vector();
	   condensedChannels = new Vector();
	   condensedFragments = new Vector();
	   expandedItems = new Vector[] { expandedFragments, expandedChannels, expandedCategories }; 
	   condensedItems = new Vector[] { condensedFragments, condensedChannels, condensedCategories }; 
    }

	private void analyzeParameters() throws PortalException {
		
		    //Document channelRegistry = (Document) CContentSubscriber.channelRegistry.cloneNode(true);
		
			String fragmentId = CommonUtils.nvl(runtimeData.getParameter("uPcCS_fragmentID"));
		    String channelId = CommonUtils.nvl(runtimeData.getParameter("uPcCS_channelID"));
		    String categoryId = CommonUtils.nvl(runtimeData.getParameter("uPcCS_categoryID"));
			String action = CommonUtils.nvl(runtimeData.getParameter("uPcCS_action"));
			boolean allFragments = false, 
			        allChannels = false, 
			        allCategories = false;
		           
			 if (action.equals("expand")) {
			 	
				if ( CommonUtils.parseInt(fragmentId) > 0 ) {
				  expandedFragments.add(fragmentId);
				  condensedFragments.remove(fragmentId);
				} else if ( fragmentId.equals("all") ) {
				   allFragments = true;
				   condensedFragments.removeAllElements(); 		    
				}  
				
				if ( CommonUtils.parseInt(channelId) > 0 ) {
				  expandedChannels.add(channelId);
				  condensedChannels.remove(channelId);
				} else if ( channelId.equals("all") ) {
				   allChannels = true;
				   condensedChannels.removeAllElements();
				}  
				   
				if ( CommonUtils.parseInt(categoryId) > 0 ) {
				  expandedCategories.add(categoryId);
				  condensedCategories.remove(categoryId);
				} else if ( categoryId.equals("all") ) {
				   allCategories = true; 	
				   condensedCategories.remove(categoryId);
				}
				  	  		 	
			 } else if ( action.equals("condense") ) {
				
				if ( CommonUtils.parseInt(fragmentId) > 0 ) {
				  condensedFragments.add(fragmentId); 
				  expandedFragments.remove(fragmentId);
				} else if ( fragmentId.equals("all") ) {
				   allFragments = true;	
				   expandedFragments.removeAllElements();
				}    		    
				
				if ( CommonUtils.parseInt(channelId) > 0 ) {
				  condensedChannels.add(channelId);
				  expandedChannels.remove(channelId);
				} else if ( channelId.equals("all") ) {
				   allChannels = true;	
				   expandedChannels.removeAllElements();
				} 
				   
				if ( CommonUtils.parseInt(categoryId) > 0 ) {
				  condensedCategories.add(categoryId);	
				  expandedCategories.remove(categoryId);
				} else if ( categoryId.equals("all") ) {
				   allCategories = true;	
				   expandedCategories.removeAllElements();
				  }  		 
			 }	
			 
			
			 
		     Vector tagNames = new Vector();
			 
			 if ( allFragments )
			 	tagNames.add("fragment");
			 if ( allChannels )
			    tagNames.add("channel");
			 if ( allCategories )
			    tagNames.add("category");
			    
			 for ( int i = 0; i < expandedItems.length; i++ ) {	 
			   Vector list = expandedItems[i];
			   for ( int j = 0; j < list.size(); j++ )
			    channelRegistry.getElementById((String)list.get(j)).setAttribute("view","expand");
			 }  
			  
			 for ( int i = 0; i < condensedItems.length; i++ ) {	 
			   Vector list = condensedItems[i];
			   for ( int j = 0; j < list.size(); j++ )
				channelRegistry.getElementById((String)list.get(j)).setAttribute("view","condense");
			 }    
			 
		     for ( int i = 0; i < tagNames.size(); i++ ) {
			  NodeList nodeList = channelRegistry.getElementsByTagName((String)tagNames.get(i));
			  for ( int k = 0; k < nodeList.getLength(); k++ ) {
				Element node = (Element) nodeList.item(k);
				node.setAttribute("view",action);
			  } 
		     } 
		     
		System.out.println ( "registry:\n" + org.jasig.portal.utils.XML.serializeNode(channelRegistry));    
			 
	}		 	


    /**
     * Passes portal control structure to the channel.
     * @see PortalControlStructures
     */
    public void setPortalControlStructures(PortalControlStructures pcs) throws PortalException {
        controlStructures = pcs;
        ulm = controlStructures.getUserPreferencesManager().getUserLayoutManager();
    }

    public void setStaticData (ChannelStaticData sd) throws PortalException {
       staticData = sd;
       if ( channelRegistry == null )
        channelRegistry = ChannelRegistryManager.getChannelRegistry(staticData.getPerson());
    }

    public void renderXML (ContentHandler out) throws PortalException {
    	
	  analyzeParameters();	

      String catId = CommonUtils.nvl(runtimeData.getParameter("catID"));

      XSLT xslt = XSLT.getTransformer(this, runtimeData.getLocales());
      xslt.setXML(channelRegistry);
      xslt.setXSL(sslLocation, "contentSubscriber", runtimeData.getBrowserInfo());
      xslt.setTarget(out);
      if ( catId.length() > 0 )
       xslt.setStylesheetParameter("catID", catId );
      xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
      xslt.transform();
    }

  }