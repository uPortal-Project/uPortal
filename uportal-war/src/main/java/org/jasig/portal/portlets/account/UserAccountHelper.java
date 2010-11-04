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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.persondir.ILocalAccountDao;
import org.jasig.portal.persondir.ILocalAccountPerson;
import org.jasig.portal.persondir.dao.jpa.LocalAccountPersonImpl;
import org.jasig.portal.portlets.StringListAttribute;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPortalPasswordService;
import org.jasig.portal.services.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("userAccountHelper")
public class UserAccountHelper {

    protected final Log log = LogFactory.getLog(getClass());
    
    private ILocalAccountDao accountDao;

    @Autowired(required = true)
    public void setLocalAccountDao(ILocalAccountDao accountDao) {
        this.accountDao = accountDao;
    }
    
    private IPortalPasswordService passwordService;
    
    @Autowired(required = true)
    public void setPortalPasswordService(IPortalPasswordService passwordService) {
        this.passwordService = passwordService;
    }
    
    public PersonForm getNewAccountForm() {
        
        PersonForm form = new PersonForm();
        
        Set<String> attributeNames = accountDao.getPossibleUserAttributeNames();
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
        
        Set<String> attributeNames = accountDao.getPossibleUserAttributeNames();
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
        // TODO create new user editing permission
        return (ap.hasPermission("UP_USERS", "EDIT_USER", target));
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
            account = new LocalAccountPersonImpl();
            account.setName(form.getUsername());
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
        }
        
        accountDao.updateAccount(account);
        log.info("Account " + account.getName() + " successfully updated");
    }
    
}
