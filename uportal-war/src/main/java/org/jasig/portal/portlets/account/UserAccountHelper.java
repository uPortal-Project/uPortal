package org.jasig.portal.portlets.account;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.persondir.ILocalAccountDao;
import org.jasig.portal.persondir.ILocalAccountPerson;
import org.jasig.portal.portlets.StringListAttribute;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.Md5Passwd;
import org.jasig.portal.services.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("userAccountHelper")
public class UserAccountHelper {
    
    private ILocalAccountDao accountDao;

    @Autowired(required = true)
    public void setLocalAccountDao(ILocalAccountDao accountDao) {
        this.accountDao = accountDao;
    }
    
    public PersonForm getForm(String username) {
        
        ILocalAccountPerson person = accountDao.getPerson(username);
        
        PersonForm form = new PersonForm();
        form.setUsername(person.getName());
        
        Map<String, StringListAttribute> attributes = new HashMap<String, StringListAttribute>();
        for (Map.Entry<String, List<Object>> attribute : person.getAttributes().entrySet()) {
            List<String> values = new ArrayList<String>();
            for (Object value : attribute.getValue()) {
                values.add((String) value);
            }
            
            attributes.put(attribute.getKey(), new StringListAttribute(values));
        }
        form.setAttributes(attributes);
        
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
        ILocalAccountPerson person = accountDao.getPerson(target);
        accountDao.deleteAccount(person);
    }
    
    public void updateAccount(IPerson currentUser, PersonForm form) {
        ILocalAccountPerson account = accountDao.getPerson(form.getUsername());
        
        // update the account attributes to match those specified in the form
        Map<String, List<String>> attributes = new HashMap<String, List<String>>();        
        for (Map.Entry<String, StringListAttribute> entry : form.getAttributes().entrySet()) {
            if (entry.getValue() != null) {
                attributes.put(entry.getKey(), entry.getValue().getValue());
            }
        }
        account.setAttributes(attributes);
        
        // if a new password has been specified, update the account password
        if (StringUtils.isNotBlank(form.getPassword()) && StringUtils.isNotBlank(form.getConfirmPassword())) {
            account.setPassword(Md5Passwd.encode(form.getPassword()));
            account.setLastPasswordChange(new Date());
        }
        
        accountDao.updateAccount(account);
    }
    
}
