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

import org.w3c.dom.Element;

/**
 * This is a temp solution for top-level <layout> node element.
 * Eventually, we would like the top-level element to become a full-blown folder,
 * but this requires many changes all over the code and XSLT :(
 *
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version 1.0
 */
public class UserLayoutRootDescription extends UserLayoutFolderDescription {

    public UserLayoutRootDescription(Element xmlNode) {
       this();
    }


    public UserLayoutRootDescription() {
        this.setImmutable(false);
        this.setUnremovable(false);
        /*
        // nothign to do really .. we can try reading immutable/unremovable since they might be added at some point
        Boolean unValue=new Boolean(xmlNode.getAttribute("unremovable"));
        Boolean imValue=new Boolean(xmlNode.getAttribute("immutable"));
        if(unValue!=null){
            this.setUnremovable(unValue.booleanValue());
        }
        if(imValue!=null){
            this.setImmutable(imValue.booleanValue());
        } else {

        }
        */
    }

    public String getId() {
        return UserLayoutNodeDescription.ROOT_FOLDER_ID;
    }
    public void setId(String id) {};
}
