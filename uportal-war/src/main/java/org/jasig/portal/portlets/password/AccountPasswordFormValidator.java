package org.jasig.portal.portlets.password;

import org.apache.commons.lang.StringUtils;
import org.jasig.portal.persondir.ILocalAccountDao;
import org.jasig.portal.persondir.ILocalAccountPerson;
import org.jasig.portal.security.IPortalPasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
@Component("accountPasswordFormValidator")
public class AccountPasswordFormValidator {

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
    
    public void validateEnterPassword(AccountPasswordForm form, MessageContext context) {
        
        // ensure that a current account password was entered
        if (StringUtils.isBlank(form.getCurrentPassword())) {
            context.addMessage(new MessageBuilder().error().source("currentPassword")
                    .code("please.enter.current.password")
                    .defaultText("Please enter your current password").build());
        }
        
        // check to see if the provided password matches the current account
        // password
        else {
            
            ILocalAccountPerson account = accountDao.getPerson(form.getUserId());
            if (!passwordService.validatePassword(form.getCurrentPassword(), account.getPassword())) {
                context.addMessage(new MessageBuilder().error().source("currentPassword")
                        .code("current.password.doesnt.match")
                        .defaultText("Provided password does not match the current account password").build());
            }

        }
        
        // ensure a new account password was entered
        if (StringUtils.isBlank(form.getNewPassword())) {
            context.addMessage(new MessageBuilder().error().source("newPassword")
                    .code("please.enter.new.password")
                    .defaultText("Please enter a new password").build());
        }
        
        // ensure a new account password confirmation was entered
        if (StringUtils.isBlank(form.getConfirmNewPassword())) {
            context.addMessage(new MessageBuilder().error().source("confirmNewPassword")
                    .code("please.enter.confirm.password")
                    .defaultText("Please confirm your new password").build());
        }
        
        // ensure the new password and new password confirmation match
        if (StringUtils.isNotBlank(form.getNewPassword())
                && StringUtils.isNotBlank(form.getConfirmNewPassword())
                && !form.getNewPassword().equals(form.getConfirmNewPassword())) {
            
            context.addMessage(new MessageBuilder().error().source("confirmPassword")
                    .code("passwords.must.match")
                    .defaultText("Passwords must match").build());
        }
        
    }
    
}
