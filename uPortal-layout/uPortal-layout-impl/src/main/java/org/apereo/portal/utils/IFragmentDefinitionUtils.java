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

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apereo.portal.layout.dlm.FragmentDefinition;
import org.apereo.portal.layout.dlm.UserView;
import org.apereo.portal.security.IPerson;
import org.w3c.dom.Document;

/**
 * Interface for classes providing utility methods for dealing with {@link FragmentDefinition}s.
 *
 */
public interface IFragmentDefinitionUtils {

    List<FragmentDefinition> getFragmentDefinitions();

    FragmentDefinition getFragmentDefinitionByName(final String fragmentName);

    FragmentDefinition getFragmentDefinitionByOwner(final IPerson person);

    FragmentDefinition getFragmentDefinitionByOwner(final String ownerId);

    List<FragmentDefinition> getFragmentDefinitionsApplicableToPerson(final IPerson person);

    List<UserView> getFragmentDefinitionUserViews(final Locale locale);

    List<UserView> getFragmentDefinitionUserViews(
            final List<FragmentDefinition> fragmentDefinitions, final Locale locale);

    List<Document> getFragmentDefinitionUserViewLayouts(
            final List<FragmentDefinition> fragmentDefinitions, final Locale locale);

    Set<String> getFragmentNames();

    Set<String> getFragmentNames(final Collection<FragmentDefinition> fragmentDefinitions);

    UserView getUserView(final FragmentDefinition fragmentDefinition, final Locale locale);
}
