/**
 * Copyright (c) 2000 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jasig.portal;

import java.security.SecureRandom;


/* 
 * An implementation of DEC UUID recommendation.
 */

public class UUID {
    private static SecureRandom sr=new SecureRandom();
    private byte[] id;
    private boolean dce=false;

    private static final char[] hexChars = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

    public UUID(byte[] id) {
	this.id=id;
    }

    /*
     * Constructor allows to specify if 
     * current UUID follows DCE recommendation
     */
    public UUID(byte[] id,boolean dce) {
	this(id);
	this.dce=dce;
    }

    public int getLength() {
	if(id==null) {
	    return -1;
	} else {
	    return id.length;
	}
    }

    public String toString() {
	return this.byteArrToHexString(id);
    }


    // DCE UUID spec-based output
    public String toDCEString() {
	if(!dce) return null;

	String time_low=byteArrToHexString(id,0,4);
	String time_mid=byteArrToHexString(id,4,2);
	String version_time_high=byteArrToHexString(id,6,2);
	String variant_clock_seq=byteArrToHexString(id,8,2);
	String node=byteArrToHexString(id,10,6);
	return new String(time_low+"-"+time_mid+"-"+version_time_high+"-"+variant_clock_seq+"-"+node);
    }


    public byte[] getBytes() {
	return id;
    }

    public static UUID generateUUID() {
	// Implementing variant 4 of DCE UUID recommendation (version 1)
	
	// generate a 128bit random buffer
	byte[] bytes=new byte[16];
	sr.nextBytes(bytes);

	// set the version
	bytes[6] &= 0x0f;
	bytes[6] |= 0x10; 
	// set the variant
 	bytes[8] &= 0x3f;
	bytes[8] |= 0x40; 

	long mostSig = 0;
	for (int i = 0; i < 8; i++) {
	    mostSig = (mostSig << 8) | (bytes[i] & 0xff);
	}
	long leastSig = 0;
	for (int i = 8; i < 16; i++) {
	    leastSig = (leastSig << 8) | (bytes[i] & 0xff);
	}
	return new UUID(bytes,true);
    };    

    private static String byteArrToHexString(byte bytes[]) {
	return byteArrToHexString(bytes,0,bytes.length);
     }
	
    private static String byteArrToHexString(byte bytes[],int offset,int count) {
 	char[] str=new char[count*2];
	int end=offset+count;
	if(end>bytes.length) end=bytes.length;
 	for (int i=offset; i<end; i++) {
 	    str[(i-offset)*2] = hexChars[(bytes[i] >> 4) & 0x0f];
 	    str[(i-offset)*2+1] = hexChars[bytes[i] & 0x0f];
 	}
 	return new String(str);
    }

    public static void main(String args[]) {
	int times=Integer.parseInt(args[0]);
	for(int i=0;i<times;i++) {
	    UUID id1=UUID.generateUUID();
	    System.out.println(id1.toDCEString());
	}
    }
}
