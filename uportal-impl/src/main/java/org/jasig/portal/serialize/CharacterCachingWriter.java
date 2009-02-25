/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
