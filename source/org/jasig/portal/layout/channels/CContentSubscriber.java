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
import org.jasig.portal.PortalException;
import org.jasig.portal.utils.CommonUtils;
import org.jasig.portal.utils.XSLT;
import org.jasig.portal.utils.DocumentFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
//import org.w3c.dom.Node;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;
import java.util.Vector;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Collection;

  /**
   * A channel for adding new content to a layout.
   * @author Michael Ivanov, mvi@immagic.com
   * @version $Revision$
   */
public class CContentSubscriber extends FragmentManager {

    private static final String sslLocation = "/org/jasig/portal/channels/CContentSubscriber/CContentSubscriber.ssl";
    private static Document channelRegistry;
	private Document registry;
    private Map expandedCategories, condensedCategories;
    private Map expandedChannels, condensedChannels;
    private Map expandedFragments, condensedFragments;
    private Map[] expandedItems;
	private Map[] condensedItems;
	private boolean initRegistry = true;
	

    public CContentSubscriber() {
       super();
	   expandedCategories = new HashMap();
	   expandedChannels = new HashMap();
	   expandedFragments = new HashMap();
	   condensedCategories = new HashMap();
	   condensedChannels = new HashMap();
	   condensedFragments = new HashMap();
	   expandedItems = new Map[] { expandedFragments, expandedChannels, expandedCategories }; 
	   condensedItems = new Map[] { condensedFragments, condensedChannels, condensedCategories }; 
    }

	protected void analyzeParameters( XSLT xslt ) throws PortalException {
		
	  try {
		    //Document channelRegistry = (Document) CContentSubscriber.channelRegistry.cloneNode(true);
		
			String fragmentId = CommonUtils.nvl(runtimeData.getParameter("uPcCS_fragmentID"));
		    String channelId = CommonUtils.nvl(runtimeData.getParameter("uPcCS_channelID"));
		    String categoryId = CommonUtils.nvl(runtimeData.getParameter("uPcCS_categoryID"));
			String action = CommonUtils.nvl(runtimeData.getParameter("uPcCS_action"));
		    String channelState = CommonUtils.nvl(runtimeData.getParameter("channelState"));
			boolean allFragments = false, 
			        allChannels = false, 
			        allCategories = false;          
		           
			 if (action.equals("expand")) {
			 	
				if ( fragmentId.equals("all") ) {
				   allFragments = true;
				   condensedFragments.clear(); 		    
				} else if ( fragmentId.trim().length() > 0 ) {
				   expandedFragments.put(categoryId+fragmentId,fragmentId);
				   condensedFragments.remove(categoryId+fragmentId); 
				}  
				
				
				if ( channelId.equals("all") ) {
				   allChannels = true;
				   condensedChannels.clear();
				} else if ( channelId.trim().length() > 0 ) {
				   expandedChannels.put(categoryId+channelId,channelId);
				   condensedChannels.remove(categoryId+channelId);
				}  
				   
				
				if ( categoryId.equals("all") ) {
				   allCategories = true; 	
				   condensedCategories.remove(categoryId);
				} else if ( categoryId.trim().length() > 0 && channelId.trim().length() == 0 && fragmentId.trim().length() == 0 ) {
		           expandedCategories.put(categoryId,categoryId);
		           condensedCategories.remove(categoryId);
				}   
				  	  		 	
			 } else if ( action.equals("condense") ) {
				
				
				if ( fragmentId.equals("all") ) {
				   allFragments = true;	
				   expandedFragments.clear();
				} else if ( fragmentId.trim().length() > 0 ) {
				   condensedFragments.put(categoryId+fragmentId,fragmentId); 
				   expandedFragments.remove(categoryId+fragmentId);  
				}    		    
				
				
				if ( channelId.equals("all") ) {
				   allChannels = true;	
				   expandedChannels.clear();
				} else if ( channelId.trim().length() > 0 ) {
		           condensedChannels.put(categoryId+channelId,channelId);
		           expandedChannels.remove(categoryId+channelId);
			    }   
				   
				
				if ( categoryId.equals("all") ) {
				   allCategories = true;	
				   expandedCategories.clear();
				} else if ( categoryId.trim().length() > 0 && channelId.trim().length() == 0 && fragmentId.trim().length() == 0 ) {
		           condensedCategories.put(categoryId,categoryId);	
		           expandedCategories.remove(categoryId); 
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
			   Set list = expandedItems[i].keySet();
			   for ( Iterator iter = list.iterator(); iter.hasNext(); ) {
			    //registry.getElementById((String)list.get(j)).setAttribute("view","expanded");
				Element elem = (Element) XPathAPI.selectSingleNode(registry,"//*[@ID='"+(String)expandedItems[i].get(iter.next())+"']");
				elem.setAttribute("view","expanded");
			   } 
			 }  
			  
			 for ( int i = 0; i < condensedItems.length; i++ ) {	 
			   Set list = condensedItems[i].keySet();
			   for ( Iterator iter = list.iterator(); iter.hasNext(); ) {
				//registry.getElementById((String)list.get(j)).setAttribute("view","condensed");
				Element elem = (Element) XPathAPI.selectSingleNode(registry,"//*[@ID='"+(String)condensedItems[i].get(iter.next())+"']");
				elem.setAttribute("view","condensed");
			   }	
			 }    
			 
		     for ( int i = 0; i < tagNames.size(); i++ ) {
			  NodeList nodeList = registry.getElementsByTagName((String)tagNames.get(i));
			  for ( int k = 0; k < nodeList.getLength(); k++ ) {
				Element node = (Element) nodeList.item(k);
				node.setAttribute("view",(action.equals("expand"))?"expanded":"condensed");
			  } 
		     } 
		     
		    
		    
		     passAllParameters(xslt);
		      
		     /*xslt.setStylesheetParameter("channelState", channelState );
		     xslt.setStylesheetParameter("uPcCS_action", action );
		     xslt.setStylesheetParameter("channelState", channelState );
		     xslt.setStylesheetParameter("uPcCS_fragmentID", fragmentId );
		     xslt.setStylesheetParameter("uPcCS_channelID", channelId );
		     xslt.setStylesheetParameter("uPcCS_categoryID", categoryId );*/
		     
	  } catch ( Exception e ) {
	  	  e.printStackTrace();
	  	  throw new PortalException(e.getMessage());	     
	  }
			 
	}		 	

    private void passAllParameters ( XSLT xslt ) {
       for ( Enumeration params = runtimeData.getParameterNames(); params.hasMoreElements(); ) {
         String paramName = (String) params.nextElement();
         xslt.setStylesheetParameter(paramName,runtimeData.getParameter(paramName));
       }  	 
    }

	protected Collection getFragments() throws PortalException {
		 return alm.getSubscribableFragments();
	}

    public void initRegistry() throws PortalException {
      if ( initRegistry ) {
      	registry = DocumentFactory.getNewDocument();
      	registry.appendChild(registry.importNode(channelRegistry.getDocumentElement(),true));	
        getFragmentList(registry,registry.getDocumentElement());
        initRegistry = false;
      }  	        	
    }

    public void setStaticData (ChannelStaticData sd) throws PortalException {
       super.setStaticData(sd);
       if ( channelRegistry == null )
        channelRegistry = ChannelRegistryManager.getChannelRegistry(staticData.getPerson());
    }

    public void renderXML (ContentHandler out) throws PortalException {
    	
      initRegistry();

      XSLT xslt = XSLT.getTransformer(this, runtimeData.getLocales());
      analyzeParameters(xslt);
	  //System.out.println ( "registry:\n" + org.jasig.portal.utils.XML.serializeNode(registry));    
      xslt.setXML(registry);
      xslt.setXSL(sslLocation, "contentSubscriber", runtimeData.getBrowserInfo());
      xslt.setTarget(out);
      xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
      xslt.transform();
      
    }

  }