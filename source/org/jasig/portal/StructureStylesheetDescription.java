/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

 package org.jasig.portal;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Stylesheet description for stylesheets performing structure transformation
 * @author Peter Kharchenko
 * @version $Revision$
 */

public class StructureStylesheetDescription extends CoreXSLTStylesheetDescription {
    Hashtable folderAttributeTable;

    public StructureStylesheetDescription() {
        super();
        folderAttributeTable=new Hashtable();
    }

    public Enumeration getFolderAttributeNames() {
        return folderAttributeTable.keys();
    }
    public String getFolderAttributeWordDescription(String attributeName) {
        DescriptionDefaultValuePair pair=(DescriptionDefaultValuePair) folderAttributeTable.get(attributeName);
        if(pair!=null) return pair.wordDescription;
        else return null;
    }
    public String getFolderAttributeDefaultValue(String attributeName) {
        DescriptionDefaultValuePair pair=(DescriptionDefaultValuePair) folderAttributeTable.get(attributeName);
        if(pair!=null) return pair.defaultValue;
        else return null;
    }
    public void setFolderAttributeWordDescription(String attributeName,String wordDescription) {
        DescriptionDefaultValuePair pair=(DescriptionDefaultValuePair) folderAttributeTable.get(attributeName);
        if(pair!=null) pair.wordDescription=wordDescription;
    }
    public void setFolderAttributeDefaultValue(String attributeName,String defaultValue) {
        DescriptionDefaultValuePair pair=(DescriptionDefaultValuePair) folderAttributeTable.get(attributeName);
        if(pair!=null) pair.defaultValue=defaultValue;
    }

    public boolean containsFolderAttribute(String attributeName) {
        return folderAttributeTable.containsKey(attributeName);
    }

    public void addFolderAttribute(String name, String defaultValue, String wordDescription) {
        DescriptionDefaultValuePair pair=new DescriptionDefaultValuePair();
        pair.defaultValue=defaultValue; pair.wordDescription=wordDescription;
        folderAttributeTable.put(name,pair);
    }

}
