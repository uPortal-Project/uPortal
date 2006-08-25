/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.utils;


import java.io.IOException;


/**
 * An interface for common methods in <code>Writer</code>s and <code>OutputStream</code>s.
 *
 * @author Peter Kharchenko  {@link <a href="mailto:pkharchenko@interactivebusiness.com"">pkharchenko@interactivebusiness.com"</a>}
 * @version $Revision$
 */
public interface IWriteable {

    public void write(int i) throws IOException;
    public void flush() throws IOException;
    public void close() throws IOException;
    public void write(int[] iBuf, int off, int len) throws IOException;
    public void write(int[] iBuf) throws IOException;
}
