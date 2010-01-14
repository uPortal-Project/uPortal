/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
        
        safeName = safeFileNamePhrase.getSafeFileName("你好藍色.channel");
        assertEquals("你好藍色.channel", safeName);
        
        safeName = safeFileNamePhrase.getSafeFileName("This is my 3rd.test\\string/lets*se-how It goes");
        assertEquals("This_is_my_3rd.test.string.lets_se-how_It_goes", safeName);
    }
}
