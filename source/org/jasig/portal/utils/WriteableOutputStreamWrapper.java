/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.utils;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A simple wrapper to present {@link IWriteable} interface for a regular <code>java.io.OutputStream</code>.
 *
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version $Revision$
 */
public class WriteableOutputStreamWrapper implements IWriteable {
    protected final OutputStream out;
    public WriteableOutputStreamWrapper(OutputStream out) {
        this.out=out;
    }

    public void write(int i) throws IOException {
        out.write(i);
    }

    public void flush() throws IOException {
        out.flush();
    }

    public void close() throws IOException {
        out.close();
    }

    public void write(int[] ibuf, int off, int len) throws IOException{
      byte[] bbuf = new byte[len-off];
      int j = 0;
      for (int i=off; i< len;i++){
        bbuf[j++]=(byte)ibuf[i];
      }
      if (j > 0){
        out.write(bbuf);
      }
    }

    public void write(int[] ibuf) throws IOException{
      write(ibuf,0,ibuf.length);
    }
}
