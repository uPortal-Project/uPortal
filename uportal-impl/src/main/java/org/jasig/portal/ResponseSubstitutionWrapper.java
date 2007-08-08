/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.jasig.portal.utils.SubstitutionWriter;

/**
 * Replaces tags in the markup with the tag for the current request.
 * @author Peter Kharchenko, pkharchenko@unicon.net
 * @version $Revision$
 */
public class ResponseSubstitutionWrapper extends HttpServletResponseWrapper {
    protected String sessionTag;
    protected String newTag;

    public ResponseSubstitutionWrapper(HttpServletResponse res, String sessionTag, String newTag) {
        super(res);
        this.sessionTag = sessionTag;
        this.newTag = newTag;
    }

    public ServletOutputStream getOutputStream() throws IOException {
        return getResponse().getOutputStream();
    }

    public PrintWriter getWriter() throws IOException {
        return new PrintWriter(new SubstitutionWriter(getResponse().getWriter(), sessionTag.toCharArray(), newTag.toCharArray(), this.getBufferSize()));
    }

}