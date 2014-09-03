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
package org.jasig.portal.portlets.lookup

import org.jasig.portal.spring.context.support.QueryablePropertySourcesPlaceholderConfigurer
import org.jasig.portal.utils.ContextPropertyPlaceholderUtils
import org.jasig.services.persondir.IPersonAttributes

/**
 * Base class for {@link IPersonAttributesFilter} impls which have something to do with
 * filtering persons by distinguished name (DN). That attribute must be present in the
 * logical person attribute defined by {@link #DN_ATTRIBUTE_NAME}. The DN relevant to
 * filtering must be defined by a property having the {@link #FILTERING_BASE_DN_CONFIG_NAME}
 * suffix.
 */
abstract class AbstractDnPersonAttributesFilter implements IPersonAttributesFilter {

    public static final FILTERING_BASE_DN_CONFIG_NAME = 'personFilteringBaseDn';
    public static final DN_ATTRIBUTE_NAME = 'distinguishedName';

    @Override
    List<IPersonAttributes> filter(List<IPersonAttributes> persons, String configPrefix) {
        if ( !(persons) ) {
            return persons
        }

        String dn = findConfigOrError(configPrefix, FILTERING_BASE_DN_CONFIG_NAME)

        if ( !(dn) ) {
            emptyConfig(configPrefix, FILTERING_BASE_DN_CONFIG_NAME, dn, persons)
        } else {
            filterWithCanonicalizedDn(persons, canonicalizeDn(dn), configPrefix)
        }
    }

    protected List<IPersonAttributes> emptyConfig(String configPrefix, String configName, String resolvedConfig, List<IPersonAttributes> persons) {
        throw new IllegalStateException("Filtering config [${fullConfigName(configPrefix,configName)}] is set to an empty value.");
    }

    protected abstract List<IPersonAttributes> filterWithCanonicalizedDn(List<IPersonAttributes> persons, String canonicalizedDn, String configPrefix);

    protected String findConfigOrError(String configPrefix, String configName) {
        def configExpression = fullConfigExpression(configPrefix, configName);
        try {
            return ContextPropertyPlaceholderUtils.resolve(configExpression,
                    QueryablePropertySourcesPlaceholderConfigurer.UnresolvablePlaceholderStrategy.ERROR);
        } catch ( Exception e ) {
            throw new IllegalStateException("Unable to resolve expression [${configExpression}] from config.", e);
        }
    }

    protected String fullConfigName(String configPrefix, String configName) {
        configPrefix ? "${configPrefix}.${configName}" : configName
    }

    protected String fullConfigExpression(String configPrefix, String configName) {
        '${' + fullConfigName(configPrefix, configName) + '}'
    }

    protected String canonicalizeDn(String dn) {
        dn ? dn.replaceAll(/\s/,"").toLowerCase() : dn
    }

    protected String canonicalizedDnOf(IPersonAttributes person) {
        def personDns = person.attributes[DN_ATTRIBUTE_NAME]
        def personDn = personDns ? personDns.find { it } : null
        personDn ? canonicalizeDn(personDn) : null
    }

    protected boolean hasCanonicalizedBaseDn(IPersonAttributes person, String canonicalizedBaseDn) {
        def personDn = canonicalizedDnOf(person)
        personDn ? personDn.endsWith(canonicalizedBaseDn) : false
    }

}
