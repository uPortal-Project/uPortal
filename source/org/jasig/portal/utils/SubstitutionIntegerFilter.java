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

package org.jasig.portal.utils;

import java.io.OutputStream;
import java.io.Writer;
import java.io.IOException;

/**
 * Performs substitution operation on a stream of integer write requests.
 *
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version: $Revision$
 */
public class SubstitutionIntegerFilter {
    final IWriteable out;

    final int[] substitute;
    final int[] target;
    private int index;
    private int[] accumulated;

    /**
     * Creates a new <code>SubstitutionIntegerFilter</code> instance.
     *
     * @param out an <code>IWriteable</code> object value
     * @param target an <code>int[]</code> pattern to be replaced
     * @param substitute an <code>int[]</code> pattern to replace the original
     */
    public SubstitutionIntegerFilter(IWriteable out, int[] target, int[] substitute) {
        this.out=out;
        this.substitute=substitute;
        this.target=target;
        this.index=0;
        this.accumulated=new int[target.length];
    }

    public void write(int number) throws IOException {
        if(number==target[index]) {
            if(index<target.length-1) {
                // continue to match the pattern
                accumulated[index]=number;
                index++;
            } else {
                // substitute
                for(int j=0;j<substitute.length;j++) {
                    out.write(substitute[j]);
                }
                index=0;
            }
        } else {
            // clear out the buffer
            for(int j=0;j<index;j++) {
                out.write(accumulated[j]);
            }
            index=0;
            out.write(number);
        }
    }

    public void flush() throws IOException {
        // do internal flush
        for(int j=0;j<index;j++) {
            out.write(accumulated[j]);
        }
        index=0;
        out.flush();
    }

    public void close() throws IOException {
        this.flush();
        out.close();
    }

}

