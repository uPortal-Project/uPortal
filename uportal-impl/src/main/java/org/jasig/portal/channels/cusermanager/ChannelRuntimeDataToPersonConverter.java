/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
