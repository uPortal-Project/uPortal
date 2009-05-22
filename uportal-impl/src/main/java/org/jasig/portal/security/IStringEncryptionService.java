package org.jasig.portal.security;

/**
 * IStringEncryptionService is a small interface for string encryption/decryption.
 * It is expected that this service will generally consist of a small wrapper
 * for some other encryption API.
 * 
 * @author bourey
 */
public interface IStringEncryptionService {
	
	/**
	 * Encrypt a string
	 * 
	 * @param plaintext
	 * @return
	 */
	public String encrypt(String plaintext);
	
	/**
	 * Decrypt a string
	 * 
	 * @param cryptotext
	 * @return
	 */
	public String decrypt(String cryptotext);

}
