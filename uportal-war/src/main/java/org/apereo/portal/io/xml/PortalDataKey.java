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
package org.apereo.portal.io.xml;

import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import org.apache.commons.lang.Validate;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Describes the type and version of a portal data XML file.
 *
 */
public class PortalDataKey {
    /**
     * The XML Attribute on the root element that contains the cernunnos script that denotes the
     * file version. Used for data files from 3.2 and earlier.
     */
    public static final QName SCRIPT_ATTRIBUTE_NAME = new QName("script");
    /** The version of the data file, used for data files form 4.0 and later. */
    public static final QName VERSION_ATTRIBUTE_NAME = new QName("version");

    /**
     * {@link #hashCode()} is called A LOT but never changes since this object and all field types
     * are immutable. A local variable is used to cache the calculated hash code
     */
    private int hash = 0;

    private final QName name;
    private final String script;
    private final String version;

    public PortalDataKey(Node rootElement) {
        if (rootElement.getNodeType() == Node.DOCUMENT_NODE) {
            rootElement = ((Document) rootElement).getDocumentElement();
        }

        final String nodeName = rootElement.getNodeName();
        final String namespaceURI = rootElement.getNamespaceURI();

        if (namespaceURI != null) {
            this.name = new QName(namespaceURI, nodeName);
        } else {
            this.name = new QName(nodeName);
        }

        final NamedNodeMap attributes = rootElement.getAttributes();
        if (attributes != null) {
            final Node scriptAttr = attributes.getNamedItem(SCRIPT_ATTRIBUTE_NAME.getLocalPart());
            if (scriptAttr != null) {
                this.script = scriptAttr.getTextContent();
            } else {
                this.script = null;
            }

            final Node versionAttr = attributes.getNamedItem(VERSION_ATTRIBUTE_NAME.getLocalPart());
            if (versionAttr != null) {
                this.version = versionAttr.getTextContent();
            } else {
                this.version = null;
            }
        } else {
            this.script = null;
            this.version = null;
        }
    }

    public PortalDataKey(StartElement startElement) {
        this.name = startElement.getName();
        this.script = getAttributeValue(startElement, SCRIPT_ATTRIBUTE_NAME);
        this.version = getAttributeValue(startElement, VERSION_ATTRIBUTE_NAME);
    }

    public PortalDataKey(QName name, String script, String version) {
        Validate.notNull(name);
        this.name = name;
        this.script = script;
        this.version = version;
    }

    protected String getAttributeValue(StartElement startElement, QName name) {
        final Attribute versionAttr = startElement.getAttributeByName(name);
        if (versionAttr != null) {
            return versionAttr.getValue();
        }

        return null;
    }

    public QName getName() {
        return this.name;
    }

    public String getScript() {
        return this.script;
    }

    public String getVersion() {
        return this.version;
    }

    @Override
    public int hashCode() {
        final int lHash = this.hash;
        if (lHash == 0) {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
            result = prime * result + ((this.script == null) ? 0 : this.script.hashCode());
            result = prime * result + ((this.version == null) ? 0 : this.version.hashCode());
            this.hash = result;
            return result;
        }
        return lHash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        PortalDataKey other = (PortalDataKey) obj;
        if (this.name == null) {
            if (other.name != null) return false;
        } else if (!this.name.equals(other.name)) return false;
        if (this.script == null) {
            if (other.script != null) return false;
        } else if (!this.script.equals(other.script)) return false;
        if (this.version == null) {
            if (other.version != null) return false;
        } else if (!this.version.equals(other.version)) return false;
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder("<");
        builder.append(this.name);
        if (this.script != null) {
            builder.append(" script=\"").append(this.script).append("\"");
        }
        if (this.version != null) {
            builder.append(" version=\"").append(this.version).append("\"");
        }
        builder.append(">");

        return builder.toString();
    }
}
