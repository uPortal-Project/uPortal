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

import java.io.IOException;
import java.io.Writer;

/**
 * Performs substitution operation on a stream of integer write requests.
 *
 * 7/25/05 - UP-1180 - dmindler@rutgers.edu
 * Modified to utilize characters instead of integers. Main reason is
 * that actual writing was delegated to the WriteableWriterWrapper class,
 * who had a statement:
 * <code>
 *     cbuf[j++]=(char)ibuf[i];
 * </code>
 * in effect, converting an integer to a character. This was an expensive
 * transformation since all data was copied into an int[] then a char[] was
 * allocated in the WriteableWriterWrapper class to which all data was 
 * copied (as shown above):
 * <code>
 *     char[] cbuf = new char[len-off];
 * </code>
 * Note: This class name was not changed.
 * 
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version $Revision$
 */
public class SubstitutionIntegerFilter {
    private final static int DEFAULT_BUFFER_SIZE=2048;
    final Writer out;

    final char[] substitute;
    final char[] target;
    private int matchindex;
    private char[] buffer;
    private int bufferindex;
    private int maxBuffer = DEFAULT_BUFFER_SIZE;

    /**
     * Creates a new <code>SubstitutionIntegerFilter</code> instance.
     *
     * @param out an <code>IWriteable</code> object value
     * @param target an <code>int[]</code> pattern to be replaced
     * @param substitute an <code>int[]</code> pattern to replace the original
     */
    public SubstitutionIntegerFilter(Writer out, char[] target, char[] substitute) {
        this.out=out;
        this.substitute=substitute;
        this.target=target;
        this.matchindex=0;
        this.bufferindex=0;
        this.buffer=new char[maxBuffer + target.length];
    }

    /**
     * Creates a new <code>SubstitutionIntegerFilter</code> instance.
     *
     * @param out an <code>IWriteable</code> object value
     * @param target an <code>int[]</code> pattern to be replaced
     * @param substitute an <code>int[]</code> pattern to replace the original
     * @param bufferSize a buffer size
     */
    public SubstitutionIntegerFilter(Writer out, char[] target, char[] substitute,int bufferSize) {
        this.out=out;
        this.substitute=substitute;
        this.target=target;
        this.matchindex=0;
        this.bufferindex=0;
        this.maxBuffer=bufferSize-target.length;
        this.buffer=new char[maxBuffer + target.length];
    }

    public void write(char number) throws IOException {
        if(number==target[matchindex]) {
            if(matchindex<target.length-1) {
                // assume match will fail, but track buffered ints
                addToBuffer(number);
                matchindex++;
            } else {
                // we have a match, roll back buffer and add substitute
                bufferindex = bufferindex - matchindex;
                matchindex=0;
                for (int x =0; x<substitute.length;x++){
                  addToBuffer(substitute[x]);
                }
            }
        } else {
            matchindex=0;
            addToBuffer(number);
        }
    }

    public void flush() throws IOException {
        // do internal flush
        out.write(buffer,0,bufferindex);
        bufferindex=0;
        out.flush();
    }

    public void close() throws IOException {
        this.flush();
        out.close();
    }

    protected void addToBuffer(char i) throws IOException{
      // flush if buffer fills up, but only if we're not tracking a possible substitution
        if ((bufferindex > (maxBuffer-2)) && matchindex==0){
          flush();
        }
        buffer[bufferindex++] = i;
    }

}

