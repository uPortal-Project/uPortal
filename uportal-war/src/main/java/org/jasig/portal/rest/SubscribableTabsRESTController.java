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

package org.jasig.portal.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.fragment.subscribe.IUserFragmentSubscription;
import org.jasig.portal.fragment.subscribe.dao.IUserFragmentSubscriptionDao;
import org.jasig.portal.layout.dlm.ConfigurationLoader;
import org.jasig.portal.layout.dlm.Evaluator;
import org.jasig.portal.layout.dlm.FragmentDefinition;
import org.jasig.portal.layout.dlm.providers.SubscribedTabEvaluatorFactory;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * Returns JSON representing the list of subscribable fragments for the 
 * currently-authenticated user.
 * 
 * @author Mary Hunt
 * @author Jen Bourey
 * @version $Revision$ $Date$
 */
@Controller
public class SubscribableTabsRESTController {

	@Autowired
	@Qualifier("userInstanceManager")
	private IUserInstanceManager userInstanceManager;
	
	public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
	    this.userInstanceManager = userInstanceManager;
	}
	
	private IUserFragmentSubscriptionDao userFragmentSubscriptionDao;
	
    @Autowired(required = true)
	public void setUserFragmentSubscriptionDao(IUserFragmentSubscriptionDao userFragmentSubscriptionDao) {
	    this.userFragmentSubscriptionDao = userFragmentSubscriptionDao;
	}
	
    @Autowired
    @Qualifier("dlmConfigurationLoader")
    private ConfigurationLoader configurationLoader;
    
    @Autowired
    private MessageSource messageSource;
    
    @RequestMapping(value="/subscribableTabs.json", method = RequestMethod.GET)
    public ModelAndView getSubscriptionList(HttpServletRequest request)  {

	    Map<String, Object> model = new HashMap<String, Object>();
	    
	    /**
	     * Retrieve the IPerson and IAuthorizationPrincipal for the currently
	     * authenticated user
	     */
	    
        IUserInstance userInstance = userInstanceManager.getUserInstance(request);
        IPerson person = userInstance.getPerson();
        AuthorizationService authService = AuthorizationService.instance();
        IAuthorizationPrincipal principal = authService.newPrincipal(person.getUserName(), IPerson.class);
    	
        /**
         * Build a collection of owner IDs for the fragments to which the 
         * authenticated user is subscribed
         */

        // get the list of current subscriptions for this user
        List<IUserFragmentSubscription> subscriptions = userFragmentSubscriptionDao
                .getUserFragmentInfo(person);
        
        // transform it into the set of owners
    	Set<String> subscribedOwners = new HashSet<String>();
    	for (IUserFragmentSubscription subscription : subscriptions){
    	    if (subscription.isActive()) {
                subscribedOwners.add(subscription.getFragmentOwner());
    	    }
    	}
    	
    	/**
    	 * Iterate through the list of all currently defined DLM fragments and
    	 * determine if the current user has permissions to subscribe to each.
    	 * Any subscribable fragments will be transformed into a JSON-friendly
    	 * bean and added to the model.
    	 */

        final List<SubscribableFragment> jsonFragments = new ArrayList<SubscribableFragment>();

    	// get the list of fragment definitions from DLM
        final List<FragmentDefinition> fragmentDefinitions = configurationLoader.getFragments();
        
        final Locale locale = RequestContextUtils.getLocale(request);

        // iterate through the list
        for (FragmentDefinition fragmentDefinition : fragmentDefinitions) {
            
            if (isSubscribable(fragmentDefinition, principal)) {
                
                String owner = fragmentDefinition.getOwnerId();
                
                // check to see if the current user has permission to subscribe to
                // this fragment
                if (principal.hasPermission("UP_FRAGMENT", "FRAGMENT_SUBSCRIBE", owner)) {
                    
                    // create a JSON fragment bean and add it to our list
                    boolean subscribed = subscribedOwners.contains(owner);
                    final String name = getMessage("fragment." + owner + ".name", fragmentDefinition.getName(), locale);
                    final String description = getMessage("fragment." + owner + ".description", fragmentDefinition.getDescription(), locale);
                    SubscribableFragment jsonFragment = new SubscribableFragment(name, description, owner, subscribed);
                    jsonFragments.add(jsonFragment);
                }
                
            }
                        
        }

        model.put("fragments", jsonFragments);
        	
		return new ModelAndView("json", model);
		
	}
	
	protected boolean isSubscribable(FragmentDefinition definition, IAuthorizationPrincipal principal) {

	    String owner = definition.getOwnerId();
        
        for (Evaluator evaluator : definition.getEvaluators()) {
            if (evaluator.getFactoryClass().equals(SubscribedTabEvaluatorFactory.class)) {
                return principal.hasPermission("UP_FRAGMENT", "FRAGMENT_SUBSCRIBE", owner);
            }
        }
        
        return false;
	}
	
	protected String getMessage(String key, String defaultName, Locale locale) {
        return messageSource.getMessage(key, new Object[] {}, defaultName, locale);
	}

	/**
	 * Convenience class for representing fragment information in JSON
	 */
    public class SubscribableFragment {
        
        private String name = null;
        private String ownerID = null;
        private String description;
        private boolean subscribed;
        
        public SubscribableFragment(String name, String description, String ownerId, boolean subscribed) {
            this.name = name;
            this.description = description;
            this.ownerID = ownerId;
            this.subscribed = subscribed;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getOwnerID() {
            return ownerID;
        }

        public void setOwnerID(String ownerID) {
            this.ownerID = ownerID;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public boolean isSubscribed() {
            return subscribed;
        }

        public void setSubscribed(boolean subscribed) {
            this.subscribed = subscribed;
        }
        
    }

}
