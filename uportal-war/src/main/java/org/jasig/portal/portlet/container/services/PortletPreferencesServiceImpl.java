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

package org.jasig.portal.portlet.container.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

import javax.portlet.PortletRequest;
import javax.portlet.PreferencesValidator;
import javax.portlet.ValidatorException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.container.PortletContainerException;
import org.apache.pluto.container.PortletPreference;
import org.apache.pluto.container.PortletPreferencesService;
import org.apache.pluto.container.PortletWindow;
import org.apache.pluto.container.om.portlet.PortletDefinition;
import org.apache.pluto.container.om.portlet.Preference;
import org.apache.pluto.container.om.portlet.Preferences;
import org.jasig.portal.portlet.dao.jpa.PortletPreferenceImpl;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletPreference;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.rendering.IPortletRenderer;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.utils.threading.NoopLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hooks into uPortal portlet preferences object model
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service("portletPreferencesService")
public class PortletPreferencesServiceImpl implements PortletPreferencesService {
    protected static final String PORTLET_PREFERENCES_MAP_ATTRIBUTE = PortletPreferencesServiceImpl.class.getName() + ".PORTLET_PREFERENCES_MAP";
    
    protected final Log logger = LogFactory.getLog(getClass());
    
    private IPersonManager personManager;
    private IPortletWindowRegistry portletWindowRegistry;
    private IPortletEntityRegistry portletEntityRegistry;
    private IPortletDefinitionRegistry portletDefinitionRegistry;
    private IPortalRequestUtils portalRequestUtils;
    
    private boolean loadGuestPreferencesFromMemory = true;
    private boolean loadGuestPreferencesFromEntity = true;
    private boolean storeGuestPreferencesInMemory = true;
    private boolean storeGuestPreferencesInEntity = false;
    
    /**
     * @return the portalRequestUtils
     */
    public IPortalRequestUtils getPortalRequestUtils() {
        return portalRequestUtils;
    }
    /**
     * @param portalRequestUtils the portalRequestUtils to set
     */
    @Autowired
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }
    
    /**
     * @return the portletWindowRegistry
     */
    public IPortletWindowRegistry getPortletWindowRegistry() {
        return this.portletWindowRegistry;
    }
    /**
     * @param portletWindowRegistry the portletWindowRegistry to set
     */
    @Autowired
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }

    /**
     * @return the portletEntityRegistry
     */
    public IPortletEntityRegistry getPortletEntityRegistry() {
        return this.portletEntityRegistry;
    }
    /**
     * @param portletEntityRegistry the portletEntityRegistry to set
     */
    @Autowired
    public void setPortletEntityRegistry(IPortletEntityRegistry portletEntityRegistry) {
        this.portletEntityRegistry = portletEntityRegistry;
    }

    /**
     * @return the portletDefinitionRegistry
     */
    public IPortletDefinitionRegistry getPortletDefinitionRegistry() {
        return this.portletDefinitionRegistry;
    }
    /**
     * @param portletDefinitionRegistry the portletDefinitionRegistry to set
     */
    @Autowired
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }
    
    /**
     * @return the personManager
     */
    public IPersonManager getPersonManager() {
        return personManager;
    }
    /**
     * @param personManager the personManager to set
     */
    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }
    
	
	public boolean isLoadGuestPreferencesFromMemory() {
		return loadGuestPreferencesFromMemory;
	}
	public void setLoadGuestPreferencesFromMemory(
			boolean loadGuestPreferencesFromMemory) {
		this.loadGuestPreferencesFromMemory = loadGuestPreferencesFromMemory;
	}
	public boolean isLoadGuestPreferencesFromEntity() {
		return loadGuestPreferencesFromEntity;
	}
	public void setLoadGuestPreferencesFromEntity(
			boolean loadGuestPreferencesFromEntity) {
		this.loadGuestPreferencesFromEntity = loadGuestPreferencesFromEntity;
	}
	public boolean isStoreGuestPreferencesInMemory() {
		return storeGuestPreferencesInMemory;
	}
	public void setStoreGuestPreferencesInMemory(
			boolean storeGuestPreferencesInMemory) {
		this.storeGuestPreferencesInMemory = storeGuestPreferencesInMemory;
	}
	public boolean isStoreGuestPreferencesInEntity() {
		return storeGuestPreferencesInEntity;
	}
	public void setStoreGuestPreferencesInEntity(
			boolean storeGuestPreferencesInEntity) {
		this.storeGuestPreferencesInEntity = storeGuestPreferencesInEntity;
	}
	public boolean isStoreInEntity(PortletRequest portletRequest) { 
    	if (this.storeGuestPreferencesInEntity || !isGuestUser(portletRequest)) {
    		return true;
    	}

    	return false; 
    }
    
    public boolean isLoadFromEntity(PortletRequest portletRequest) { 
    	if (this.loadGuestPreferencesFromEntity || !isGuestUser(portletRequest)){
            return true;
        }

        return false; 
    }
    
    public boolean isStoreInMemory(PortletRequest portletRequest) { 
    	if (this.storeGuestPreferencesInMemory && isGuestUser(portletRequest)){
            return true;
        }

        return false; 
    }

    public boolean isLoadFromMemory(PortletRequest portletRequest) { 
    	if (this.loadGuestPreferencesFromMemory && isGuestUser(portletRequest)){
            return true;
        }

        return false; 
    }
    
    /*
     * (non-Javadoc)
     * @see org.apache.pluto.container.PortletPreferencesService#getDefaultPreferences(org.apache.pluto.container.PortletWindow, javax.portlet.PortletRequest)
     */
    @Override
	public Map<String, PortletPreference> getDefaultPreferences(PortletWindow plutoPortletWindow, PortletRequest portletRequest)
			throws PortletContainerException {
        
        final HttpServletRequest httpServletRequest = this.portalRequestUtils.getPortletHttpRequest(portletRequest);
        
        final IPortletWindow portletWindow = this.portletWindowRegistry.convertPortletWindow(httpServletRequest, plutoPortletWindow);
        final IPortletEntity portletEntity = portletWindow.getPortletEntity();
        final IPortletDefinition portletDefinition = portletEntity.getPortletDefinition();
        final PortletDefinition portletDescriptor = this.portletDefinitionRegistry.getParentPortletDescriptor(portletDefinition.getPortletDefinitionId());

        
        //Linked hash map used to add preferences in order loaded from the descriptor, definition and entity
        final LinkedHashMap<String, PortletPreference> preferencesMap = new LinkedHashMap<String, PortletPreference>();
        
        final boolean configMode = IPortletRenderer.CONFIG.equals(portletWindow.getPortletMode());
        
        //Add descriptor preferences
        final List<IPortletPreference> descriptorPreferencesList = this.getDescriptorPreferences(portletDescriptor);
        this.addPreferencesToMap(descriptorPreferencesList, preferencesMap, configMode);
        
        //Add definition preferences
        final List<IPortletPreference> definitionPreferencesList = portletDefinition.getPortletPreferences();
        this.addPreferencesToMap(definitionPreferencesList, preferencesMap, configMode);
        
        return preferencesMap;
	}
    /*
     * (non-Javadoc)
     * @see org.apache.pluto.container.PortletPreferencesService#getPreferencesValidator(org.apache.pluto.container.om.portlet.PortletDefinition)
     */
	@Override
	public PreferencesValidator getPreferencesValidator(
			PortletDefinition portletDefinition) throws ValidatorException {
		// TODO load validator when the portlet is registered since the classloader context will be correct
		return null;
	}
	
    /*
     * (non-Javadoc)
     * @see org.apache.pluto.container.PortletPreferencesService#getStoredPreferences(org.apache.pluto.container.PortletWindow, javax.portlet.PortletRequest)
     */
	@Override
    public Map<String,PortletPreference> getStoredPreferences(PortletWindow plutoPortletWindow, PortletRequest portletRequest) throws PortletContainerException {
        final HttpServletRequest httpServletRequest = this.portalRequestUtils.getPortletHttpRequest(portletRequest);
        
        final IPortletWindow portletWindow = this.portletWindowRegistry.convertPortletWindow(httpServletRequest, plutoPortletWindow);
        final IPortletEntity portletEntity = portletWindow.getPortletEntity();
        
        //Linked hash map used to add preferences in order loaded from the descriptor, definition and entity
        final Map<String, PortletPreference> preferencesMap = new LinkedHashMap<String, PortletPreference>();
        
        if (!IPortletRenderer.CONFIG.equals(portletWindow.getPortletMode())) {
            //If not guest or storing shared guest prefs get the prefs from the portlet entity
            if (this.isLoadFromEntity(portletRequest)) {
                //Add entity preferences
                final List<IPortletPreference> entityPreferencesList = portletEntity.getPortletPreferences();
                this.addPreferencesToMap(entityPreferencesList, preferencesMap, false);
    
                if (!this.isLoadFromMemory(portletRequest) && !this.isStoreInEntity(portletRequest) && this.isStoreInMemory(portletRequest)) {
                    store(plutoPortletWindow, portletRequest, preferencesMap);
                }
            }
            //If a guest and storing non-shared guest prefs get the prefs from the session
            if (this.isLoadFromMemory(portletRequest)) {
                //Add memory preferences
                final List<IPortletPreference> entityPreferencesList = this.getSessionPreferences(portletEntity.getPortletEntityId(), httpServletRequest);
                this.addPreferencesToMap(entityPreferencesList, preferencesMap, false);
            }
        }

        
        return preferencesMap;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.pluto.container.PortletPreferencesService#store(org.apache.pluto.container.PortletWindow, javax.portlet.PortletRequest, java.util.Map)
     */
	@Transactional
	@Override
    public void store(PortletWindow plutoPortletWindow, PortletRequest portletRequest, Map<String,PortletPreference> newPreferences) throws PortletContainerException {
        final HttpServletRequest httpServletRequest = this.portalRequestUtils.getPortletHttpRequest(portletRequest);
        
        //Determine if the user is a guest
        final boolean isGuest = isGuestUser(portletRequest);
        
        //If this is a guest and no prefs are being stored just return as the rest of the method is not needed for this case
        if (isGuest && !(this.isStoreInEntity(portletRequest) || this.isStoreInMemory(portletRequest))) {
            return;
        }

        final IPortletWindow portletWindow = this.portletWindowRegistry.convertPortletWindow(httpServletRequest, plutoPortletWindow);
        IPortletEntity portletEntity = portletWindow.getPortletEntity();
        final IPortletEntityId portletEntityId = portletEntity.getPortletEntityId();
        final IPortletDefinition portletDefinition = portletEntity.getPortletDefinition();
        final PortletDefinition portletDescriptor = this.portletDefinitionRegistry.getParentPortletDescriptor(portletDefinition.getPortletDefinitionId());

        //Is this CONFIG mode
        final boolean configMode = IPortletRenderer.CONFIG.equals(portletWindow.getPortletMode());
        
        //Get Map of descriptor and definition preferences to check new preferences against
        final Map<String, PortletPreference> basePreferences = new HashMap<String, PortletPreference>();
        
        //Add deploy preferences
        final List<IPortletPreference> descriptorPreferencesList = this.getDescriptorPreferences(portletDescriptor);
        this.addPreferencesToMap(descriptorPreferencesList, basePreferences, configMode);
  
        final Lock prefLock;
        if (configMode) {
            //In config mode we don't worry about locking
            prefLock = NoopLock.INSTANCE;
        }
        else {
            prefLock = this.portletEntityRegistry.getPortletEntityLock(httpServletRequest, portletEntityId);
        }

        //Do a tryLock firsrt so that we can warn about concurrent preference modification if it fails
        boolean locked = prefLock.tryLock();
        try {
            if (!locked) {
                logger.warn("Concurrent portlet preferences modification by: " + portletDefinition.getFName() +  " " +
                        "This has the potential for changes to preferences to be lost. " +
                        "This portlet should be modified to synchronize its preference modifications appropriately", new Throwable());
                
                prefLock.lock();
                locked = true;
                
                //Refresh the portlet entity that may have been changed by the thread we were blocked by
                if (!configMode) {
                    portletEntity = this.portletEntityRegistry.getPortletEntity(httpServletRequest, portletEntityId);
                }
            }
            
            //Add definition preferences if not config mode
            if (!configMode) {
                final List<IPortletPreference> definitionPreferencesList = portletDefinition.getPortletPreferences();
                this.addPreferencesToMap(definitionPreferencesList, basePreferences, false);
            }

            final List<IPortletPreference> preferencesList = new ArrayList<IPortletPreference>(newPreferences.size());
        
            for (final PortletPreference internalPreference : newPreferences.values()) {
                //Ignore preferences with null names
                final String name = internalPreference.getName();
                if (name == null) {
                    throw new IllegalArgumentException("PortletPreference name cannot be null");
                }
    
                //Convert to a uPortal preference class to ensure quality check and persistence works
                final IPortletPreference preference = new PortletPreferenceImpl(internalPreference);
                
                //If the preference exactly equals a descriptor or definition preference ignore it
                final PortletPreference basePreference = basePreferences.get(name);
                if (preference.equals(basePreference)) {
                    continue;
                }
                
                //New preference, add it to the list
                preferencesList.add(preference);
            }
        
            //If in config mode store the preferences on the definition
            if (configMode) {
            	portletDefinition.setPortletPreferences(preferencesList);
                this.portletDefinitionRegistry.updatePortletDefinition(portletDefinition);
            }
            //If not a guest or if guest prefs are shared store them on the entity
            else if (this.isStoreInEntity(portletRequest)) {
                //Update the portlet entity with the new preferences
                portletEntity.setPortletPreferences(preferencesList);
                this.portletEntityRegistry.storePortletEntity(httpServletRequest, portletEntity);
            }
            //Must be a guest and share must be off so store the prefs on the session
            else {
                //Store memory preferences
                this.storeSessionPreferences(portletEntityId, httpServletRequest, preferencesList);
            }
        }
        finally {
            //check if locked, needed due to slighly more complex logic around the tryLock and logging
            if (locked) {
                prefLock.unlock();
            }
        }
    }
    
    /**
     * Gets the preferences for a portlet descriptor converted to the uPortal IPortletPreference
     * interface.
     */
    protected List<IPortletPreference> getDescriptorPreferences(PortletDefinition portletDescriptor) {
        final List<IPortletPreference> preferences = new LinkedList<IPortletPreference>();
        
        final Preferences descriptorPreferences = portletDescriptor.getPortletPreferences();
        if (descriptorPreferences != null) {
            final List<? extends Preference> descriptorPreferencesList = descriptorPreferences.getPortletPreferences();
            for (final Preference descriptorPreference : descriptorPreferencesList) {
                final IPortletPreference internaldescriptorPreference = new PortletPreferenceImpl(descriptorPreference);
                preferences.add(internaldescriptorPreference);
            }
        }
        
        return preferences;
    }
    
    /**
     * Add all of the preferences in the List to the Map using the preference name as the key
     */
    protected void addPreferencesToMap(List<IPortletPreference> preferencesList, Map<String, PortletPreference> preferencesMap, boolean disableReadOnly) {
        if (preferencesList == null) {
            return;
        }

        for (final IPortletPreference preference : preferencesList) {
            final PortletPreferenceImpl clonedPreference = new PortletPreferenceImpl(preference);
            if (disableReadOnly) {
                clonedPreference.setReadOnly(false);
            }
            preferencesMap.put(preference.getName(), clonedPreference);
        }
    }
    
    
    /**
     * Determine if the user for the specified request is a guest as it pertains to shared portlet preferences.
     */
    protected boolean isGuestUser(PortletRequest portletRequest) {
        final HttpServletRequest portalRequest = this.portalRequestUtils.getPortletHttpRequest(portletRequest);
        final IPerson person = this.personManager.getPerson(portalRequest);
        return person.isGuest();
    }
    
    /**
     * Gets the session-stored list of IPortletPreferences for the specified request and IPortletEntityId.
     * 
     * @return List of IPortletPreferences for the entity and session, may be null if no preferences have been set.
     */
    @SuppressWarnings("unchecked")
	protected List<IPortletPreference> getSessionPreferences(IPortletEntityId portletEntityId, HttpServletRequest httpServletRequest) {
        final HttpSession session = httpServletRequest.getSession();
        
        final Map<IPortletEntityId, List<IPortletPreference>> portletPreferences;
        
        //Sync on the session to ensure the Map isn't in the process of being created
        synchronized (session) {
            portletPreferences = (Map<IPortletEntityId, List<IPortletPreference>>)session.getAttribute(PORTLET_PREFERENCES_MAP_ATTRIBUTE);
        }
        
        if (portletPreferences == null) {
            return null;
        }

        return portletPreferences.get(portletEntityId);
    }
    
    @SuppressWarnings("unchecked")
	protected void storeSessionPreferences(IPortletEntityId portletEntityId, HttpServletRequest httpServletRequest, List<IPortletPreference> preferences) {
        final HttpSession session = httpServletRequest.getSession();
        
        Map<IPortletEntityId, List<IPortletPreference>> portletPreferences;
        
        //Sync on the session to ensure other threads aren't creating the Map at the same time
        synchronized (session) {
            portletPreferences = (Map<IPortletEntityId, List<IPortletPreference>>)session.getAttribute(PORTLET_PREFERENCES_MAP_ATTRIBUTE);
            if (portletPreferences == null) {
                portletPreferences = new ConcurrentHashMap<IPortletEntityId, List<IPortletPreference>>();
                session.setAttribute(PORTLET_PREFERENCES_MAP_ATTRIBUTE, portletPreferences);
            }
        }
        
        portletPreferences.put(portletEntityId, preferences);
    }
	
	
}
