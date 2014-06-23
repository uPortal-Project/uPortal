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

package org.jasig.portal.tenants;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.persondir.ILocalAccountDao;
import org.jasig.portal.persondir.ILocalAccountPerson;
import org.jasig.portal.portlets.account.IPasswordResetNotification;
import org.jasig.portal.portlets.account.UserAccountHelper;
import org.jasig.portal.url.IPortalRequestUtils;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Integrates with the 'forgot-password' framework portlet to grant the tenant 
 * admin contact access via local authentication when a new tenant is created.
 * 
 * <p>This behavior is completely optional.  You can add or remove it by
 * adjusting the 'tenantOperationsListeners' bean in servicesContext.xml.
 */
public final class ResetPasswordTenantOperationsListener extends AbstractTenantOperationsListener {

    public static final String ADMIN_CONTACT_USERNAME = "adminContactUsername";
    public static final String ADMIN_CONTACT_EMAIL = "adminContactEmail";

    @Autowired
    private ILocalAccountDao localAccountDao;

    @Autowired
    private IPortalRequestUtils portalRequestUtils;

    @Autowired
    private UserAccountHelper userAccountHelper;

    private IPasswordResetNotification passwordResetNotification;

    @Autowired
    public void setPasswordResetNotification(IPasswordResetNotification passwordResetNotification) {
        this.passwordResetNotification = passwordResetNotification;
    }

    @Override
    public void onCreate(final ITenant tenant) {
        sendResetPasswordEmail(tenant);
    }

    @Override
    public void onUpdate(final ITenant tenant) {
        // Send email here as well, in case the contact changes
        sendResetPasswordEmail(tenant);
    }

    /*
     * Implementation
     */

    private void sendResetPasswordEmail(final ITenant tenant) {
        ILocalAccountPerson admin = localAccountDao.getPerson(tenant.getAttribute(ADMIN_CONTACT_USERNAME));
        admin.setAttribute("loginToken", userAccountHelper.getRandomToken());
        final HttpServletRequest http = portalRequestUtils.getCurrentPortalRequest();
        this.userAccountHelper.sendLoginToken(http, admin, passwordResetNotification);
        localAccountDao.updateAccount(admin);
    }

}
