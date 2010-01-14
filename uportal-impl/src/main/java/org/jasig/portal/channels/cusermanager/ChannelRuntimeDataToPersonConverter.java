/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.channels.cusermanager;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.provider.PersonImpl;

/**
 * This is a utility class for extracting parameters from ChannelRuntimeData and populating them into an IPerson.
 * @author apetro
 *
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public class ChannelRuntimeDataToPersonConverter {

	  public IPerson channelRuntimeDataToPerson( ChannelRuntimeData channelRuntimeData ) {

	      IPerson newborn = new PersonImpl();
	      
	      for (String channelRuntimeDataParameterName : channelRuntimeData.getParameters().keySet()) {
	    	  if (! channelRuntimeDataParameterName.equals(Constants.FORMACTION)) {
	    		  String paramValue = channelRuntimeData.getParameter(channelRuntimeDataParameterName);
	    		  // this null handling is required becausee PersonImpl is a HashTable 
	    		  // which does not allow null user attribute values
	    		  if (paramValue == null) {
	    			  paramValue = "";
	    		  }
	    		  newborn.setAttribute(channelRuntimeDataParameterName, paramValue);
	    	  }
	      }
	     
	      return newborn;
	  }// crd2persion
	
}
