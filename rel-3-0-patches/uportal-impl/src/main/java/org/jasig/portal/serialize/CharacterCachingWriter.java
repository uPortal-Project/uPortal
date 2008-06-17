/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.serialize;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public interface CharacterCachingWriter {

    public boolean startCaching() throws IOException;
    public boolean stopCaching();
    public String getCache(String encoding) throws UnsupportedEncodingException, IOException;
    public void flush() throws IOException;

}
