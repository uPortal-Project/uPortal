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

package org.jasig.portal.portlets.search.people;

import org.springframework.stereotype.Controller;

/**
 * @author Eric Dalquist
 * @version $Revision: 1.1 $
 */
@Controller
public class PeopleSearchController {
    public static final String QUERY_PARAM = "name";
    
    private String baseSearchUrl = "http://www.wisc.edu/directories/json/";
    
//    @RequestMapping("/search/uwPeople")
//    public void search(HttpServletRequest request, HttpServletResponse response,
//            @RequestParam("name") String name) throws IOException {
//        
//        final Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>();
//        
//        parameters.put(QUERY_PARAM, Arrays.asList(name));
//
//        this.search(baseSearchUrl, parameters, response);
//    }
}
