/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
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




/**
 * Provides information about encodings. Depends on the Java runtime
 * to provides writers for the different encodings, but can be used
 * to override encoding names and provide the last printable character
 * for each encoding.
 *
 * @version $Id$
 * @author <a href="mailto:arkin@intalio.com">Assaf Arkin</a>
 */
public class Encodings
{


    /**
     * The last printable character for unknown encodings.
     */
    static final int DefaultLastPrintable = 0x7F;

    /**
     * @param encoding a MIME charset name, or null.
     */
    static EncodingInfo getEncodingInfo(String encoding) {
        if (encoding == null)
            return new EncodingInfo(null, DefaultLastPrintable);
        for (int i = 0;  i < _encodings.length;  i++) {
            if (_encodings[i].name.equalsIgnoreCase(encoding))
                return _encodings[i];
        }
        return new SieveEncodingInfo(encoding, DefaultLastPrintable);
    }

    static final String JIS_DANGER_CHARS
    = "\\\u007e\u007f\u00a2\u00a3\u00a5\u00ac"
    +"\u2014\u2015\u2016\u2026\u203e\u203e\u2225\u222f\u301c"
    +"\uff3c\uff5e\uffe0\uffe1\uffe2\uffe3";

    /**
     * Constructs a list of all the supported encodings.
     */
    private static final EncodingInfo[] _encodings = new EncodingInfo[] {
        new EncodingInfo("ASCII", 0x7F),
        new EncodingInfo("US-ASCII", 0x7F),
        new EncodingInfo("ISO-8859-1", 0xFF),
        new EncodingInfo("ISO-8859-2", 0xFF),
        new EncodingInfo("ISO-8859-3", 0xFF),
        new EncodingInfo("ISO-8859-4", 0xFF),
        new EncodingInfo("ISO-8859-5", 0xFF),
        new EncodingInfo("ISO-8859-6", 0xFF),
        new EncodingInfo("ISO-8859-7", 0xFF),
        new EncodingInfo("ISO-8859-8", 0xFF),
        new EncodingInfo("ISO-8859-9", 0xFF),
        /**
         * Does JDK's converter supprt surrogates?
         * A Java encoding name "UTF-8" is suppoted by JDK 1.2 or later.
         */
        new EncodingInfo("UTF-8", "UTF8", 0x10FFFF),
        /**
         * JDK 1.1 supports "Shift_JIS" as an alias of "SJIS".
         * But JDK 1.2 treats "Shift_JIS" as an alias of "MS932".
         * The JDK 1.2's behavior is invalid against IANA registrations.
         */
        new SieveEncodingInfo("Shift_JIS", "SJIS", 0x7F, JIS_DANGER_CHARS),
        /**
         * "MS932" is supported by JDK 1.2 or later.
         */
        new SieveEncodingInfo("Windows-31J", "MS932", 0x7F, JIS_DANGER_CHARS),
        new SieveEncodingInfo("EUC-JP", null, 0x7F, JIS_DANGER_CHARS),
        new SieveEncodingInfo("ISO-2022-JP", null, 0x7F, JIS_DANGER_CHARS),
    };
}
