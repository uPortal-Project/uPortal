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

package org.jasig.portal.portlet.delegation.jsp;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Parameter tag, uses the {@link ParameterizableTag} as a parent to add parameters
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ParamTag extends TagSupport {
    private static final long serialVersionUID = 1L;

    private String name;
    private String value;

    /**
     * Processes the <CODE>param</CODE> tag.
     * @return <CODE>SKIP_BODY</CODE>
     */
    @Override
    public int doStartTag() throws JspException {
        final ParameterizableTag parameterizableTag = (ParameterizableTag) findAncestorWithClass(this, ParameterizableTag.class);

        if (parameterizableTag == null) {
            throw new JspException("the 'param' Tag must have a parent tag that implements ParameterizableTag");
        }

        if (this.name != null) {
            parameterizableTag.addParameter(this.name, this.value);
        }
        
        return SKIP_BODY;
    }

    /**
     * Returns the name.
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the value.
     * @return String
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the name.
     * @param name The name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the value.
     * @param value The value to set
     */
    public void setValue(String value) {
        this.value = value;
    }
}
