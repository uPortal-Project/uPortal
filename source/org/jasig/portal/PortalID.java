/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
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
import java.io.*;

/*
 * A generic class for storing all sorts of unique IDs
 */

public class PortalID {
    protected byte[] id;

    protected static final char[] hexChars = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

    public PortalID() {
    };

    /*
     * Creates a new ID object given the byte[] representation of a key.
     */
    public PortalID(byte[] id) {
        this.id=id;
    }

    /*
     * Create an integer id
     */
    public PortalID(int id) {
        this.id=new byte[4];
        this.id[0]=(byte) id;
        this.id[1]=(byte) (id>>8);
        this.id[2]=(byte) (id>>16);
        this.id[3]=(byte) (id>>24);
    }

    /*
     * Creates a new ID object given a string representation of a key.
     * By default, the string is taken to be a hexadecimal representation of a byte array.
     */
    public PortalID(String id) {
        this.id=hexStringToByteArr(id);
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

    public byte[] getBytes() {
        return id;
    }

    public DataInputStream getDataInputStream() {
        return new DataInputStream(new ByteArrayInputStream(id));
    }

    protected static String byteArrToHexString(byte bytes[]) {
        return byteArrToHexString(bytes,0,bytes.length);
     }

    protected  static String byteArrToHexString(byte bytes[],int offset,int count) {
        char[] str=new char[count*2];
        int end=offset+count;
        if(end>bytes.length) end=bytes.length;
        for (int i=offset; i<end; i++) {
            str[(i-offset)*2] = hexChars[(bytes[i] >> 4) & 0x0f];
            str[(i-offset)*2+1] = hexChars[bytes[i] & 0x0f];
        }
        return new String(str);
    }

    protected static byte[] hexStringToByteArr(String s){
        int len=(s.length()+1)/2;
        byte[] b = new byte[len];
        int shift=0;
        if(s.length()%2==1) {
            Integer in=new Integer(Integer.parseInt(s.substring(0,1),16));
            b[0]=in.byteValue();
            shift=1;
        }

        for(int j=0;j<len;j++) {
            int spos=j*2+shift;
            Integer in=new Integer(Integer.parseInt(s.substring(spos,spos+2),16));
            b[j+shift]=in.byteValue();
        }
        return b;
    }

}
