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

package org.jasig.portal.dao.usertype;

import javax.xml.namespace.QName;

import org.jadira.usertype.spi.shared.AbstractStringColumnMapper;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class QNameColumnMapper extends AbstractStringColumnMapper<QName> {
    private static final long serialVersionUID = 1L;

    @Override
    public QName fromNonNullValue(String s) {
        return QName.valueOf(s);
    }

    @Override
    public String toNonNullValue(QName value) {
        return value.toString();
    }
}
