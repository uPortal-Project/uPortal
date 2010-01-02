/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
