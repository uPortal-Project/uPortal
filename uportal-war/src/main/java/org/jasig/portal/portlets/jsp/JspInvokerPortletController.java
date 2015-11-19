/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.portlets.jsp;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.lang.StringUtils;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.url.IPortalUrlProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ExtendedPropertySourcesPlaceholderConfigurer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

/**
 * Provides a very simple, Spring Portlet MVC portlet implementation that 
 * renders a JSP at a location specified in the portlet definition (publish 
 * time).  This feature is very similar to the SimpleJspPortlet in the 
 * jasig-widget-portlets project, except portlets based on tech (1) are 
 * framework portlets, and (2) may access the native {@link IPortalUrlProvider}
 * API.
 */
@Controller
@RequestMapping("VIEW")
public final class JspInvokerPortletController implements ApplicationContextAware {

    private static final String CONTROLLER_PREFERENCE_PREFIX = JspInvokerPortletController.class.getSimpleName() + ".";
    private static final String VIEW_LOCATION_PREFERENCE = CONTROLLER_PREFERENCE_PREFIX + "viewLocation";
    private static final String BEANS_PREFERENCE = CONTROLLER_PREFERENCE_PREFIX + "beans";
    public static final String PREF_SECURITY_ROLE_NAMES = CONTROLLER_PREFERENCE_PREFIX + "securityRolesToTest";

    private ApplicationContext applicationContext;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired()
    private ExtendedPropertySourcesPlaceholderConfigurer properties;

    @Autowired()
    private IPortalUrlProvider portalUrlProvider;

    @Autowired()
    private IPortalRequestUtils portalRequestUtils;

    @Autowired
    private IPersonManager personManager;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setProperties(ExtendedPropertySourcesPlaceholderConfigurer props) {
        this.properties = props;
    }

    @RenderMapping
    protected ModelAndView render(RenderRequest req, RenderResponse res) {

        final Map<String,Object> model = new HashMap<String, Object>();

        @SuppressWarnings("unchecked")
        final Map<String, String> userInfo = (Map<String, String>) req.getAttribute(PortletRequest.USER_INFO);
        model.put("userInfo", userInfo);
        logger.debug("Invoking with userInfo={}", userInfo);

        // Can access property values in JSP using ${properties.getProperty('propertyName')}
        model.put("properties", properties.getPropertyResolver());

        // Determine if guest user.
        IPerson person = personManager.getPerson(portalRequestUtils.getPortletHttpRequest(req));
        model.put("authenticated", !person.isGuest());

        model.putAll(getBeans(req));

        model.putAll(getPreferences(req));

        addSecurityRoleChecksToModel(req, model);

        final String viewLocation = getViewLocation(req);
        return new ModelAndView(viewLocation, model);

    }

    private Map<String,Object> getBeans(PortletRequest req) {
        Map<String,Object> rslt = new HashMap<String,Object>();  // default
        PortletPreferences prefs = req.getPreferences();
        String[] beanNames = prefs.getValues(BEANS_PREFERENCE, new String[]{});
        for (String name : beanNames) {
            Object bean = applicationContext.getBean(name);
            rslt.put(name, bean);
        }
        logger.debug("Invoking with beans={}", (Object[]) beanNames);
        return rslt;
    }

    private Map<String,List<String>> getPreferences(PortletRequest req) {
        Map<String,List<String>> rslt = new HashMap<String,List<String>>();  // default
        PortletPreferences prefs = req.getPreferences();
        List<String> names = Collections.list(prefs.getNames());
        for (String name : names) {
            if (!name.startsWith(CONTROLLER_PREFERENCE_PREFIX)) {
                // Pass it along in the model
                List<String> values = Arrays.asList(prefs.getValues(name, new String[] {}));
                rslt.put(name, values);
            }
        }
        logger.debug("Invoking with preferences={}", rslt);
        return rslt;
    }

    private String getViewLocation(PortletRequest req) {
        String rslt;
        PortletPreferences prefs = req.getPreferences();
        String preferenceViewLocation = prefs.getValue(VIEW_LOCATION_PREFERENCE, null);
        if(StringUtils.isNotBlank(preferenceViewLocation)) {
            rslt = preferenceViewLocation;
        } else {
            throw new RuntimeException("Portlet preference '" + VIEW_LOCATION_PREFERENCE + "' not set");
        }
        logger.debug("Invoking with viewLocation={}", rslt);
        return rslt;
    }

    /**
     * Run through the list of configured security roles and add an "is"+Rolename to the model.  The security roles
     * must also be defined with a <code>&lt;security-role-ref&gt;</code> element in the portlet.xml.
     * @param req Portlet request
     * @param model Model object to add security indicators to
     */
    private void addSecurityRoleChecksToModel(PortletRequest req, Map<String,Object> model) {
        PortletPreferences prefs = req.getPreferences();
        String[] securityRoles = prefs.getValues(PREF_SECURITY_ROLE_NAMES, new String[]{});
        for (int i = 0; i < securityRoles.length; i++) {
            model.put("is"+securityRoles[i].replace(" ", "_"), req.isUserInRole(securityRoles[i]));
        }
    }

}
