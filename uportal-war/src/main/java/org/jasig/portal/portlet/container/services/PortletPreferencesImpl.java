package org.jasig.portal.portlet.container.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;
import javax.portlet.ValidatorException;
import javax.servlet.http.HttpServletRequest;

import org.apache.pluto.container.PortletRequestContext;
import org.apache.pluto.container.om.portlet.PortletDefinition;
import org.apache.pluto.container.om.portlet.Preference;
import org.apache.pluto.container.om.portlet.Preferences;
import org.jasig.portal.portlet.dao.jpa.PortletPreferenceImpl;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletPreference;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.util.Assert;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

/**
 * Create impls
 *  abstract base class
 *  entity prefs (normal) read & read/write 
 *  def prefs (config mode) read & read/write
 *  guest (memory/nostore) read & read/write
 * 
 * entity
 *  Need references to descriptor, definition & entity
 * def
 *   Need references to descriptor & definition & entity
 * 
 * @author Eric Dalquist
 */
public class PortletPreferencesImpl implements PortletPreferences {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final PortletRequestContext portletRequestContext;
    private final IPortletEntityRegistry portletEntityRegistry;
    private final IPortletDefinitionRegistry portletDefinitionRegistry;
    private final TransactionOperations transactionOperations;
    private final IPortletEntity portletEntity;
    private final boolean render;
    
    private Map<String, IPortletPreference> targetPortletPreferences;
    private Map<String, IPortletPreference> basePortletPreferences;
    private Map<String, IPortletPreference> compositePreferences;
    private Map<String, String[]> compositePreferencesView;
    private boolean modified = false;

    public PortletPreferencesImpl(PortletRequestContext portletRequestContext,
            IPortletEntityRegistry portletEntityRegistry, IPortletDefinitionRegistry portletDefinitionRegistry,
            TransactionOperations transactionOperations, IPortletEntity portletEntity, boolean render) {
        
        this.portletRequestContext = portletRequestContext;
        this.portletEntityRegistry = portletEntityRegistry;
        this.portletDefinitionRegistry = portletDefinitionRegistry;
        this.transactionOperations = transactionOperations;
        this.portletEntity = portletEntity;
        this.render = render;
    }

    protected void initPrefs() {
        if (this.compositePreferencesView != null) {
            return;
        }
        
        this.targetPortletPreferences = new LinkedHashMap<String, IPortletPreference>();
        final List<IPortletPreference> entityPreferences = this.portletEntity.getPortletPreferences();
        for (final IPortletPreference preference : entityPreferences) {
            this.targetPortletPreferences.put(preference.getName(), preference);
        }
        
        this.basePortletPreferences = new LinkedHashMap<String, IPortletPreference>();

        final IPortletDefinition portletDefinition = this.portletEntity.getPortletDefinition();
        
        //Add descriptor prefs to base Map
        final IPortletDefinitionId portletDefinitionId = portletDefinition.getPortletDefinitionId();
        final PortletDefinition portletDescriptor = this.portletDefinitionRegistry.getParentPortletDescriptor(portletDefinitionId);
        final Preferences descriptorPreferences = portletDescriptor.getPortletPreferences();
        for (final Preference preference : descriptorPreferences.getPortletPreferences()) {
            final IPortletPreference preferenceWrapper = new PortletPreferenceImpl(preference);
            this.basePortletPreferences.put(preferenceWrapper.getName(), preferenceWrapper);
        }

        //Add definition prefs to base Map
        final List<IPortletPreference> definitionPreferences = portletDefinition.getPortletPreferences();
        for (final IPortletPreference preference : definitionPreferences) {
            this.basePortletPreferences.put(preference.getName(), preference);
        }
        
        this.compositePreferences = new LinkedHashMap<String, IPortletPreference>(this.basePortletPreferences);
        this.compositePreferences.putAll(this.targetPortletPreferences);
        
        this.compositePreferencesView = Maps.transformValues(this.compositePreferences, new Function<IPortletPreference, String[]>() {
            @Override
            public String[] apply(IPortletPreference input) {
                return input.getValues();
            }
        });
    }
    
    protected Map<String, IPortletPreference> getTargetPortletPreferences() {
        this.initPrefs();
        return this.targetPortletPreferences;
    }
    
    protected Map<String, IPortletPreference> getBasePortletPreferences() {
        this.initPrefs();
        return this.basePortletPreferences;
    }
    
    protected Map<String, IPortletPreference> getCompositePortletPreferences() {
        this.initPrefs();
        return this.compositePreferences;
    }
    
    protected Map<String, String[]> getCompositePortletPreferencesView() {
        this.initPrefs();
        return this.compositePreferencesView;
    }
    
    protected IPortletPreference getPortletPreference(String key) {
        Assert.notNull(key, "Preference Key cannot be null");
        
        final Map<String, IPortletPreference> targetPortletPreferences = this.getTargetPortletPreferences();
        final IPortletPreference portletPreference = targetPortletPreferences.get(key);
        if (portletPreference != null) {
            return portletPreference;
        }
        
        final Map<String, IPortletPreference> basePortletPreferences = this.getBasePortletPreferences();
        return basePortletPreferences.get(key);
    }

    @Override
    public boolean isReadOnly(String key) {
        final IPortletPreference portletPreference = this.getPortletPreference(key);
        return portletPreference == null || portletPreference.isReadOnly();
    }

    @Override
    public String getValue(String key, String def) {
        final IPortletPreference portletPreference = this.getPortletPreference(key);
        
        if (portletPreference != null) {
            final String[] values = portletPreference.getValues();
            if (values == null || values.length == 0) {
                return null;
            }
            
            return values[0];
        }
        
        return def;
    }

    @Override
    public String[] getValues(String key, String[] def) {
        final IPortletPreference portletPreference = this.getPortletPreference(key);
        
        if (portletPreference != null) {
            return portletPreference.getValues();
        }
        
        return def;
    }

    @Override
    public void setValue(String key, String value) throws ReadOnlyException {
        this.setValues(key, new String[] { value });
    }

    @Override
    public void setValues(String key, String[] values) throws ReadOnlyException {
        Assert.notNull(key, "Preference Key cannot be null");
        
        final Map<String, IPortletPreference> targetPortletPreferences = this.getTargetPortletPreferences();
        
        //Check if there is a base preference for the key
        final Map<String, IPortletPreference> basePortletPreferences = this.getBasePortletPreferences();
        final IPortletPreference basePreference = basePortletPreferences.get(key);
        if (basePreference != null) {
            if (basePreference.isReadOnly()) { //TODO handle "no-read-only'
                throw new ReadOnlyException("Preference '" + key + "' is read only");
            }

            //if the set value matches base value, delete any target pref
            if (Arrays.equals(values, basePreference.getValues())) {
                final IPortletPreference oldPreference = targetPortletPreferences.remove(key);
                this.modified = modified || oldPreference != null;
                return;
            }
        }
        
        IPortletPreference portletPreference = targetPortletPreferences.get(key);
        //No target preference exists yet, create it and then update the composite map
        if (portletPreference == null) {
            portletPreference = new PortletPreferenceImpl(key, false, values.clone());
            targetPortletPreferences.put(key, portletPreference);
            
            final Map<String, IPortletPreference> compositePortletPreferences = this.getCompositePortletPreferences();
            compositePortletPreferences.put(key, portletPreference);
            
            this.modified = true;
        }
        //Update the existing preference if the values array is different
        else if (!Arrays.equals(values, portletPreference.getValues())) {
            portletPreference.setValues(values);
            
            this.modified = true;
        }
    }

    @Override
    public Enumeration<String> getNames() {
        final Map<String, String[]> compositePortletPreferencesView = this.getCompositePortletPreferencesView();
        return Collections.enumeration(compositePortletPreferencesView.keySet());
    }

    @Override
    public Map<String, String[]> getMap() {
        return this.getCompositePortletPreferencesView();
    }

    @Override
    public void reset(String key) throws ReadOnlyException {
        final Map<String, IPortletPreference> targetPortletPreferences = this.getTargetPortletPreferences();
        final IPortletPreference removed = targetPortletPreferences.remove(key);

        //There was a target preference with that key, update the composite preferences map
        if (removed != null) {
            final Map<String, IPortletPreference> basePortletPreferences = this.getBasePortletPreferences();
            final IPortletPreference basePreference = basePortletPreferences.get(key);
            final Map<String, IPortletPreference> compositePortletPreferences = this.getCompositePortletPreferences();
            if (basePreference != null) {
                compositePortletPreferences.put(key, basePreference);
            }
            else {
                compositePortletPreferences.remove(key);
            }
            
            this.modified = true;
        }
    }

    @Override
    public void store() throws IOException, ValidatorException {
        if (this.render) {
            throw new IllegalStateException("store is not allowed during RENDER phase.");
        }
        
        if (!modified) {
            return;
        }
        
        final HttpServletRequest containerRequest = portletRequestContext.getContainerRequest();
        
        final IPortletEntityId portletEntityId = portletEntity.getPortletEntityId();
        final Lock portletEntityLock = this.portletEntityRegistry.getPortletEntityLock(containerRequest, portletEntityId);
        
        //Do a tryLock firsrt so that we can warn about concurrent preference modification if it fails
        boolean locked = portletEntityLock.tryLock();
        try {
            if (!locked) {
                logger.warn("Concurrent portlet preferences modification by: " + this.portletEntity +  " " +
                        "This has the potential for changes to preferences to be lost. " +
                        "This portlet should be modified to synchronize its preference modifications appropriately", new Throwable());
                
                portletEntityLock.lock();
                locked = true;
            }
            
            this.transactionOperations.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    //Refresh the entity to avoid optimistic locking errors
                    final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(containerRequest, portletEntityId);
                
                    final Collection<IPortletPreference> values = targetPortletPreferences.values();
                    portletEntity.setPortletPreferences(new ArrayList<IPortletPreference>(values));
                    portletEntityRegistry.storePortletEntity(containerRequest, portletEntity);
                }
            });
        }
        finally {
            //check if locked, needed due to slightly more complex logic around the tryLock and logging
            if (locked) {
                portletEntityLock.unlock();
            }
        }

        this.modified = false;
    }

    
}
