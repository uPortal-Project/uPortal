/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.security;

import org.jasypt.encryption.pbe.PBEStringEncryptor;

/**
 * JasyptPBEStringEncryptionServiceImpl is an implementation of 
 * IStringEncryptionService that uses a configurable Jasypt PBEStringEncryptor
 * to perform string encryption and decryption.
 * 
 * @author Jen Bourey
 */
public class JasyptPBEStringEncryptionServiceImpl implements IStringEncryptionService {

	PBEStringEncryptor encryptor = null;
	
	/**
	 * Set the PBEStringEncryptor to be used
	 * 
	 * @param encryptor
	 */
	public void setStringEncryptor(PBEStringEncryptor encryptor) {
		this.encryptor = encryptor;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String encrypt(String plaintext) {
		return this.encryptor.encrypt(plaintext);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String decrypt(String cryptotet) {
		return this.encryptor.decrypt(cryptotet);
	}
	
}
