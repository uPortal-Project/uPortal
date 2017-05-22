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
package org.apereo.portal.layout.dlm;

import org.w3c.dom.Element;

/**
 * Simple tuple that holds onto metadata about a layout node while the Composite Layout is being
 * calculated.
 *
 * @since 2.5
 */
public class NodeInfo {

    private final Element node;
    private final String id;
    private final Precedence precedence;
    private int indexInCVP = -1; // CVP = Composite View Parent
    private Element positionDirective = null;

    NodeInfo(Element node) {
        this.node = node;
        precedence = Precedence.newInstance(node.getAttribute(Constants.ATT_FRAGMENT));
        id = node.getAttribute(Constants.ATT_ID);
    }

    NodeInfo(Element node, int indexInCVP) {
        this(node);
        this.indexInCVP = indexInCVP;
    }

    public int getIndexInCVP() {
        return indexInCVP;
    }

    public Element getPositionDirective() {
        return positionDirective;
    }

    public void setPositionDirective(Element positionDirective) {
        this.positionDirective = positionDirective;
    }

    public Element getNode() {
        return node;
    }

    public String getId() {
        return id;
    }

    public Precedence getPrecedence() {
        return precedence;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        NodeInfo other = (NodeInfo) obj;
        if (id == null) {
            if (other.id != null) return false;
        } else if (!id.equals(other.id)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "NodeInfo [node="
                + node
                + ", id="
                + id
                + ", precedence="
                + precedence
                + ", indexInCVP="
                + indexInCVP
                + ", positionDirective="
                + positionDirective
                + "]";
    }
}
