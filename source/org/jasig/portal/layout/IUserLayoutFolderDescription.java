/**
 * Copyright © 2002 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.layout;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * An interface describing a folder user layout node.
 *
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version 1.0
 */
public interface IUserLayoutFolderDescription extends IUserLayoutNodeDescription {

    public static final int REGULAR_TYPE=0;
    public static final int HEADER_TYPE=1;
    public static final int FOOTER_TYPE=2;

    public static final String[] folderTypeNames= {"regular","header","footer"};

    /**
     * Returns folder type.
     *
     * @return an <code>int</code> value corresponding
     * to one of the valid folder types.
     */
    public int getFolderType();

    /**
     * Assign a type to a folder.
     *
     * @param folderType an <code>int</code> value corresponding
     * to one of the valid folder types.
     */
    public void setFolderType(int folderType);

    /**
     * Creates a <code>org.w3c.dom.Element</code> representation of the current node.
     *
     * @param root a <code>Document</code> for which the <code>Element</code> should be created.
     * @return a <code>Node</code> value
     */
    public Element getXML(Document root);

}
