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

import org.jasig.services.persondir.IPersonAttributes

/**
 * Given multiple persons with the same unique identifier (defined by
 * {@link #PERSON_DIRECTORY_UUID_ATTRIBUTE_CONFIG_NAME}), will choose the one (or ones) having a configured
 * base DN ({@link AbstractDnPersonAttributesFilter#FILTERING_BASE_DN_CONFIG_NAME}). Or, for any given
 * unique identifier, if no persons have the configured base DN, return all such persons.
 */
class PreferredBaseDnPersonAttributesFilter extends AbstractDnPersonAttributesFilter {

    public static final PERSON_DIRECTORY_UUID_ATTRIBUTE_CONFIG_NAME = 'personDirectoryUuidAttributeName';

    @Override
    protected List<IPersonAttributes> filterWithCanonicalizedDn(List<IPersonAttributes> persons, String canonicalizedDn, String configPrefix) {

        String personDirectoryUuidName = findConfigOrError(configPrefix, PERSON_DIRECTORY_UUID_ATTRIBUTE_CONFIG_NAME)
        if ( !(personDirectoryUuidName) ) {
            return emptyConfig(configPrefix,PERSON_DIRECTORY_UUID_ATTRIBUTE_CONFIG_NAME,personDirectoryUuidName,persons)
        }

        // groupBy() doco says bySchoolId will be linked hashmap, but use schoolIds to be *sure* to preserve original
        // ordering
        def uuids = []
        def byUuid = persons.groupBy {
            def personUuids = it.attributes[personDirectoryUuidName]
            def personUuid = personUuids ? personUuids.find { it } : null
            uuids << personUuid
            personUuid
        }

        if ( !(uuids) ) {
            return []
        }

        def canonicalized = []
        uuids.unique().each { uuid ->
            def withUuid = byUuid[uuid]
            if ( withUuid && withUuid.size() > 1 ) {
                def withUuidAndBaseDn = withUuid.findAll { hasCanonicalizedBaseDn(it, canonicalizedDn) }
                if ( withUuidAndBaseDn ) {
                    canonicalized.addAll(withUuidAndBaseDn)
                } else {
                    canonicalized.addAll(withUuid)
                }
            } else {
                canonicalized << withUuid[0]
            }
        }
        canonicalized
    }


}
