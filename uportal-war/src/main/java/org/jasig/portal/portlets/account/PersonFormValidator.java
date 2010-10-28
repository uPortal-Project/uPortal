package org.jasig.portal.portlets.account;

import org.apache.commons.lang.StringUtils;
import org.jasig.portal.persondir.ILocalAccountDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.stereotype.Component;

@Component("personValidator")
public class PersonFormValidator {

    private ILocalAccountDao accountDao;

    @Autowired(required = true)
    public void setLocalAccountDao(ILocalAccountDao accountDao) {
        this.accountDao = accountDao;
    }

    public void validateEditLocalAccount(PersonForm person, MessageContext context) {
        
        // ensure that this username isn't already taken
        if (person.getId() < 0 && accountDao.getPerson(person.getUsername()) != null) {
            context.addMessage(new MessageBuilder().error().source("username")
                    .code("username.in.use").build());
        } 
        
        if (StringUtils.isNotBlank(person.getPassword()) || StringUtils.isNotBlank(person.getConfirmPassword())) {
            if (person.getPassword() == null || !person.getPassword().equals(person.getConfirmPassword())) {
                context.addMessage(new MessageBuilder().error().source("password")
                        .code("passwords.must.match").build());
            }
        }
        
    }
    
}
