/**
 * Copyright (c) 2000 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jasig.portal;

import java.util.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;
/**
 * Sets up a channel that produces old-style HTML to render within the portal. 
 *
 *
 * @deprecated This interface will not be supported in uPortal 2.x, because
 *             all channels in 2.0 must produce well-formed XML. 
 *
 * @since uPortal 1.0
 *
 */
public interface IChannel
{
  public void init (ChannelConfig chConfig);
  public String getName ();
  public boolean isMinimizable ();
  public boolean isDetachable ();
  public boolean isRemovable ();
  public boolean isEditable ();
  public boolean hasHelp ();

  public int getDefaultDetachWidth ();
  public int getDefaultDetachHeight ();

  /*a Vector of arrays [fieldLabel, fieldName, fieldSize, fieldMaxSize, desc] */
  public Vector getParameters();

  public void render (HttpServletRequest req, HttpServletResponse res, JspWriter out);
  public void edit (HttpServletRequest req, HttpServletResponse res, JspWriter out);
  public void help (HttpServletRequest req, HttpServletResponse res, JspWriter out);
}
