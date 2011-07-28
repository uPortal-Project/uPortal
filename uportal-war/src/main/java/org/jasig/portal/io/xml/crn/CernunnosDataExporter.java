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

import org.danann.cernunnos.ReturnValueImpl;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.TaskResponse;
import org.danann.cernunnos.runtime.RuntimeRequestResponse;
import org.dom4j.Element;
import org.jasig.portal.utils.SafeFilenameUtils;
import org.jasig.portal.utils.Tuple;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.base.Function;

/**
 * Generic export impl that support Cernunnous Tasks.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class CernunnosDataExporter extends AbstractDom4jExporter {
    private Task task;
    private String idAttributeName;
    private Function<Tuple<String, Element>, String> fileNameFunction;
    
    @Required
    public void setIdAttributeName(String idAttributeName) {
        this.idAttributeName = idAttributeName;
    }

    /**
     * The CRN task to execute to perform the export
     */
    @Required
    public void setTask(Task task) {
        this.task = task;
    }
    
    /**
     * An optional function that resolves the filename for each exported node. If not specified the default data id used.
     */
    public void setFileNameFunction(Function<Tuple<String, Element>, String> fileNameFunction) {
        this.fileNameFunction = fileNameFunction;
    }

    @Override
    protected Element exportDataElement(String id) {
        final RuntimeRequestResponse request = new RuntimeRequestResponse();
        request.setAttribute(this.idAttributeName, id);

        final ReturnValueImpl result = new ReturnValueImpl();
        final TaskResponse response = new RuntimeRequestResponse(
                Collections.<String, Object> singletonMap("Attributes.RETURN_VALUE", result));
        
        task.perform(request, response);
        
        return (Element)result.getValue();
    }
    

    @Override
    public String getFileName(Tuple<String, Element> data) {
        final String fileName;
        if (this.fileNameFunction == null) {
            fileName = data.first;
        }
        else {
            fileName = this.fileNameFunction.apply(data);
        }
        
        return SafeFilenameUtils.makeSafeFilename(fileName);
    }
}
