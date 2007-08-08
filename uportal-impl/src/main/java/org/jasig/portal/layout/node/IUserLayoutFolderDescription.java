/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.node;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * An interface describing a folder user layout node.
 *
 * @author Peter Kharchenko  {@link <a href="mailto:pkharchenko@interactivebusiness.com"">pkharchenko@interactivebusiness.com"</a>}
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
