/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.utils;

import java.io.IOException;
import java.io.OutputStream;


/**
 * Very simple OutputStream implementation that does nothing
 * with the data written to it (the famous /dev/null)
 * 
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$
 */
public class NullOutputStream extends OutputStream {
    /**
     * @see java.io.OutputStream#write(int)
     */
    public void write(int b) throws IOException {
        
    }
}
