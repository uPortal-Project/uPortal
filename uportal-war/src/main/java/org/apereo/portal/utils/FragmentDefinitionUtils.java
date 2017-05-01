/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apereo.portal.layout.dlm.ConfigurationLoader;
import org.apereo.portal.layout.dlm.FragmentActivator;
import org.apereo.portal.layout.dlm.FragmentDefinition;
import org.apereo.portal.layout.dlm.UserView;
import org.apereo.portal.security.IPerson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

/**
 * Default {@link IFragmentDefinitionUtils} implementation, backed by {@link ConfigurationLoader}
 * and {@link FragmentActivator} objects.
 *
 */
@Service
public class FragmentDefinitionUtils implements IFragmentDefinitionUtils {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ConfigurationLoader configurationLoader;
    private FragmentActivator fragmentActivator;

    @Autowired
    public void setConfigurationLoader(ConfigurationLoader configurationLoader) {
        this.configurationLoader = configurationLoader;
    }

    @Autowired
    public void setFragmentActivator(FragmentActivator fragmentActivator) {
        this.fragmentActivator = fragmentActivator;
    }

    @Override
    public final List<FragmentDefinition> getFragmentDefinitions() {
        return this.configurationLoader.getFragments();
    }

    @Override
    public FragmentDefinition getFragmentDefinitionByName(final String fragmentName) {
        return this.configurationLoader.getFragmentByName(fragmentName);
    }

    @Override
    public FragmentDefinition getFragmentDefinitionByOwner(final IPerson person) {
        return this.getFragmentDefinitionByOwner(person.getUserName());
    }

    @Override
    public FragmentDefinition getFragmentDefinitionByOwner(final String ownerId) {
        return this.configurationLoader.getFragmentByOwnerId(ownerId);
    }

    @Override
    public List<FragmentDefinition> getFragmentDefinitionsApplicableToPerson(final IPerson person) {
        final List<FragmentDefinition> result = new ArrayList<FragmentDefinition>();
        final List<FragmentDefinition> definitions = this.configurationLoader.getFragments();
        logger.debug("About to check applicability of {} fragments", definitions.size());

        if (definitions != null) {
            for (final FragmentDefinition fragmentDefinition : definitions) {
                logger.debug(
                        "Checking applicability of the following fragment: {}",
                        fragmentDefinition.getName());
                if (fragmentDefinition.isApplicable(person)) {
                    result.add(fragmentDefinition);
                }
            }
        }
        return result;
    }

    @Override
    public List<UserView> getFragmentDefinitionUserViews(final Locale locale) {
        return this.getFragmentDefinitionUserViews(this.getFragmentDefinitions(), locale);
    }

    @Override
    public List<UserView> getFragmentDefinitionUserViews(
            final List<FragmentDefinition> fragmentDefinitions, final Locale locale) {
        final List<UserView> result = new LinkedList<UserView>();
        if (fragmentDefinitions != null) {
            final FragmentActivator activator = this.fragmentActivator;
            for (FragmentDefinition definition : fragmentDefinitions) {
                final UserView userView = activator.getUserView(definition, locale);
                if (userView != null) {
                    result.add(userView);
                }
            }
        }
        return result;
    }

    @Override
    public List<Document> getFragmentDefinitionUserViewLayouts(
            final List<FragmentDefinition> fragmentDefinitions, final Locale locale) {
        final List<Document> result = new LinkedList<Document>();
        final List<UserView> userViews =
                this.getFragmentDefinitionUserViews(fragmentDefinitions, locale);
        for (UserView userView : userViews) {
            result.add(userView.getLayout());
        }
        return result;
    }

    @Override
    public Set<String> getFragmentNames() {
        return this.getFragmentNames(this.getFragmentDefinitions());
    }

    @Override
    public Set<String> getFragmentNames(final Collection<FragmentDefinition> fragmentDefinitions) {
        final Set<String> result = new HashSet<String>(fragmentDefinitions.size());
        for (FragmentDefinition definition : fragmentDefinitions) {
            result.add(definition.getName());
        }
        return result;
    }

    @Override
    public UserView getUserView(final FragmentDefinition fragmentDefinition, final Locale locale) {
        return this.fragmentActivator.getUserView(fragmentDefinition, locale);
    }
}
