package org.jasig.portal.portlets.permissionsadmin;

import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.stereotype.Component;

/**
 * Validator for the permission editing subflow.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
@Component
public class PermissionDefinitionFormValidator {
    
    public void validateEditPermission(PermissionDefinitionForm form, MessageContext messageContext) {

        // ensure at least one principal has been assigned
        if (form.getPermissions().isEmpty()) {
            messageContext.addMessage(new MessageBuilder().error().source("principal")
                .defaultText("Specify one or more principals").build());
        }

    }

}
