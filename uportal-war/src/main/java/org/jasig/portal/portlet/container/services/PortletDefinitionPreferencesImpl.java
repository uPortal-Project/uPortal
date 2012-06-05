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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.portlet.ValidatorException;

import org.apache.pluto.container.om.portlet.PortletDefinition;
import org.apache.pluto.container.om.portlet.Preference;
import org.apache.pluto.container.om.portlet.Preferences;
import org.jasig.portal.portlet.dao.jpa.PortletPreferenceImpl;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletPreference;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

/**
 * Preferences impl that manipulates the portlet definition level preference data
 * 
 * @author Eric Dalquist
 */
public class PortletDefinitionPreferencesImpl extends AbstractPortletPreferencesImpl<IPortletDefinition> {
    private final IPortletDefinitionId portletDefinitionId;
    
    private final IPortletDefinitionRegistry portletDefinitionRegistry;
    private final TransactionOperations transactionOperations;

    public PortletDefinitionPreferencesImpl(IPortletDefinitionRegistry portletDefinitionRegistry,
            TransactionOperations transactionOperations, IPortletDefinitionId portletDefinitionId, boolean render) {

        super(render);
        
        this.portletDefinitionRegistry = portletDefinitionRegistry;
        this.transactionOperations = transactionOperations;
        this.portletDefinitionId = portletDefinitionId;
    }
    

    @Override
    protected IPortletDefinition getInitializationContext() {
        return this.portletDefinitionRegistry.getPortletDefinition(portletDefinitionId);
    }

    @Override
    protected Object getLogDescription() {
        return this.getInitializationContext();
    }

    @Override
    protected void loadTargetPortletPreferences(IPortletDefinition portletDefinition, Map<String, IPortletPreference> targetPortletPreferences) {
        final List<IPortletPreference> entityPreferences = portletDefinition.getPortletPreferences();
        for (final IPortletPreference preference : entityPreferences) {
            targetPortletPreferences.put(preference.getName(), preference);
        }        
    }

    @Override
    protected void loadBasePortletPreferences(IPortletDefinition portletDefinition, Map<String, IPortletPreference> basePortletPreferences) {
        //Add descriptor prefs to base Map
        final IPortletDefinitionId portletDefinitionId = portletDefinition.getPortletDefinitionId();
        final PortletDefinition portletDescriptor = this.portletDefinitionRegistry.getParentPortletDescriptor(portletDefinitionId);
        final Preferences descriptorPreferences = portletDescriptor.getPortletPreferences();
        for (final Preference preference : descriptorPreferences.getPortletPreferences()) {
            final IPortletPreference preferenceWrapper = new PortletPreferenceImpl(preference);
            basePortletPreferences.put(preferenceWrapper.getName(), preferenceWrapper);
        }
    }
    
    @Override
    protected boolean isReadOnly(IPortletPreference portletPreference) {
        //In config mode (editing definition prefs) we ignore the read-only flag
        return false;
    }


    @Override
    protected boolean storeInternal() throws IOException, ValidatorException {
        
        return this.transactionOperations.execute(new TransactionCallback<Boolean>() {
            @Override
            public Boolean doInTransaction(TransactionStatus status) {
                //Refresh the entity to avoid optimistic locking errors
                final IPortletDefinition portletDefinition = portletDefinitionRegistry.getPortletDefinition(portletDefinitionId);
            
                final Map<String, IPortletPreference> targetPortletPreferences = getTargetPortletPreferences();
                final Collection<IPortletPreference> values = targetPortletPreferences.values();
                final boolean modified = portletDefinition.setPortletPreferences(new ArrayList<IPortletPreference>(values));
                if (!modified) {
                    //Nothing actually changed, skip the store
                    return Boolean.FALSE;
                }
                
                portletDefinitionRegistry.updatePortletDefinition(portletDefinition);
                
                return Boolean.TRUE;
            }
        });
    }
}
