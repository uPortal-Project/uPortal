/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.io;

import junit.framework.TestCase;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class SafeFileNamePhraseTest extends TestCase {
    public void testGetSafeFileName()  {
        SafeFileNamePhrase safeFileNamePhrase = new SafeFileNamePhrase();
        
        String safeName;
        
        safeName = safeFileNamePhrase.getSafeFileName("fileName.channel");
        assertEquals("fileName.channel", safeName);
        
        safeName = safeFileNamePhrase.getSafeFileName("file Name.channel");
        assertEquals("file_Name.channel", safeName);
        
        safeName = safeFileNamePhrase.getSafeFileName("file/Name.channel");
        assertEquals("file.Name.channel", safeName);
        
        safeName = safeFileNamePhrase.getSafeFileName("file\\Name.channel");
        assertEquals("file.Name.channel", safeName);
        
        safeName = safeFileNamePhrase.getSafeFileName("file-Name.channel");
        assertEquals("file-Name.channel", safeName);
        
        safeName = safeFileNamePhrase.getSafeFileName("file*Name.channel");
        assertEquals("file_Name.channel", safeName);
        
        safeName = safeFileNamePhrase.getSafeFileName("This is my 3rd.test\\string/lets*se-how It goes");
        assertEquals("This_is_my_3rd.test.string.lets_se-how_It_goes", safeName);
    }
}
