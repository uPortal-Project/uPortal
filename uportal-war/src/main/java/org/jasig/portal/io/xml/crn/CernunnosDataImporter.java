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

package org.jasig.portal.io.xml.crn;

import java.util.Collections;

import org.apache.commons.lang.StringUtils;
import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.ReturnValueImpl;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.TaskResponse;
import org.danann.cernunnos.runtime.RuntimeRequestResponse;
import org.dom4j.Element;
import org.jasig.portal.utils.Tuple;

/**
 * Generic import impl that support Cernunnous Tasks.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class CernunnosDataImporter extends AbstractDom4jImporter {
    private Task task;

    public void setTask(Task task) {
        this.task = task;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.io.xml.crn.AbstractDom4jImporter#importDataNode(org.jasig.portal.utils.Tuple)
     */
    @Override
    protected void importDataElement(Tuple<String, Element> data) {
        final RuntimeRequestResponse request = new RuntimeRequestResponse();
        request.setAttribute(Attributes.NODE, data.second);
        request.setAttribute(Attributes.LOCATION, StringUtils.trimToEmpty(data.first));

        final ReturnValueImpl result = new ReturnValueImpl();
        final TaskResponse response = new RuntimeRequestResponse(
                Collections.<String, Object> singletonMap("Attributes.RETURN_VALUE", result));
        
        this.task.perform(request, response);
    }
}
