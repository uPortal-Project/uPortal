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
package org.jasig.portal.layout;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.security.IPerson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Revision$
 */
public class ChainingProfileMapperImpl implements IProfileMapper {

    protected Logger logger = LoggerFactory.getLogger(getClass());
    
    private String defaultProfileName = "default";
    
    public void setDefaultProfileName(String defaultProfileName) {
        this.defaultProfileName = defaultProfileName;
    }
    
    private List<IProfileMapper> subMappers = Collections.<IProfileMapper>emptyList();
    
    public void setSubMappers(List<IProfileMapper> subMappers) {
        this.subMappers = subMappers;
    }

    
    @Override
    public String getProfileFname(IPerson person, HttpServletRequest request) {
        for (IProfileMapper mapper : subMappers) {
            final String fname = mapper.getProfileFname(person, request);
            if (fname != null) {
                return fname;
            }
        }
        return defaultProfileName;
    }

}
