/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights 
 * reserved.
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
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:  
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xerces" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */


package org.jasig.portal.serialize;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * This class represents an encoding.
 *
 * @version $Id$
 */
public class SieveEncodingInfo extends EncodingInfo {

    BAOutputStream checkerStream = null;
    Writer checkerWriter = null;
    String dangerChars = null;

    /**
     * Creates new <code>SeiveEncodingInfo</code> instance.
     *
     * @param dangers A sorted characters that are always printed as character references.
     */
    public SieveEncodingInfo(String mimeName, String javaName,
                             int lastPrintable, String dangers) {
        super(mimeName, javaName, lastPrintable);
        this.dangerChars = dangers;
    }

    /**
     * Creates new <code>SeiveEncodingInfo</code> instance.
     */
    public SieveEncodingInfo(String mimeName, int lastPrintable) {
        this(mimeName, mimeName, lastPrintable, null);
    }

    /**
     * Checks whether the specified character is printable or not.
     *
     * @param ch a code point (0-0x10ffff)
     */
    public boolean isPrintable(int ch) {
        if (this.dangerChars != null && ch <= 0xffff) {
            /**
             * Searches this.dangerChars for ch.
             * TODO: Use binary search.
             */
            if (this.dangerChars.indexOf(ch) >= 0)
                return false;
        }

        if (ch <= this.lastPrintable)
            return true;

        boolean printable = true;
        synchronized (this) {
            try {
                if (this.checkerWriter == null) {
                    this.checkerStream = new BAOutputStream(10);
                    this.checkerWriter = new OutputStreamWriter(this.checkerStream, this.javaName);
                }

                if (ch > 0xffff) {
                    this.checkerWriter.write(((ch-0x10000)>>10)+0xd800);
                    this.checkerWriter.write(((ch-0x10000)&0x3ff)+0xdc00);
                    byte[] result = this.checkerStream.getBuffer();
                    if (this.checkerStream.size() == 2 && result[0] == '?' && result[1] == '?')
                        printable = false;
                } else {
                    this.checkerWriter.write(ch);
                    this.checkerWriter.flush();
                    byte[] result = this.checkerStream.getBuffer();
                    if (this.checkerStream.size() == 1 && result[0] == '?')
                        printable = false;
                }
                this.checkerStream.reset();
            } catch (IOException ioe) {
                printable = false;
            }
        }

        return printable;
    }

    /**
     * Why don't we use the original ByteArrayOutputStream?
     * - Because the toByteArray() method of the ByteArrayOutputStream
     * creates new byte[] instances for each call.
     */
    static class BAOutputStream extends ByteArrayOutputStream {
        BAOutputStream() {
            super();
        }

        BAOutputStream(int size) {
            super(size);
        }

        byte[] getBuffer() {
            return this.buf;
        }
    }

}
