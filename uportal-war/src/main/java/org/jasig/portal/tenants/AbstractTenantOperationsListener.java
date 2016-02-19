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
package org.jasig.portal.tenants;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.i18n.ILocaleStore;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.tenants.TenantOperationResponse.Result;
import org.jasig.portal.url.IPortalRequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

/**
 * Implements all methods of {@link ITenantOperationsListener} as no-ops and can
 * therefore serve as a base class for concrete listeners that need to override
 * some but not all of the methods.
 * 
 * @since 4.1
 * @author awills
 */
public abstract class AbstractTenantOperationsListener implements ITenantOperationsListener {

    private static final String NO_OPERATIONS_PERFORMED = "no.operations.performed";

    private final String fname;

    @Autowired
    private IPortalRequestUtils portalRequestUtils;

    @Autowired
    private IPersonManager personManager;

    @Autowired
    private ILocaleStore localeStore;

    @Autowired
    private MessageSource messageSource;

    protected AbstractTenantOperationsListener(final String fname) {
        this.fname = fname;
    }

    @Override
    public final String getName() {
        return messageSource.getMessage(getClass().getName() + ".name", null, getCurrentUserLocale());
    }

    /**
     * @since uPortal 4.3
     */
    @Override
    public final String getFname() {
        return fname;
    }

    /**
     * @since uPortal 4.3
     */
    @Override
    public boolean isOptional() {
        return false;
    }

    @Override
    public TenantOperationResponse onCreate(ITenant tenant) {
        return getDefaultResponse();
    }

    @Override
    public TenantOperationResponse onUpdate(ITenant tenant) {
        return getDefaultResponse();
    }

    @Override
    public TenantOperationResponse onDelete(ITenant tenant) {
        return getDefaultResponse();
    }

    /**
     * @since uPortal 4.3
     */
    @Override
    public Set<ITenantManagementAction> getAvaialableActions() {
        return Collections.emptySet();
    }

    /**
     * Default implementation is a no-op.
     */
    @Override
    public void validateAttribute(final String key, final String value) throws Exception {}

    /**
     * @since uPortal 4.3
     */
    protected String createLocalizedMessage(final String messageCode, final Object[] args) {
        final Locale locale = getCurrentUserLocale();
        return messageSource.getMessage(messageCode, args, locale);
    }

    /*
     * Implementation
     */

    private Locale getCurrentUserLocale() {
        final HttpServletRequest req = this.portalRequestUtils.getCurrentPortalRequest();
        final IPerson person = personManager.getPerson(req);
        final Locale[] userLocales = localeStore.getUserLocales(person);
        final LocaleManager localeManager = new LocaleManager(person, userLocales);
        final Locale locale = localeManager.getLocales()[0];
        return locale;
    }

    private TenantOperationResponse getDefaultResponse() {
        TenantOperationResponse rslt = new TenantOperationResponse(this, Result.IGNORE);
        return rslt;
    }

}
