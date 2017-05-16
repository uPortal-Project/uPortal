/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.layout;

import org.w3c.dom.Document;

/**
 * Implementations of this interface should be used when searching for node identifier. For example,
 * it might be tab identifier lookup based on channel ID or channel ID lookup by its functional
 * name. Implementations are used when calling {@link IUserLayout#findNodeId(NodeIdFinder)}. The
 * same behavior can be achieved using XPath expressions (and vice versa), but traversing DOM
 * "manually" is much faster in some situations. Note that with simple XPath expressions it might
 * not
 *
 */
public interface INodeIdResolver {

    /**
     * Traverse the document in order to find the required node identifier.
     *
     * @param document User layout document.
     * @return The ID of the resolved node, null if there is no match
     */
    public String traverseDocument(Document document);
}
