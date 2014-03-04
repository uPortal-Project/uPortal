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
package org.jasig.portal.layout;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Returns the subscribe ID of a channel having the passed in functional name or null if it can't find such a channel in
 * the layout.
 */
public class PortletSubscribeIdResolver implements INodeIdResolver {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

	private final String fname;

	/**
	 * @param fname Functional name of portlet whose subscribe id to search for.
	 */
	public PortletSubscribeIdResolver(String fname) {
		this.fname = fname;
	}

	@Override
	public String traverseDocument(Document document) {
		final NodeList channels = document.getElementsByTagName("channel");
		for (int i = 0; i < channels.getLength(); i++) {
			final Element e = (Element) channels.item(i);
			if (fname.equals(e.getAttribute("fname"))) {
				String ID = e.getAttribute("ID");

                if (StringUtils.isEmpty(ID)) {
                    logger.warn("Found channel element with @fname {} it had an empty ID attribute {} (in {}).",
                            fname, ID, document);
                    return null;
                }

                logger.trace("Found channel element ID {} matching fname {}.",
                        ID, fname);

				return ID;
			}
		}

        logger.trace("Did not find a channel element with fname {} in {}.",
                fname, document);
		return null;
	}

    public String toString() {
        return new ToStringBuilder(this).
                append("fname", fname).
                toString();
    }

}
