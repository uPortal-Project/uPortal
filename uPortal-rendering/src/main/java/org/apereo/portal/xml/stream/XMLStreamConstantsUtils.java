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
package org.apereo.portal.xml.stream;

import javax.xml.stream.XMLStreamConstants;

/**
 */
public class XMLStreamConstantsUtils {
    /** Get the human readable event name for the numeric event id */
    public static String getEventName(int eventId) {
        switch (eventId) {
            case XMLStreamConstants.START_ELEMENT:
                return "StartElementEvent";
            case XMLStreamConstants.END_ELEMENT:
                return "EndElementEvent";
            case XMLStreamConstants.PROCESSING_INSTRUCTION:
                return "ProcessingInstructionEvent";
            case XMLStreamConstants.CHARACTERS:
                return "CharacterEvent";
            case XMLStreamConstants.COMMENT:
                return "CommentEvent";
            case XMLStreamConstants.START_DOCUMENT:
                return "StartDocumentEvent";
            case XMLStreamConstants.END_DOCUMENT:
                return "EndDocumentEvent";
            case XMLStreamConstants.ENTITY_REFERENCE:
                return "EntityReferenceEvent";
            case XMLStreamConstants.ATTRIBUTE:
                return "AttributeBase";
            case XMLStreamConstants.DTD:
                return "DTDEvent";
            case XMLStreamConstants.CDATA:
                return "CDATA";
        }
        return "UNKNOWN_EVENT_TYPE";
    }
}
