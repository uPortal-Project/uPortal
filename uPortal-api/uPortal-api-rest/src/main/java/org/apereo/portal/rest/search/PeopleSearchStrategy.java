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
package org.apereo.portal.rest.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.apereo.portal.portlets.lookup.PersonLookupHelperImpl;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.services.persondir.IPersonAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Provides search results to the {@link SearchRESTController} that are people.
 *
 * @since 5.0
 */
@Component
public class PeopleSearchStrategy implements ISearchStrategy {

    private static final String RESULT_TYPE_NAME = "people";

    @Autowired private IPersonManager personManager;

    @Autowired private PersonLookupHelperImpl lookupHelper;

    @Resource(name = "directoryQueryAttributes")
    private List<String> directoryQueryAttributes;

    @Override
    public String getResultTypeName() {
        return RESULT_TYPE_NAME;
    }

    @Override
    public List<?> search(String query, HttpServletRequest request) {
        final List<Object> result = new ArrayList<>();

        final IPerson user = personManager.getPerson(request);

        final Map<String, Object> queryPplAttrMap = new HashMap<>();
        for (String attr : directoryQueryAttributes) {
            queryPplAttrMap.put(attr, query);
        }

        final List<IPersonAttributes> people = lookupHelper.searchForPeople(user, queryPplAttrMap);
        if (people != null) {
            for (IPersonAttributes p : people) {
                result.add(p.getAttributes());
            }
        }
        return result;
    }
}
