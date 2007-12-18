/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
 * @author Peter Kharchenko  {@link <a href="mailto:pkharchenko@interactivebusiness.com"">pkharchenko@interactivebusiness.com"</a>}
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
    private int maxBufferSubstitute = maxBuffer - 2;

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
        this.maxBufferSubstitute = this.maxBuffer - 2;
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

	// Optimized; perform filter against given array
	public void write(final char[] ca, final int off, final int len) throws IOException {
		final int tlen = target.length - 1;

		for (int i = off; i < len; i++) {
			final char c = ca[i];
			if (c == target[matchindex]) {
				if (matchindex < tlen) {
					matchindex++;
				} else {
					// we have a match, roll back buffer and add substitute
					bufferindex = bufferindex - matchindex;
					matchindex = 0;
					for (int x =0; x<substitute.length;x++){
						if ((bufferindex > (maxBufferSubstitute)) && matchindex == 0) {
							flush();
						}
						buffer[bufferindex++] = substitute[x];
					}
					continue;
				}
			} else {
				matchindex=0;
			}

			if ((bufferindex > maxBufferSubstitute) && matchindex == 0) {
				flush();
			}

			buffer[bufferindex++] = c;
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
    	if ((bufferindex > maxBufferSubstitute) && matchindex==0) {
          flush();
        }
        buffer[bufferindex++] = i;
    }

}

