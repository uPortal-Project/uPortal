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

package org.jasig.portal.portlets.account;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

@Component("userAccountHelper")
public class UserAccountHelper {

    protected final Log log = LogFactory.getLog(getClass());
    
    private String passwordResetTemplate  = "properties/templates/passwordReset";
    private STGroup stringTemplateGroup = new STGroup('$', '$');
    private ILocaleStore localeStore;
    private ILocalAccountDao accountDao;
    private IPortalPasswordService passwordService;
    private List<Preference> accountEditAttributes;
    private JavaMailSenderImpl mailSender;
    private IPortalUrlProvider urlProvider;
    private MessageSource messageSource;
    private String portalEmailAddress;
    private IPersonManager personManager;
    private IGroupListHelper groupListHelper;
    
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
        this.accountEditAttributes = accountEditAttributes;
    }
    
    @Autowired
    public void setMailSender(JavaMailSenderImpl mailSender) {
        this.mailSender = mailSender;
    }

    @Autowired
    public void setPortalUrlProvider(IPortalUrlProvider urlProvider) {
        this.urlProvider = urlProvider;
    }

    @Autowired
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    
    @Resource(name="portalEmailAddress")
    public void setPortalEmailAddress(String portalEmailAddress) {
        this.portalEmailAddress = portalEmailAddress;
    }
    
    
    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    @Autowired
    public void setGroupListHelper(IGroupListHelper groupListHelper) {
        this.groupListHelper = groupListHelper;
    }

    
    public PersonForm getNewAccountForm() {
        
        PersonForm form = new PersonForm();
        
        Set<String> attributeNames = accountDao.getCurrentAttributeNames();
        for (String name : attributeNames) {
            form.getAttributes().put(name, new StringListAttribute(Collections.<String>emptyList()));
        }
        
        return form;
    }

    public PersonForm getForm(String username) {
        
        ILocalAccountPerson person = accountDao.getPerson(username);
        
        PersonForm form = new PersonForm();
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
        Iterator<IGroupMember> iterator = (Iterator<IGroupMember>) member.getAllContainingGroups();
        List<JsonEntityBean> parents = new ArrayList<JsonEntityBean>();
        while (iterator.hasNext()) {
            parents.add(groupListHelper.getEntity(iterator.next()));
        }
        Collections.sort(parents);
        return parents;
    }
    
    public boolean canImpersonateUser(IPerson currentUser, String target) {
        EntityIdentifier ei = currentUser.getEntityIdentifier();
        IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
        return ap.hasPermission("UP_USERS", "IMPERSONATE", target);
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
    
    public void updateAccount(PersonForm form) {
        
        ILocalAccountPerson account;
        
        // if this is a new user, create an account object with the specified
        // username
        if (form.getId() < 0) {
            account = accountDao.getPerson(form.getUsername());
            if (account == null) {
                account = accountDao.createPerson(form.getUsername());
            }
        } 
        
        // otherwise, get the existing account from the database
        else {
            account = accountDao.getPerson(form.getId());
        }
        
        // update the account attributes to match those specified in the form
        Map<String, List<String>> attributes = new HashMap<String, List<String>>();        
        for (Map.Entry<String, StringListAttribute> entry : form.getAttributes().entrySet()) {
            if (entry.getValue() != null) {
                attributes.put(entry.getKey(), entry.getValue().getValue());
            }
        }
        account.setAttributes(attributes);
        
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
    
    public boolean validateLoginToken(String username, String password) {
        ILocalAccountPerson person = accountDao.getPerson(username);
        if (person != null) {
            Object recordedToken = person.getAttributeValue("loginToken");
            if (recordedToken != null && recordedToken.equals(password)) {
                return true;
            }
        }
        return false;
    }
    
    public void sendLoginToken(HttpServletRequest request, ILocalAccountPerson account) {
        
        IPerson person = personManager.getPerson(request);
        final Locale[] userLocales = localeStore.getUserLocales(person);
        LocaleManager localeManager = new LocaleManager(person, userLocales);
        Locale locale = localeManager.getLocales()[0];
        
        IPortalUrlBuilder builder = urlProvider.getPortalUrlBuilderByPortletFName(request, "reset-password", UrlType.RENDER);
        IPortletUrlBuilder portletUrlBuilder = builder.getTargetedPortletUrlBuilder();
        portletUrlBuilder.addParameter("username", account.getName());
        portletUrlBuilder.addParameter("loginToken", (String) account.getAttributeValue("loginToken"));
        portletUrlBuilder.setPortletMode(PortletMode.VIEW);
        portletUrlBuilder.setWindowState(WindowState.MAXIMIZED);

        StringBuffer url = new StringBuffer(); 
        url.append(request.getScheme());
        url.append("://").append(request.getServerName());
        int port = request.getServerPort();
        if (port != 80 && port != 443) {
            url.append(":").append(port);
        }
        url.append(builder.getUrlString());
        
        log.debug("Sending password reset instructions to user with url " + url.toString());

        String emailAddress = (String) account.getAttributeValue("mail");
        
        final ST template = stringTemplateGroup
            .getInstanceOf(passwordResetTemplate);
        template.add("displayName", account.getAttributeValue("given") + " " + account.getAttributeValue("sn"));
        template.add("url", url.toString());

        MimeMessage message = mailSender.createMimeMessage();

        try {
            
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(emailAddress);
            helper.setText(template.toString(), true);
            helper.setSubject(messageSource.getMessage("reset.your.password", new Object[]{}, locale));
            helper.setFrom(portalEmailAddress, messageSource.getMessage("portal.name", new Object[]{}, locale));

            log.debug("Sending message to " + emailAddress + " from " + 
                      Arrays.toString(message.getFrom()) +  " subject " + message.getSubject());
            this.mailSender.send(message);
            
        } catch(MailException e) {
            log.error("Unable to send password reset email ", e);
        } catch (MessagingException e) {
            log.error("Unable to send password reset email ", e);
        } catch (UnsupportedEncodingException e) {
            log.error("Unable to send password reset email ", e);
        }
    }
    
}
