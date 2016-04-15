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
package org.jasig.portal.portlets.account;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.i18n.ILocaleStore;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.layout.dlm.remoting.IGroupListHelper;
import org.jasig.portal.layout.dlm.remoting.JsonEntityBean;
import org.jasig.portal.persondir.ILocalAccountDao;
import org.jasig.portal.persondir.ILocalAccountPerson;
import org.jasig.portal.portletpublishing.xml.Preference;
import org.jasig.portal.portlets.StringListAttribute;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.security.IPortalPasswordService;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.url.IPortalUrlBuilder;
import org.jasig.portal.url.IPortalUrlProvider;
import org.jasig.portal.url.IPortletUrlBuilder;
import org.jasig.portal.url.UrlType;
import org.jasig.services.persondir.IPersonAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;


@Component("userAccountHelper")
public class UserAccountHelper {

    protected final Log log = LogFactory.getLog(getClass());
    private static final String PORTLET_FNAME_LOGIN = "login";

    private ILocaleStore localeStore;
    private ILocalAccountDao accountDao;
    private IPortalPasswordService passwordService;
    private List<Preference> accountEditAttributes;
    private IPortalUrlProvider urlProvider;
    private MessageSource messageSource;
    private IPersonManager personManager;
    private IGroupListHelper groupListHelper;
    private IPasswordResetNotification passwordResetNotification;
    private String loginPortletFolderName = "welcome";

    @Autowired
    public void setLocaleStore(ILocaleStore localeStore) {
        this.localeStore = localeStore;
    }

    @Autowired
    public void setLocalAccountDao(ILocalAccountDao accountDao) {
        this.accountDao = accountDao;
    }
    
    @Autowired
    public void setPortalPasswordService(IPortalPasswordService passwordService) {
        this.passwordService = passwordService;
    }
    
    @Resource(name="accountEditAttributes")
    public void setAccountEditAttributes(List<Preference> accountEditAttributes) {
        this.accountEditAttributes = Collections.unmodifiableList(accountEditAttributes);
    }
    
    @Autowired
    public void setPortalUrlProvider(IPortalUrlProvider urlProvider) {
        this.urlProvider = urlProvider;
    }

    @Autowired
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    @Autowired
    public void setGroupListHelper(IGroupListHelper groupListHelper) {
        this.groupListHelper = groupListHelper;
    }

    @Autowired
    public void setPasswordResetNotification(IPasswordResetNotification passwordResetNotification) {
        this.passwordResetNotification = passwordResetNotification;
    }

    @Value("${org.jasig.portal.folder.login-layout:welcome}")
    public void setLoginPortletFolderName(String loginPortletFolderName) {
        this.loginPortletFolderName = loginPortletFolderName;
    }

    public PersonForm getNewAccountForm() {
        
        PersonForm form = new PersonForm(accountEditAttributes);
        
        Set<String> attributeNames = accountDao.getCurrentAttributeNames();
        for (String name : attributeNames) {
            form.getAttributes().put(name, new StringListAttribute(Collections.<String>emptyList()));
        }
        
        return form;
    }

    public PersonForm getForm(String username) {
        
        ILocalAccountPerson person = accountDao.getPerson(username);
        
        PersonForm form = new PersonForm(accountEditAttributes);
        form.setUsername(person.getName());
        form.setId(person.getId());
        
        Set<String> attributeNames = accountDao.getCurrentAttributeNames();
        for (String name : attributeNames) {
            List<String> values = new ArrayList<String>();
            List<Object> attrValues = person.getAttributeValues(name);
            if (attrValues != null) {
                for (Object value : person.getAttributeValues(name)) {
                    values.add((String) value);
                }
            }
            form.getAttributes().put(name, new StringListAttribute(values));
        }

        return form;
    }

    protected boolean isLocalAccount(String username) {
        ILocalAccountPerson person = accountDao.getPerson(username);
        if (person != null) {
            return true;
        } else {
            return false;
        }
    }
    
    public List<JsonEntityBean> getParentGroups(String target) {
        IGroupMember member = GroupService.getEntity(target, IPerson.class);
        @SuppressWarnings("unchecked")
        Iterator<IGroupMember> iterator = (Iterator<IGroupMember>) member.getAncestorGroups();
        List<JsonEntityBean> parents = new ArrayList<JsonEntityBean>();
        while (iterator.hasNext()) {
            parents.add(groupListHelper.getEntity(iterator.next()));
        }
        Collections.sort(parents);
        return parents;
    }
    
    public boolean canEditUser(IPerson currentUser, String target) {
        
        // first check to see if this is a local user
        if (!isLocalAccount(target)) {
            return false;
        }
        
        EntityIdentifier ei = currentUser.getEntityIdentifier();
        IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
        
        // if the target represents the current user, determine if they can
        // edit their own account
        if (currentUser.getName().equals(target) && ap.hasPermission("UP_USERS", "EDIT_USER", "SELF")) {
            return true;
        } 
        
        // otherwise determine if the user has permission to edit the account
        else if (ap.hasPermission("UP_USERS", "EDIT_USER", target)) {
            return true;
        } 
        
        else {
            return false;
        }

    }
    
    /**
     * Returns the collection of attributes that the specified currentUser can 
     * edit.
     * 
     * @param currentUser
     * @return
     */
    public List<Preference> getEditableUserAttributes(IPerson currentUser) {
        
        EntityIdentifier ei = currentUser.getEntityIdentifier();
        IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
        
        List<Preference> allowedAttributes = new ArrayList<Preference>();
        for (Preference attr : accountEditAttributes) {
            if (ap.hasPermission("UP_USERS", "EDIT_USER_ATTRIBUTE", attr.getName())) {
                allowedAttributes.add(attr);
            }
        }
        return allowedAttributes;
    }
    
    public List<GroupedPersonAttribute> groupPersonAttributes(final IPersonAttributes user, final HttpServletRequest request) {
        
        // get the locale for the current user
        final Locale locale = getCurrentUserLocale(request);

        // construct a list of grouped user attributes for the specified user
        final List<GroupedPersonAttribute> displayAttributes = new ArrayList<GroupedPersonAttribute>();
        for (Map.Entry<String, List<Object>> attr : user.getAttributes().entrySet()) {
            
            // get the display name for this user attribute
            final String displayName = messageSource.getMessage("attribute.displayName.".concat(attr.getKey()), new Object[]{}, attr.getKey(), locale);
            boolean found = false;

            // if this display name and value is already in the list, add this 
            // new attribute name to that group
            for (GroupedPersonAttribute displayAttribute : displayAttributes) {
                if (displayAttribute.getDisplayName().equals(displayName) && attr.getValue().equals(displayAttribute.getValues())) {
                    displayAttribute.getAttributeNames().add(attr.getKey());
                    found = true;
                    break;
                }
            }
            
            // otherwise add a new group
            if (!found) {
                final GroupedPersonAttribute displayAttribute = new GroupedPersonAttribute(displayName, attr.getValue(), attr.getKey());
                displayAttributes.add(displayAttribute);
            }
        }
        
        Collections.sort(displayAttributes, new GroupedPersonAttributeByNameComparator());
        return displayAttributes;
    }

    public boolean canDeleteUser(IPerson currentUser, String target) {
        
        // first check to see if this is a local user
        if (!isLocalAccount(target)) {
            return false;
        }
        
        EntityIdentifier ei = currentUser.getEntityIdentifier();
        IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
        // TODO create new user editing permission
        return (ap.hasPermission("UP_USERS", "DELETE_USER", target));
    }
    
    public void deleteAccount(IPerson currentUser, String target) {
        
        if (!canDeleteUser(currentUser, target)) {
            throw new RuntimeException("Current user " + currentUser.getName()
                    + " does not have permissions to update person " + target);
        }
        
        ILocalAccountPerson person = accountDao.getPerson(target);
        
        accountDao.deleteAccount(person);
        log.info("Account " + person.getName() + " successfully deleted");
        
    }
    
    public void updateAccount(IPerson currentUser, PersonForm form) {
        
        ILocalAccountPerson account;
        
        // if this is a new user, create an account object with the specified
        // username
        if (form.getId() < 0) {
            account = accountDao.getPerson(form.getUsername());
            if (account == null) {
                /*
                 * Should there be a permissions check to verify 
                 * the user is allowed to create new users?
                 */
                account = accountDao.createPerson(form.getUsername());
            }
        } 
        
        // otherwise, get the existing account from the database
        else {
            account = accountDao.getPerson(form.getId());
        }
        
        /*
         * SANITY CHECK #1:  Is the user permitted to modify this account?  
         * (Presumably this check was already made when the page was rendered, 
         * but re-checking alleviates danger from cleverly-crafted HTTP 
         * requests.) 
         */
        if (!canEditUser(currentUser, account.getName())) {
            throw new RuntimeException("Current user " + currentUser.getName()
                    + " does not have permissions to update person " 
                    + account.getName());
        }

        // Used w/ check #2
        EntityIdentifier ei = currentUser.getEntityIdentifier();
        IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
        
        // update the account attributes to match those specified in the form
        List<Preference> editableAttributes = getEditableUserAttributes(currentUser);
        for (Preference editableAttribute : editableAttributes) {
            String attributeName = editableAttribute.getName();

            /*
             * SANITY CHECK #2:  Should never fail since getEditableUserAttributes should return only
             * editable attribute names, but do this anyway just in case.
             */
            if (!ap.hasPermission("UP_USERS", "EDIT_USER_ATTRIBUTE", attributeName)) {
                throw new RuntimeException("Current user " + currentUser.getName()
                        + " does not have permissions to edit attribute " + attributeName);
            }

            if (form.getAttributes().get(attributeName) == null || form.getAttributes().get(attributeName).isBlank()) {
                account.removeAttribute(attributeName);
            } else {
                account.setAttribute(attributeName, form.getAttributes().get(attributeName).getValue());
            }
        }

        // if a new password has been specified, update the account password
        if (StringUtils.isNotBlank(form.getPassword())) {
            account.setPassword(passwordService.encryptPassword(form.getPassword()));
            account.setLastPasswordChange(new Date());
            account.removeAttribute("loginToken");
        }
        
        accountDao.updateAccount(account);
        log.info("Account " + account.getName() + " successfully updated");
    }
    
    public String getRandomToken() {
        String token = RandomStringUtils.randomAlphanumeric(20);
        return token;
    }
    
    public boolean validateLoginToken(String username, String token) {
        ILocalAccountPerson person = accountDao.getPerson(username);
        if (person != null) {
            Object recordedToken = person.getAttributeValue("loginToken");
            if (recordedToken != null && recordedToken.equals(token)) {
                if (log.isInfoEnabled()) {
                    log.info("Successfully validated security token for user:  " + username);
                }
                return true;
            } else {
                if (log.isInfoEnabled()) {
                    log.info("Unable to validate security token;  recordedToken=" + recordedToken + ", submitted token=" + token);
                }
            }
        } else {
            if (log.isInfoEnabled()) {
                log.info("Unable to validate security token;  person not found:  " + username);
            }
        }
        return false;
    }

    public void sendLoginToken(HttpServletRequest request, ILocalAccountPerson account) {
        sendLoginToken(request, account, passwordResetNotification);
    }
    
    public void sendLoginToken(HttpServletRequest request, ILocalAccountPerson account, IPasswordResetNotification notification) {

        Locale locale = getCurrentUserLocale(request);

        IPortalUrlBuilder builder = urlProvider.getPortalUrlBuilderByPortletFName(request, PORTLET_FNAME_LOGIN, UrlType.RENDER);
        IPortletUrlBuilder portletUrlBuilder = builder.getTargetedPortletUrlBuilder();
        portletUrlBuilder.addParameter("username", account.getName());
        portletUrlBuilder.addParameter("loginToken", (String) account.getAttributeValue("loginToken"));
        portletUrlBuilder.setPortletMode(PortletMode.VIEW);
        portletUrlBuilder.setWindowState(WindowState.MAXIMIZED);

        try {
            String path = fixPortletPath(request, builder);

            URL url = new URL(request.getScheme(), request.getServerName(),
                    request.getServerPort(), path);

            notification.sendNotification(url, account, locale);
        } catch (MalformedURLException e) {
            log.error(e);
        }
    }

    /**
     * Similar to updateAccount, but narrowed to the password and re-tooled to 
     * work as the guest user (which is what you are, when you have a valid 
     * security token).
     */
    public void createPassword(PersonForm form, String token) {

        final String username = form.getUsername();

        // Re-validate the token to prevent URL hacking
        if (!validateLoginToken(username, token)) {
            throw new RuntimeException("Attempt to set a password for user '" 
                    + username + "' without a valid security token");
        }

        final String password = form.getPassword();
        if (StringUtils.isNotBlank(password)) {
            if (!password.equals(form.getConfirmPassword())) {
                throw new RuntimeException("Passwords don't match");
            }

            ILocalAccountPerson account = accountDao.getPerson(username);
            account.setPassword(passwordService.encryptPassword(password));
            account.setLastPasswordChange(new Date());
            account.removeAttribute("loginToken");
            accountDao.updateAccount(account);
            if (log.isInfoEnabled()) {
                log.info("Password created for account:  " + account);
            }
        } else {
            throw new RuntimeException("Attempt to set a password for user '" 
                    + form.getUsername() + "' but the password was blank");
        }

    }

    protected Locale getCurrentUserLocale(final HttpServletRequest request) {
        final IPerson person = personManager.getPerson(request);
        final Locale[] userLocales = localeStore.getUserLocales(person);
        final LocaleManager localeManager = new LocaleManager(person, userLocales);
        final Locale locale = localeManager.getLocales()[0];
        return locale;
    }


    /**
     * This entire method is a hack!  The login portlet URL only seems to load correctly if
     * you pass in the folder name for the guest layout.  For the short term, allow configuration
     * of the folder name and manually re-write the URL to include the folder if it doesn't already.
     *
     * This is terrible and needs to go away as soon as I can find a better approach.
     *
     * @param request
     * @param urlBuilder
     * @return
     */
    private String fixPortletPath(HttpServletRequest request, IPortalUrlBuilder urlBuilder) {
        String path = urlBuilder.getUrlString();
        String context = request.getContextPath();
        if (StringUtils.isBlank(context)) {
            context = "/";
        }

        if (!path.startsWith(context + "/f/")) {
            return path.replaceFirst(context, context + "/f/" + loginPortletFolderName);
        }

        return path;
    }
}
