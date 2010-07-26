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
