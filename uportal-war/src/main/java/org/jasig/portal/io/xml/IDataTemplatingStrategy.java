/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.io.xml;

import javax.xml.transform.Source;

import org.dom4j.Document;

/**
 * This interface brings templating to data import, allowing entity XML
 * documents to contain tokens that are replaced with real values at runtime
 * before importing.
 * 
 * @since 4.2
 * @author drewwills
 */
public interface IDataTemplatingStrategy {

    /**
     * Apply templating to the specified document.  Returns a valid
     * Source object, ready for calling IPortalDataHandlerService.importData().
     */
    Source processTemplates(Document data, String filename);

}
