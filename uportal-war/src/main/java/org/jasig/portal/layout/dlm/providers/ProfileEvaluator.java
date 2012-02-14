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

package org.jasig.portal.layout.dlm.providers;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.servlet.http.HttpServletRequest;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.jasig.portal.layout.IProfileMapper;
import org.jasig.portal.layout.dlm.Evaluator;
import org.jasig.portal.layout.dlm.EvaluatorFactory;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.spring.locator.ApplicationContextLocator;
import org.jasig.portal.url.IPortalRequestUtils;
import org.springframework.context.ApplicationContext;

@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ProfileEvaluator extends Evaluator {

    @Column(name = "PROFILE_FNAME")
    protected String profileFname;

    public ProfileEvaluator() { }
    
    public ProfileEvaluator(String profileFname) {
        this.profileFname = profileFname;
    }
    
    @Override
    public boolean isApplicable(IPerson person) {
        
        final ApplicationContext applicationContext = ApplicationContextLocator.getApplicationContext();
        final IPortalRequestUtils portalRequestUtils = applicationContext.getBean(IPortalRequestUtils.class);
        final IProfileMapper profileMapper = applicationContext.getBean(IProfileMapper.class);
        
        final HttpServletRequest request = portalRequestUtils.getCurrentPortalRequest();
        final String currentFname = profileMapper.getProfileFname(person, request);
        
        return profileFname.equals(currentFname);
    }

    @Override
    public void toElement(Element parent) {
        // Assertions.
        if (parent == null) {
            String msg = "Argument 'parent' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        
        Element rslt = DocumentHelper.createElement("profile");
        rslt.addAttribute("fname", this.profileFname);
        parent.add(rslt);
    }

    @Override
    public Class<? extends EvaluatorFactory> getFactoryClass() {
        return ProfileEvaluatorFactory.class;
    }

    @Override
    public String getSummary() {
        return "(PROFILE IS '" + this.profileFname + "')";
    }

}
