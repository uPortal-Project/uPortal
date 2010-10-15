package org.jasig.portal.security;

public interface IPortalPasswordService {
    
    public String encryptPassword(String cleartext);
    
    public boolean validatePassword(String cleartext, String encrypted);

}
