package org.jasig.portal.security;

import org.jasypt.digest.config.SimpleDigesterConfig;
import org.jasypt.util.password.ConfigurablePasswordEncryptor;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
@Component("portalPasswordService")
public class PortalPasswordServiceImpl implements IPortalPasswordService {

    protected final String MD5_PREFIX = "(MD5)";
    protected final String SHA256_PREFIX = "(SHA256)";
    
    private ConfigurablePasswordEncryptor md5Encryptor;    
    private ConfigurablePasswordEncryptor sha256Encryptor;
    
    /**
     * Create a new PortalPasswordService, initializing all required password
     * encryption resources.
     */
    public PortalPasswordServiceImpl() {
        
        /*
         * Create an MD5 password encryptor that uses an 8-byte salt with one
         * hash iteration.  This encryptor should be  capable of validating 
         * legacy uPortal passwords. 
         */
        md5Encryptor = new ConfigurablePasswordEncryptor();
        SimpleDigesterConfig md5Config = new SimpleDigesterConfig();
        md5Config.setIterations(1);
        md5Config.setAlgorithm("MD5");
        md5Config.setSaltSizeBytes(8);
        md5Encryptor.setConfig(md5Config);
        
        /*
         * Create a stronger SHA-256 password encryptor for setting and 
         * validating new passwords.
         */
        sha256Encryptor = new ConfigurablePasswordEncryptor();
        SimpleDigesterConfig shaConfig = new SimpleDigesterConfig();
        shaConfig.setIterations(1000);
        shaConfig.setAlgorithm("SHA-256");
        shaConfig.setSaltSizeBytes(8);
        sha256Encryptor.setConfig(shaConfig);

    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.security.IPortalPasswordService#encryptPassword(java.lang.String)
     */
    public String encryptPassword(String cleartext) {
        String encrypted = sha256Encryptor.encryptPassword(cleartext);
        if (encrypted.endsWith("\n")) {
            encrypted = encrypted.substring(0, encrypted.length()-1);
        }
        encrypted = SHA256_PREFIX.concat(encrypted);
        return encrypted;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.security.IPortalPasswordService#validatePassword(java.lang.String, java.lang.String)
     */
    public boolean validatePassword(String cleartext, String encrypted) {
        if (encrypted.startsWith(MD5_PREFIX)) {
            encrypted = encrypted.substring(5);
            return md5Encryptor.checkPassword(cleartext, encrypted);
        } else if (encrypted.startsWith(SHA256_PREFIX)) {
            encrypted = encrypted.substring(8);
            return sha256Encryptor.checkPassword(cleartext, encrypted);
        } else {
            throw new IllegalArgumentException(
                    "This portal password service is only currently capable of validating MD5 and SHA-256 passwords");
        }
    }

}
