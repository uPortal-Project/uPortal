package org.jasig.portal.portlets.account.selfedit;

import java.io.Serializable;

/**
 * Form object (DTO) representing a password change request.
 * 
 * @author apetro
 * @author jbourey
 *
 */
public class AccountPasswordForm implements Serializable {

    private long userId;
    private String currentPassword;
    private String newPassword;
    private String confirmNewPassword;
    
    public String getCurrentPassword() {
        return currentPassword;
    }
    
    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }
    
    public String getNewPassword() {
        return newPassword;
    }
    
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
    
    public String getConfirmNewPassword() {
        return confirmNewPassword;
    }
    
    public void setConfirmNewPassword(String confirmNewPassword) {
        this.confirmNewPassword = confirmNewPassword;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

}
