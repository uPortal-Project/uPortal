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


/*
 * An implementation of DEC UUID recommendation.
 */

public class UUID extends PortalID {
    private static SecureRandom sr=new SecureRandom();

    public UUID(byte[] id) {
        super(id);
    }

    public UUID(String id) {
        // rip out the '-' chars
        String clean=id.substring(0,8)+id.substring(9,13)+id.substring(14,18)+id.substring(19,23)+id.substring(24,36);
        this.id=hexStringToByteArr(clean);
    }

    public String toString() {
        String time_low=byteArrToHexString(id,0,4);
        String time_mid=byteArrToHexString(id,4,2);
        String version_time_high=byteArrToHexString(id,6,2);
        String variant_clock_seq=byteArrToHexString(id,8,2);
        String node=byteArrToHexString(id,10,6);
        return new String(time_low+"-"+time_mid+"-"+version_time_high+"-"+variant_clock_seq+"-"+node);
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

        return new UUID(bytes);
    };

    public static void main(String args[]) {
        int times=Integer.parseInt(args[0]);

        /*	Byte.parseByte("10",10);
        System.out.println(".");
        Byte.parseByte("9f",16);
        System.out.println("."); */

        for(int i=0;i<times;i++) {
            UUID id1=UUID.generateUUID();
            System.out.println(id1.toString());
            UUID id2=new UUID(id1.toString());
            System.out.println(id2.toString());
        }
    }
}
