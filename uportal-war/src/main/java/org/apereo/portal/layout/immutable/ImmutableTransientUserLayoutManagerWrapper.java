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
package org.apereo.portal.layout.immutable;

import javax.xml.stream.XMLEventReader;
import org.apereo.portal.PortalException;
import org.apereo.portal.layout.IUserLayoutManager;
import org.apereo.portal.layout.TransientUserLayoutManagerWrapper;
import org.apereo.portal.layout.node.IUserLayoutNodeDescription;
import org.slf4j.Logger;

/**
 * Description
 *
 */
public class ImmutableTransientUserLayoutManagerWrapper extends TransientUserLayoutManagerWrapper {
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

    public ImmutableTransientUserLayoutManagerWrapper(IUserLayoutManager manager)
            throws PortalException {
        super(manager);
    }

    @Override
    public XMLEventReader getUserLayoutReader() {
        final XMLEventReader userLayoutReader = super.getUserLayoutReader();
        return new ImmutableUserLayoutXMLEventReader(userLayoutReader);
    }

    /**
     * Ignore attempts to save the layout.
     *
     * @throws PortalException
     */
    @Override
    public void saveUserLayout() throws PortalException {}

    @Override
    public boolean canAddNode(
            IUserLayoutNodeDescription node, String parentId, String nextSiblingId)
            throws PortalException {
        return false;
    }

    @Override
    public boolean canMoveNode(String nodeId, String parentId, String nextSiblingId)
            throws PortalException {
        return false;
    }

    @Override
    public boolean canDeleteNode(String nodeId) throws PortalException {
        return false;
    }

    @Override
    public boolean canUpdateNode(IUserLayoutNodeDescription node) throws PortalException {
        return false;
    }

    @Override
    public IUserLayoutNodeDescription addNode(
            IUserLayoutNodeDescription node, String parentId, String nextSiblingId)
            throws PortalException {
        throw new PortalException("Cannot add a node to an ImmutableTransientLayout");
    }

    @Override
    public boolean moveNode(String nodeId, String parentId, String nextSiblingId)
            throws PortalException {
        return false;
    }

    @Override
    public boolean deleteNode(String nodeId) throws PortalException {
        return false;
    }

    @Override
    public boolean updateNode(IUserLayoutNodeDescription node) throws PortalException {
        return false;
    }
}
