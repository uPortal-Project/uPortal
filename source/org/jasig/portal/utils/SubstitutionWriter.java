/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.utils;

import java.io.IOException;
import java.io.Writer;


/**
 * A filter presenting a <code>Writer</code> that performs
 * word substitution (search and replace) on the fly.
 *
 * 7/25/05 - UP-1180 - dmindler@rutgers.edu
 * Modified to make use of optimized SubstitutionIntegerFilter
 *
 * @author Peter Kharchenko  {@link <a href="mailto:pkharchenko@interactivebusiness.com"">pkharchenko@interactivebusiness.com"</a>}
 * @version $Revision$
 */
public class SubstitutionWriter extends Writer {
    SubstitutionIntegerFilter filter;

    /**
     * Creates a new <code>SubstitutionWriter</code> instance.
     *
     * @param out a true <code>Writer</code> value where processed stream should be directed
     * @param target a <code>byte[]</code> value of a target to be replaced
     * @param substitute a <code>byte[]</code> value with which the target will be replaced
     * @param bufferSize a size of the buffer
     */
    public SubstitutionWriter(Writer out, char[] target, char[] substitute, int bufferSize) {
        filter=new SubstitutionIntegerFilter(out,target,substitute,bufferSize);
    }

    /**
     * Creates a new <code>SubstitutionWriter</code> instance.
     *
     * @param out a true <code>Writer</code> value where processed stream should be directed
     * @param target a <code>byte[]</code> value of a target to be replaced
     * @param substitute a <code>byte[]</code> value with which the target will be replaced
     */
    public SubstitutionWriter(Writer out, char[] target, char[] substitute) {
        filter=new SubstitutionIntegerFilter(out,target,substitute);
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

    public void write(char[] cbuf, int off, int len) throws IOException {
        // check boundaries
        if(off+len>cbuf.length) throw new IOException("Invalid offsent or length specified");

        for(int i=0;i<len;i++) {
            filter.write(cbuf[i]);
        }
    }

    /**
     * A helper method to convert char array to int array.
     * I am sure there's a way to cast it correctly, but I don't want to take my chances :)
     * @param c a <code>char[]</code> value
     * @return an <code>int[]</code> value
     */
    private static int[] getIntArrayFromCharArray(char[] c) {
        int[] ic=new int[c.length];
        for(int i=0;i<c.length;i++) {
            ic[i]=(int)c[i];
        }
        return ic;
    }

    /**
     * A test self-test method for the class.
     *
     */
    public static void main(String[] args) {
        // construct a string
        String inputString="Marry had a little lamb, little lamb, little lamb.";

        // set out the sink
        java.io.StringWriter sw=new java.io.StringWriter();
        SubstitutionWriter substw=new SubstitutionWriter(sw,(new String("lamb")).toCharArray(),(new String("rump")).toCharArray());
        try {
            substw.write(inputString);
            substw.flush();
            String resultString=sw.toString();
            if(resultString.equals("Marry had a little rump, little rump, little rump.")) {
               System.out.println("Test passed.");
            } else {
               System.out.println("Test failed!");
            }
        } catch (Exception e) {
            System.out.println("Test failed:");
            e.printStackTrace();
        }

    }

}
