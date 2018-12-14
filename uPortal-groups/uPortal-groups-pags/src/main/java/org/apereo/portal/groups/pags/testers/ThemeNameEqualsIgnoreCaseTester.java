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
package org.apereo.portal.groups.pags.testers;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.apereo.portal.IUserProfile;
import org.apereo.portal.groups.pags.IPersonTester;
import org.apereo.portal.groups.pags.dao.IPersonAttributesGroupTestDefinition;
import org.apereo.portal.layout.IUserLayoutStore;
import org.apereo.portal.layout.dao.IStylesheetDescriptorDao;
import org.apereo.portal.layout.om.IStylesheetDescriptor;
import org.apereo.portal.layout.profile.IProfileMapper;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.spring.locator.ApplicationContextLocator;
import org.apereo.portal.url.IPortalRequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * This Group tester checks the current session's user profile theme against the test value ignoring
 * case.
 *
 * @deprecated Avoid this PAGS tester because (1) it doesn't function outside of a container request
 *     thread; and (2) it isn't necessary any longer (without Universality).
 */
@Deprecated
public class ThemeNameEqualsIgnoreCaseTester implements IPersonTester {

    protected Logger logger = LoggerFactory.getLogger(getClass());
    private String themeTestValue;
    private final ApplicationContext applicationContext;
    private final IPortalRequestUtils portalRequestUtils;
    private final IProfileMapper profileMapper;
    private final IUserLayoutStore userLayoutStore;
    private final IStylesheetDescriptorDao stylesheetDescriptorDao;

    /** @since 4.3 */
    public ThemeNameEqualsIgnoreCaseTester(IPersonAttributesGroupTestDefinition definition) {
        final String themeTestValue = definition.getTestValue();
        assert StringUtils.isNotBlank(themeTestValue);
        this.themeTestValue = themeTestValue;
        this.applicationContext = ApplicationContextLocator.getApplicationContext();
        this.portalRequestUtils = applicationContext.getBean(IPortalRequestUtils.class);
        this.profileMapper = applicationContext.getBean("profileMapper", IProfileMapper.class);
        this.userLayoutStore =
                applicationContext.getBean("userLayoutStore", IUserLayoutStore.class);
        this.stylesheetDescriptorDao =
                applicationContext.getBean(
                        "stylesheetDescriptorDao", IStylesheetDescriptorDao.class);
    }

    @Override
    public boolean test(IPerson person) {
        HttpServletRequest currentPortalRequest = getCurrentHttpServletRequest();
        if (currentPortalRequest == null) {
            return false;
        }
        IStylesheetDescriptor descriptor =
                getCurrentUserProfileStyleSheetDescriptor(person, currentPortalRequest);
        String uiTheme = descriptor.getName();
        logDebugMessages(uiTheme);
        return getStringCompareResults(uiTheme);
    }

    private HttpServletRequest getCurrentHttpServletRequest() {
        HttpServletRequest currentPortalRequest = null;
        try {
            currentPortalRequest = portalRequestUtils.getCurrentPortalRequest();
        } catch (IllegalStateException e) {
            logger.warn(
                    "No HttpServletRequest is available for testing, which may lead to "
                            + "surprising outcomes;  ThemeNameEqualsIgnoreCaseTester is deprecated and should "
                            + "not be used.",
                    e);
        }
        return currentPortalRequest;
    }

    private IStylesheetDescriptor getCurrentUserProfileStyleSheetDescriptor(
            IPerson person, HttpServletRequest currentPortalRequest) {
        final String currentFname = profileMapper.getProfileFname(person, currentPortalRequest);
        IUserProfile profile = userLayoutStore.getSystemProfileByFname(currentFname);
        int profileId = profile.getThemeStylesheetId();
        return stylesheetDescriptorDao.getStylesheetDescriptor(profileId);
    }

    private void logDebugMessages(String uiTheme) {
        if (logger.isDebugEnabled()) {
            logger.debug("themeTestValue: {}", themeTestValue);
            logger.debug("uiTheme: {}", uiTheme);
            logger.debug("getStringCompareResults(uiTheme): {}", getStringCompareResults(uiTheme));
        }
    }

    private boolean getStringCompareResults(String uiTheme) {
        boolean testResult = false;
        if (StringUtils.isNotBlank(uiTheme)) {
            testResult = uiTheme.equalsIgnoreCase(themeTestValue);
        }
        return testResult;
    }
}
