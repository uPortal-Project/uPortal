/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.utils;

import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.servlet.ServletOutputStream;


/**
 * A filter presenting a <code>ServletOutputStream</code> that performs
 * word substitution (search&replace) on the fly.
 *
 * 7/25/05 - UP-1180 - dmindler@rutgers.edu
 * Modified to make use of optimized SubstitutionIntegerFilter
 * 
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version $Revision$
 */
public class SubstitutionServletOutputStream extends ServletOutputStream {
    SubstitutionIntegerFilter filter;

    /**
     * Creates a new <code>SubstitutionServletOutputStream</code> instance.
     *
     * @param out a true <code>ServletOutputStream</code> value where processed stream should be directed
     * @param target a <code>byte[]</code> value of a target to be replaced
     * @param substitute a <code>byte[]</code> value with which the target will be replaced
     */
    public SubstitutionServletOutputStream(ServletOutputStream out, byte[] target, byte[] substitute) {
        filter=new SubstitutionIntegerFilter(new OutputStreamWriter(out),getCharArrayFromByteArray(target),getCharArrayFromByteArray(substitute));
    }

    /**
     * Creates a new <code>SubstitutionServletOutputStream</code> instance.
     *
     * @param out a true <code>ServletOutputStream</code> value where processed stream should be directed
     * @param target a <code>byte[]</code> value of a target to be replaced
     * @param substitute a <code>byte[]</code> value with which the target will be replaced
     * @param bufferSize a buffer size
     */
    public SubstitutionServletOutputStream(ServletOutputStream out, byte[] target, byte[] substitute, int bufferSize) {
        filter=new SubstitutionIntegerFilter(new OutputStreamWriter(out),getCharArrayFromByteArray(target),getCharArrayFromByteArray(substitute),bufferSize);
    }

    public void write(int i) throws IOException {
        filter.write((char) i);
    }

    public void flush() throws IOException {
        filter.flush();
    }
    public void close() throws IOException {
        filter.close();
    }

    /**
     * A helper method to convert byte array to int array.
     * I am sure there's a way to cast it correctly, but I don't want to take my chances :)
     * @param c a <code>byte[]</code> value
     * @return an <code>int[]</code> value
     */
    private static char[] getCharArrayFromByteArray(byte[] c) {
        char[] ic=new char[c.length];
        for(int i=0;i<c.length;i++) {
            ic[i]=(char)c[i];
        }
        return ic;
    }

}
