/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.utils;

import java.io.IOException;

/**
 * Performs substitution operation on a stream of integer write requests.
 *
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version $Revision$
 */
public class SubstitutionIntegerFilter {
    private final static int DEFAULT_BUFFER_SIZE=2048;
    final IWriteable out;

    final int[] substitute;
    final int[] target;
    private int matchindex;
    private int[] buffer;
    private int bufferindex;
    private int maxBuffer = DEFAULT_BUFFER_SIZE;

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
        this.matchindex=0;
        this.bufferindex=0;
        this.buffer=new int[maxBuffer + target.length];
    }

    /**
     * Creates a new <code>SubstitutionIntegerFilter</code> instance.
     *
     * @param out an <code>IWriteable</code> object value
     * @param target an <code>int[]</code> pattern to be replaced
     * @param substitute an <code>int[]</code> pattern to replace the original
     * @param bufferSize a buffer size
     */
    public SubstitutionIntegerFilter(IWriteable out, int[] target, int[] substitute,int bufferSize) {
        this.out=out;
        this.substitute=substitute;
        this.target=target;
        this.matchindex=0;
        this.bufferindex=0;
        this.maxBuffer=bufferSize-target.length;
        this.buffer=new int[maxBuffer + target.length];
    }

    public void write(int number) throws IOException {
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

    protected void addToBuffer(int i) throws IOException{
      // flush if buffer fills up, but only if we're not tracking a possible substitution
        if ((bufferindex > (maxBuffer-2)) && matchindex==0){
          flush();
        }
        buffer[bufferindex++] = i;
    }

}

